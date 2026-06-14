# YAS on AWS — Cluster Setup & Infrastructure Deployment

End-to-end guide to running YAS on a self-managed `kubeadm` Kubernetes cluster on AWS EC2:
**1 master + 1 worker**, deployed remotely from your laptop, up to and including the three
infrastructure scripts (`setup-keycloak.sh`, `setup-redis.sh`, `setup-cluster.sh`).

> Application deployment (`deploy-yas-applications.sh`) and access/ingress are covered separately.

---

## 0. Overview

| Component | Choice |
|---|---|
| Kubernetes | `kubeadm` v1.30, self-managed on EC2 |
| Nodes | master `t3.medium` (2 vCPU/4 GB), worker `t3.2xlarge` (8 vCPU/32 GB) |
| OS | Ubuntu Server 22.04 LTS |
| Container runtime | containerd (systemd cgroups) |
| CNI | Calico v3.28.0 (pod CIDR `192.168.0.0/16`) |
| Storage | Rancher local-path-provisioner v0.0.30 (default StorageClass) |
| Ingress | ingress-nginx, exposed as NodePort `30080`/`30443` |
| Domain | `yas.local.com` (from `cluster-config.yaml`) |
| Access | `kubectl`/`helm` run from laptop against the master's API server |

Both instances have **Elastic IPs** attached, so their public IPs are stable across stop/start.

---

## 1. AWS infrastructure

### 1.1 Key pair
EC2 → Key Pairs → create `yas-k8s` → download `yas-k8s.pem`:
```bash
chmod 400 ~/.ssh/yas-k8s.pem
```

### 1.2 Security group `yas-k8s-sg`
Inbound rules:

| Type | Port | Source | Purpose |
|---|---|---|---|
| SSH | 22 | My IP | shell access |
| Custom TCP | 6443 | My IP | kubectl → API server |
| All traffic | all | **this SG (self-reference)** | node↔node, etcd, kubelet, Calico, pods |
| Custom TCP | 30000–32767 | My IP | NodePort range (ingress + app access) |

### 1.3 EC2 instances
- **master** — `t3.medium`, 30 GB gp3, SG `yas-k8s-sg`, key `yas-k8s`
- **worker** — `t3.2xlarge`, 60 GB gp3, SG `yas-k8s-sg`, key `yas-k8s`

### 1.4 Elastic IPs
Allocate 2 EIPs and associate one to each instance. Record:
- `MASTER_EIP`  — master public IP
- `WORKER_EIP`  — worker public IP

Private IPs are retained across stop/start regardless; EIPs keep the **public** IPs stable too.

---

## 2. Prepare BOTH nodes

SSH into each (`ssh -i ~/.ssh/yas-k8s.pem ubuntu@<EIP>`) and run **all of this on master AND worker**:

```bash
# --- disable swap ---
sudo swapoff -a
sudo sed -i '/ swap / s/^/#/' /etc/fstab

# --- kernel modules + sysctl ---
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF
sudo modprobe overlay && sudo modprobe br_netfilter

cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF
sudo sysctl --system

# --- containerd ---
sudo apt-get update && sudo apt-get install -y containerd
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml >/dev/null
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml
sudo systemctl restart containerd && sudo systemctl enable containerd

# --- kubeadm / kubelet / kubectl v1.30 ---
sudo apt-get install -y apt-transport-https ca-certificates curl gpg
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.30/deb/Release.key \
  | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.30/deb/ /" \
  | sudo tee /etc/apt/sources.list.d/kubernetes.list
sudo apt-get update && sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
```

---

## 3. Initialize the master (master only)

Include the master's EIP in the API server cert SANs so the laptop can connect over TLS:

```bash
sudo kubeadm init \
  --pod-network-cidr=192.168.0.0/16 \
  --apiserver-cert-extra-sans=<MASTER_EIP>
```

Copy the printed `kubeadm join ...` command for the worker. Then set up local kubectl on the master:

```bash
mkdir -p $HOME/.kube
sudo cp /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

Install the Calico CNI:
```bash
kubectl apply -f https://raw.githubusercontent.com/projectcalico/calico/v3.28.0/manifests/calico.yaml
```

---

## 4. Join the worker (worker only)

```bash
sudo kubeadm join <master-private-ip>:6443 --token <token> \
  --discovery-token-ca-cert-hash sha256:<hash>
```
> Lost the command? On the master: `kubeadm token create --print-join-command`

Verify on the master:
```bash
kubectl get nodes      # both Ready (Calico takes ~1 min)
```

---

## 5. Remote access from your laptop

So you can run `kubectl`/`helm` (and the deploy scripts) from your local clone.

```bash
# one-time: copy the kubeconfig off the master
scp -i ~/.ssh/yas-k8s.pem ubuntu@<MASTER_EIP>:/home/ubuntu/.kube/config ~/.kube/yas-config
```

Edit `~/.kube/yas-config` → set the API endpoint to the master's EIP:
```yaml
    server: https://<MASTER_EIP>:6443
```
Because the EIP was added to the cert SANs in step 3, TLS verifies cleanly (no
`insecure-skip-tls-verify` needed).

```bash
export KUBECONFIG=~/.kube/yas-config
kubectl get nodes      # confirms remote control works
```

Install `helm` and `yq` locally (the scripts need them):
```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
sudo wget -qO /usr/local/bin/yq https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 && sudo chmod +x /usr/local/bin/yq
```

---

## 6. Cluster prerequisites (REQUIRED before any scripts)

A bare `kubeadm` cluster ships with **no storage provisioner and no ingress controller** —
unlike Minikube. Both must be installed or every stateful component will hang `Pending`.

### 6.1 Storage provisioner (critical)

Without a default StorageClass, all PVCs (Postgres, Redis, Elasticsearch, Zookeeper) stay
`Pending` → databases never start → Keycloak and every backend crash-loop.

```bash
export KUBECONFIG=~/.kube/yas-config
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.30/deploy/local-path-storage.yaml
kubectl patch storageclass local-path \
  -p '{"metadata":{"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'

kubectl get storageclass        # local-path should show "(default)"
```

> `local-path` stores volumes as directories on the worker's disk. Fine for this single-worker
> setup. **Do not delete a bound PVC** — a PVC's storage class is immutable, and recreating
> Postgres' PVC wipes the DB and can leave the Zalando operator in `SyncFailed` (see §7 caveat).

### 6.2 Ingress controller (NodePort)

The YAS charts route by hostname via an `nginx` Ingress. Expose the controller as NodePort so
the app is reachable through `<WORKER_EIP>:30080`:

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx --create-namespace \
  --set controller.service.type=NodePort \
  --set controller.service.nodePorts.http=30080 \
  --set controller.service.nodePorts.https=30443
```

---

## 7. Run the infrastructure scripts

From your **local clone**, with `KUBECONFIG` exported:

```bash
export KUBECONFIG=~/.kube/yas-config
cd ~/yas/k8s/deploy
```

Run in this order:

```bash
./setup-keycloak.sh      # Keycloak CRDs + operator + Keycloak instance
./setup-redis.sh         # Redis (bitnami) for backend sessions
./setup-cluster.sh       # postgres / kafka / elasticsearch operators + clusters (+ observability)
```

> If a script isn't executable (`Permission denied`), run it with `bash <script>.sh`.

> **Observability is optional.** `setup-cluster.sh` also installs Loki/Tempo/Prometheus/Grafana/
> OTel/Promtail (lines ~67–120). The assignment does not require them — comment those blocks out
> if you want to save RAM on the single worker.

### Verify infrastructure is healthy
```bash
watch kubectl get pods -A
```
Wait for these namespaces to be Running/Ready before deploying apps:
`postgres`, `kafka`, `elasticsearch`, `keycloak`, `redis`.

### Known caveat — Postgres operator `SyncFailed`

The Zalando Postgres operator creates the `yasadminuser` role and the per-service databases
(including `keycloak`) **only after** the Spilo pod is labeled `spilo-role=master`. If it raced
ahead and shows `SyncFailed`, the role/databases won't exist and Keycloak will report
`password authentication failed for user "yasadminuser"`.

Fix — force the operator to re-sync once the pod has the master label:
```bash
# confirm the master label is present and Patroni has a leader
kubectl get pod postgresql-0 -n postgres --show-labels | grep spilo-role=master
kubectl exec -n postgres postgresql-0 -c postgres -- patronictl list

# restart the operator to re-sync
kubectl rollout restart deploy/postgres-operator -n postgres
kubectl rollout status  deploy/postgres-operator -n postgres

# verify role + database now exist
kubectl get postgresql postgresql -n postgres -o jsonpath='{.status.PostgresClusterStatus}'   # → Running
kubectl exec -n postgres postgresql-0 -c postgres -- psql -U postgres -tAc "\du" | grep yasadmin
kubectl exec -n postgres postgresql-0 -c postgres -- psql -U postgres -tAc "SELECT datname FROM pg_database WHERE datname='keycloak';"
```

If Keycloak was already crash-looping, clear its backoff after the DB is ready:
```bash
kubectl delete pod keycloak-0 -n keycloak
kubectl get pod keycloak-0 -n keycloak -w     # → 1/1 Running after first-boot migrations (~60s)
```

### Known caveat — Kafka never gets created (Strimzi version skew)

`setup-cluster.sh` installs the Strimzi operator **unpinned** (`helm install ... strimzi/strimzi-kafka-operator`),
so it pulls the newest release. Recent Strimzi (the **1.0** line) is **KRaft-only** — it dropped
Zookeeper support and the `kafka.strimzi.io/v1beta2` API. But the YAS `kafka-cluster` chart is
**Zookeeper-based and uses `v1beta2`**, so applying the `Kafka` CR fails silently (the script has
`set -x` but no error-stop) and **no broker is ever created**. Symptoms:

- `kubectl get kafka -n kafka` → `No resources found`, the `kafka` namespace has only the operator.
- `recommendation` / `webhook` / `payment` crash with
  `No resolvable bootstrap urls given in bootstrap.servers`.
- Re-applying the chart errors: `no matches for kind "Kafka" in version "kafka.strimzi.io/v1beta2"`.

Fix — pin Strimzi to the last Zookeeper/`v1beta2`-compatible line (**0.45.x**). Helm never
overwrites existing CRDs, so the v1-only CRDs must be **deleted first** (safe — there are no
`Kafka` CRs yet):

```bash
helm uninstall kafka-operator -n kafka
kubectl delete $(kubectl get crd -o name | grep strimzi.io)        # remove the v1-only CRDs
helm install kafka-operator strimzi/strimzi-kafka-operator \
  --namespace kafka --version 0.45.2                                # serves v1beta2 + Zookeeper
kubectl rollout status deploy/strimzi-cluster-operator -n kafka

# re-apply the Kafka cluster (same args as setup-cluster.sh's kafka block)
helm upgrade --install kafka-cluster ./kafka/kafka-cluster --namespace kafka \
  --set kafka.replicas="$(yq -r '.kafka.replicas' cluster-config.yaml)" \
  --set zookeeper.replicas="$(yq -r '.zookeeper.replicas' cluster-config.yaml)" \
  --set postgresql.username="$(yq -r '.postgresql.username' cluster-config.yaml)" \
  --set postgresql.password="$(yq -r '.postgresql.password' cluster-config.yaml)"

kubectl get kafka kafka-cluster -n kafka -w     # wait for READY=True; broker + zookeeper pods come up
```

> **Better:** avoid the skew entirely by pinning the version in `setup-cluster.sh` — change the
> kafka-operator install to `helm upgrade --install kafka-operator strimzi/strimzi-kafka-operator
> --create-namespace --namespace kafka --version 0.45.2`. After Kafka is up, restart the dependent
> apps: `kubectl rollout restart deploy/recommendation deploy/webhook deploy/payment -n yas`.

At this point the cluster and all infrastructure are up — ready to deploy the applications (§8).

---

## 8. Deploy the YAS applications

**Order matters.** The shared configuration ConfigMaps must exist *before* the applications,
or every backend/BFF pod hangs in `ContainerCreating` forever (it mounts ConfigMaps that don't
yet exist). The config step is a **separate script** that is easy to miss:

```bash
export KUBECONFIG=~/.kube/yas-config
cd ~/yas/k8s/deploy

bash deploy-yas-configuration.sh    # ← creates yas-configuration-configmap, gateway routes,
                                    #    and the per-service *-application-configmap ConfigMaps
bash deploy-yas-applications.sh     # BFFs, UIs, swagger, and ~16 microservices
```

`deploy-yas-applications.sh` installs services one at a time with 60s sleeps, so it takes
~20+ min. Watch progress:
```bash
watch kubectl get pods -n yas
```

### Symptom — backends stuck in `ContainerCreating`
If you ran the apps without the config step, you'll see:
```
MountVolume.SetUp failed ... configmap "yas-configuration-configmap" not found
```
(UI pods run fine because they don't mount those ConfigMaps.) Fix:
```bash
bash deploy-yas-configuration.sh                       # create the missing ConfigMaps
# kubelet's mount-retry backoff is slow; force an immediate remount:
kubectl get pods -n yas | grep ContainerCreating | awk '{print $1}' | xargs -r kubectl delete pod -n yas
```
The Deployments recreate the pods immediately and they mount cleanly. Stakater **Reloader**
(`yas-reloader`) is also installed, so editing a ConfigMap later auto-restarts the dependent pods.

### Symptom — BFFs crash with `UnknownHostException: identity.yas.local.com`

The BFFs validate the OIDC issuer `http://identity.yas.local.com/realms/Yas` at startup, but
that hostname is the **external ingress name** and doesn't resolve inside the cluster (NXDOMAIN).
Fix: add a CoreDNS `hosts` entry mapping the `*.yas.local.com` names to the **ingress-nginx
controller ClusterIP**, so in-cluster requests route through ingress → Keycloak (the issuer string
stays identical, so tokens still validate).

```bash
INGRESS_IP=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.spec.clusterIP}')
# edit the CoreDNS Corefile and add, inside the `.:53 { ... }` block:
#   hosts {
#       <INGRESS_IP> identity.yas.local.com api.yas.local.com storefront.yas.local.com backoffice.yas.local.com
#       fallthrough
#   }
kubectl -n kube-system edit configmap coredns
kubectl -n kube-system rollout restart deployment/coredns
kubectl rollout restart deployment/backoffice-bff deployment/storefront-bff -n yas
```
Verify: `kubectl run dnstest --rm -it --image=busybox:1.36 -n yas -- nslookup identity.yas.local.com`
should resolve to the ingress ClusterIP.

### Symptom — a few services crash-loop on image/app issues (not cluster faults)

Some services published as `:latest` are stale or broken relative to the source in this repo:

| Service | Cause | Resolution |
|---|---|---|
| `payment` | `:latest` predates the `enabled`-column fix; Liquibase fails on `column "is_enabled" ... does not exist` | build/deploy a `payment` image from the fixed branch (the **`developer_build`** workflow) |
| `payment-paypal` | upstream `:latest` jar is broken (`no main manifest attribute`) | deploy a rebuilt image |
| `search` | ES 8.8 rejects the client's `indices.exists` (client/server skew) | search build aligned to ES 8.8 |

These are deployment **image-tag** choices, not cluster misconfigurations — exactly what the CD
pipeline (per-branch images + `developer_build`) is meant to manage.

---

## Appendix — Stop / start the instances

EIPs keep public IPs stable, so after a stop/start you only need to start the nodes
(master first, then worker) and wait ~3–5 min:

```bash
export KUBECONFIG=~/.kube/yas-config
kubectl get nodes     # both Ready
kubectl get pods -A   # Patroni recovers Postgres; pods reschedule automatically
```

EBS volumes (and therefore all cluster state and PVC data) persist while stopped.
