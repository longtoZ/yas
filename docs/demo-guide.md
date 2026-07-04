# YAS — Demo Guide (Đồ án 2)

Step-by-step script for demoing every graded part of the assignment
(`Project02_HKII_25_26.txt` + `Updated_Requirements.txt`). Each section maps to a
requirement, lists what to show, the exact commands, and what "success" looks like.

> **Prop branch:** the demo relies on the throwaway branch `dev_tax_service`
> (contains a startup-log marker commit in the tax service). Do **not** delete it
> until all demos are done.

---

## 0. Pre-demo checklist (do this before the teacher is watching)

1. Start the EC2 instances — **master first, then worker** — and wait ~2–3 minutes.
2. Verify the cluster self-recovered:

   ```bash
   export KUBECONFIG=~/.kube/yas-config
   kubectl get nodes                        # both Ready
   kubectl get pods -n yas                  # all Running
   kubectl get pods -A | grep -v Running    # nothing unhealthy left over
   ```

3. Make sure your laptop's hosts file points the YAS domains at the **worker node
   public IP (Elastic IP — stable across stop/start)**:

   ```
   <WORKER_EIP>  storefront.yas.local.com backoffice.yas.local.com api.yas.local.com grafana.yas.local.com
   ```

4. Open in browser tabs ahead of time: GitHub Actions page, Docker Hub
   (`hub.docker.com/u/baohuyhuy`), ArgoCD UI, Grafana (`http://grafana.yas.local.com`),
   Storefront (`http://storefront.yas.local.com:30080`).

---

## 1. §2 — K8s cluster (1 master + 1 worker)

**Claim:** self-managed kubeadm cluster on AWS EC2 (not EKS/Minikube).

1. Show the nodes:

   ```bash
   kubectl get nodes -o wide
   ```

   → 1 master (`t3.medium`) + 1 worker (`m5.4xlarge`), both `Ready`.

2. Show the supporting infra running in-cluster:

   ```bash
   kubectl get pods -A
   ```

   Point out: Zalando Postgres, Strimzi Kafka, Keycloak, Elasticsearch, Redis,
   ingress-nginx (NodePort 30080 + hostPort 80), local-path storage provisioner.

3. Open `http://storefront.yas.local.com:30080` — the shop works end-to-end.
   Backoffice (`http://backoffice.yas.local.com:30080`) redirects to Keycloak
   login: `admin` / `password`.

---

## 2. §3 — CI: per-branch image tagged with commit id

**Claim:** every push on any branch builds the changed service's image and pushes
it to Docker Hub tagged with **the commit SHA and the branch name**.

1. Show the workflow files:
   - `.github/workflows/reusable-build-push-image.yaml` — reusable Maven build (jar → image),
   - `.github/workflows/reusable-build-push-ui-image.yaml` — reusable Next.js build,
   - one thin caller per service, e.g. `.github/workflows/image-tax.yaml`.

2. Live run — push a trivial commit to the demo branch:

   ```bash
   git checkout dev_tax_service
   # edit the startup-log marker string in the tax service, then:
   git commit -am "demo: bump startup marker"
   git push origin dev_tax_service
   ```

3. GitHub → Actions → watch the `tax` image CI run go green.

4. Docker Hub → `baohuyhuy/yas-tax` → show the two fresh tags:
   `<commit-sha>` **and** `dev_tax_service`.

---

## 3. §4 — `developer_build` CD job

**Claim:** the exact scenario from the assignment — developer worked on
`dev_tax_service`, wants tax deployed from that branch while every other service
stays on the default tag.

1. GitHub → Actions → **developer_build** → *Run workflow* with:

   ```
   overrides = tax=dev_tax_service
   ```

   (namespace stays `yas`). Or from the CLI:

   ```bash
   gh workflow run cd-developer-build.yaml -f overrides="tax=dev_tax_service"
   ```

   > One `overrides` input instead of one input per service because
   > `workflow_dispatch` caps at 10 inputs and YAS has 20 deployable services.
   > Format: space-separated `svc=branch` pairs.

2. While it runs, explain what it does: checks out the Helm charts from the
   GitOps config repo (`yas-deploy`), then for each service runs
   `helm upgrade --install --reuse-values` — the named service gets
   `baohuyhuy/yas-tax:dev_tax_service`, everything else keeps the chart default
   `:main` image. Image override keys are `backend.image.*` (or `ui.image.*` for
   the Next.js frontends) — nested under the subchart, not top-level.

3. Open the run's **Summary** page — it prints a per-service table
   (service / image / override-vs-default) and the **access info**: the hosts-file
   line for the worker IP and the NodePort URLs
   (`http://storefront.yas.local.com:30080`, `http://backoffice.yas.local.com:30080`).
   This is the "provide domain:port, developer adds it to their hosts file"
   requirement — show your own hosts file entry as the developer side.

4. Prove the branch image is actually running:

   ```bash
   kubectl get deploy tax -n yas -o jsonpath='{.spec.template.spec.containers[*].image}'
   # → baohuyhuy/yas-tax:dev_tax_service

   kubectl logs deploy/tax -n yas | grep -i "<marker>"
   # → the startup-log marker that only exists on the branch
   ```

---

## 4. §5 — Teardown job

**Claim:** a manual job removes the developer_build deployment.

1. GitHub → Actions → **teardown** → *Run workflow* with:

   ```
   services = tax        (namespace yas, delete_namespace unchecked)
   ```

2. Show the result:

   ```bash
   helm list -n yas          # tax release gone
   kubectl get pvc -n yas    # PVCs / infra untouched
   ```

3. Mention (don't run live) the other modes: `services=all` uninstalls every app
   release but preserves infra + PVCs; `delete_namespace=true` nukes the whole
   namespace. **Never delete bound PVCs casually — that wipes Postgres data.**

4. Re-run developer_build (same input as §4 step 1) to restore tax — this shows
   the deploy/teardown round-trip **and** resets the cluster for the next part.

---

## 5. Bonus — ArgoCD GitOps (dev + staging)

**Claim:** two-repo GitOps (app repo + `yas-deploy` config repo), ApplicationSets
for `dev` and `staging`, ArgoCD handles both.

1. Open the ArgoCD UI → show **26/26 apps Synced + Healthy** and the
   ApplicationSet layout. Briefly show the `yas-deploy` repo structure.

2. **dev auto-deploy on merge to main:** merge any small PR (or push a trivial
   commit) to `main` → the `deploy-dev` workflow stamps a `yas.dev/commit`
   marker in `yas-deploy` → ArgoCD auto-syncs the `dev` namespace. Watch the app
   roll in the ArgoCD UI.

   > Expected quirk: dev rolls even when the `:main` images didn't change,
   > because every merge stamps a new commit marker. That's by design — say so
   > if asked.

3. **staging on release tag:**

   ```bash
   git checkout main && git pull
   git tag v1.0.X
   git push origin v1.0.X
   ```

   → the `release-staging` workflow builds images tagged `v1.0.X`, pushes to
   Docker Hub, updates `yas-deploy` → ArgoCD deploys to `staging`. Prove it:

   ```bash
   kubectl get pods -n staging \
     -o jsonpath='{range .items[*]}{.spec.containers[0].image}{"\n"}{end}' | sort -u
   # → images carry the v1.0.X tag
   ```

---

## 6. Updated requirements — service trim + Observability

### 6a. Trim to the 14-service keep-list

```bash
helm list -n yas
kubectl get deploy -n yas
```

→ exactly the 13 running services (product, cart, order, customer, inventory,
tax, media, search, storefront-bff/ui, backoffice-bff/ui, swagger-ui), and
`sampledata` scaled to 0 — per the requirement, it runs **once** to seed data,
then gets turned off. The removed services (location, payment, payment-paypal,
promotion, rating, recommendation, webhook) are helm-uninstalled.

### 6b. Observability (Grafana / Loki / Tempo / Prometheus / OTel)

Grafana: `http://grafana.yas.local.com` — `admin` / `admin`.

Demo "biết cách sử dụng các chức năng cơ bản" by following **one real request
through all three signals**:

1. Generate traffic: browse the storefront, search a product, add to cart.
2. **Traces — Tempo:** Grafana → Explore → Tempo → search recent traces → open
   one → show spans crossing `storefront-bff → product` (etc.).
3. **Logs — Loki:** Explore → Loki → query e.g. `{namespace="yas"}` filtered to
   a service → show live logs; if the log line carries a trace ID, click through
   log → trace to show correlation.
4. **Metrics — Prometheus:** Explore → Prometheus → JVM / HTTP-server metrics
   coming from the OTel agent; then open the project **"Observability Dashboard"**
   (Dashboards → browse).
5. If asked *how* telemetry gets in: the opentelemetry-operator auto-injects the
   OTel Java agent via the `Instrumentation` CR + `inject-java` pod annotation.
   Proof on a live pod:

   ```bash
   kubectl get pod -n yas -l app.kubernetes.io/name=product -o yaml | grep -A2 JAVA_TOOL_OPTIONS
   # or show the opentelemetry-auto-instrumentation init container
   ```

   Pipeline: apps → otel-collector → Tempo (traces) / Prometheus (metrics) /
   Loki (logs) → Grafana datasources managed by grafana-operator.

---

## 7. Bonus — Service Mesh (Istio + Kiali) — DEPLOYED

Istio 1.29.5, mesh over the `yas` namespace, Kiali at
`http://kiali.yas.local.com:30080/kiali` (add `kiali.yas.local.com` to your
hosts line). Full runbook + manifests: [`k8s/istio/README.md`](../k8s/istio/README.md);
captured evidence in `k8s/istio/tests/`.

1. **mTLS (STRICT):** show `k8s/istio/10-peer-authentication-strict.yaml` and
   `kubectl get peerauthentication -n yas`. Prove it live:

   ```bash
   # meshed pod -> 200 over mTLS
   kubectl exec -n yas deploy/curl-mesh -- curl -s -o /dev/null -w '%{http_code}\n' http://product:8090/actuator/health
   # non-mesh pod (default ns) -> Connection reset by peer
   kubectl exec -n default deploy/mtls-test -- curl -v --max-time 10 http://product.yas:8090/actuator/health
   ```

   In Kiali, enable the *Security* display option → padlock icons on all edges.
   Mention the ingress trick: nginx carries an outbound-only sidecar +
   `service-upstream`/`upstream-vhost` annotations, so even browser traffic is
   mTLS on its last hop.

2. **Topology:** run the traffic loop from `k8s/istio/README.md` §5 for a
   couple of minutes → Kiali Graph (namespace `yas`, versioned app graph) shows
   `ingress-nginx → storefront-bff → product/search/storefront-ui`,
   `product → media`, `search → kafka + elasticsearch`. Screenshot it.

3. **AuthorizationPolicy:** only the BFF ServiceAccounts may call `search`:

   ```bash
   kubectl exec -n yas deploy/curl-as-bff -- curl -s -o /dev/null -w '%{http_code}\n' \
     'http://search/search/storefront/catalog-search?keyword=laptop&page=0'   # 200 (SA storefront-bff)
   kubectl exec -n yas deploy/curl-mesh -- curl -s \
     'http://search/search/storefront/catalog-search?keyword=laptop&page=0'   # RBAC: access denied (403)
   ```

   And the real user flow still works: storefront search in the browser → 200.

4. **Retry policy:** follow `k8s/istio/README.md` §7 — apply the fault
   EnvoyFilter (tax aborts 50% with 500), show ~8/20 success **without** the
   retry VirtualService, then 20/20 **with** it; finish with the tax sidecar
   access log showing the same `x-request-id` logged as
   `500 FI fault_filter_abort` (x2) then `200 via_upstream`.
   **Delete the fault filter afterwards.**

---

## 8. Report reminders

- Screenshot **every configuration step** as you demo (Actions runs, Docker Hub
  tags, ArgoCD UI, Grafana dashboards, kubectl outputs).
- Report filename: `<MSSV1>_<MSSV2>_<MSSV3>_<MSSV4>.docx`, MSSVs sorted ascending.
- After all demos: delete the prop branch —
  `git push origin --delete dev_tax_service`.
- Stop the EC2 instances when done (EBS persists; cluster self-recovers on the
  next start, master first).
