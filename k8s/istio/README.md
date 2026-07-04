# YAS Service Mesh — Istio + Kiali (Đồ án 2 bonus)

Istio **1.29.5** service mesh over the `yas` namespace: strict mTLS between all
services, Kiali topology, service-to-service AuthorizationPolicy, and automatic
retry policy — the four deliverables of the Service Mesh bonus.

All commands assume `export KUBECONFIG=~/.kube/yas-config`.

## Architecture decisions

- **Sidecar mode, `minimal` profile** (istiod only). We keep the existing
  ingress-nginx as the edge instead of adding an Istio ingress gateway; nginx
  itself joins the mesh so the last hop to the pods is mTLS (see §3).
- **Mesh scope = `yas` namespace only.** Infra (postgres/kafka/redis/ES/keycloak)
  and the ArgoCD-managed dev/staging namespaces stay outside the mesh. Outbound
  calls from meshed pods to non-mesh infra keep working (Istio passthrough).
- **Metrics reuse the existing kube-prometheus-stack** in `observability` —
  no second Prometheus. Kiali reads from it and links traces to the existing Tempo.

## 1. Install the control plane

```bash
# istioctl 1.29.5 lives in ~/istio-1.29.5/bin (curl -L https://istio.io/downloadIstio | ISTIO_VERSION=1.29.5 sh -)
istioctl x precheck
istioctl install --set profile=minimal \
  --set meshConfig.defaultConfig.holdApplicationUntilProxyStarts=true \
  --set meshConfig.enableTracing=false -y
```

`holdApplicationUntilProxyStarts` makes app containers wait for the sidecar so
Java services don't crash-loop on DB connections during rollouts.

`enableTracing=false` disables Envoy's legacy tracing config (default: 1% random
sampling, no collector). Without this, Envoy stamps a not-sampled trace context
on every request entering the mesh, and the apps' OTel java agents (parentbased
sampler) obey it — killing ~99% of application traces. Envoy spans were never
exported anywhere, so nothing is lost; Kiali reads app traces from Tempo.

## 2. Join the yas workloads to the mesh

```bash
kubectl label namespace yas istio-injection=enabled --overwrite
kubectl rollout restart deployment -n yas
```

All pods become `2/2` (app + `istio-proxy`). Verify sync: `istioctl proxy-status`.

**OTel-agent injection gotcha:** with `holdApplicationUntilProxyStarts`, Istio
puts `istio-proxy` FIRST in the containers list, and the opentelemetry-operator
injects `JAVA_TOOL_OPTIONS`/`OTEL_*` into `containers[0]` by default — i.e. into
Envoy, not the Java app → meshed pods silently stop producing traces. Every Java
chart must therefore set both pod annotations (already in yas-deploy charts):

```yaml
instrumentation.opentelemetry.io/inject-java: "true"
instrumentation.opentelemetry.io/container-names: "<app-container-name>"
```

## 3. Bring ingress-nginx into the mesh

Without this, STRICT mTLS (§4) would break all browser traffic, because nginx
has no sidecar and would speak plaintext to the pods. Three pieces:

```bash
# 3a. Outbound-only sidecar on the controller (label triggers injection;
#     inbound stays un-intercepted so NodePort/hostPort traffic still reaches nginx)
kubectl -n ingress-nginx patch deployment ingress-nginx-controller --type=strategic -p '{
  "spec": {"template": {"metadata": {
    "labels":      {"sidecar.istio.io/inject": "true"},
    "annotations": {"traffic.sidecar.istio.io/includeInboundPorts": ""}
  }}}}'
# single worker + hostPort 80 -> the RollingUpdate deadlocks; delete the old pod:
kubectl delete pod -n ingress-nginx -l app.kubernetes.io/component=controller

# 3b. Proxy to the Service VIP instead of pod IPs (Envoy can only mTLS-route service traffic)
# 3c. Rewrite the upstream Host header to the in-mesh authority — Envoy routes by
#     Host; with the public host (storefront.yas.local.com) it would fall into the
#     plaintext PassthroughCluster and get rejected under STRICT (503).
for ing in storefront-bff backoffice-bff media swagger-ui; do
  kubectl annotate ingress $ing -n yas \
    nginx.ingress.kubernetes.io/service-upstream=true --overwrite
done
kubectl annotate ingress storefront-bff -n yas nginx.ingress.kubernetes.io/upstream-vhost=storefront-bff.yas.svc.cluster.local --overwrite
kubectl annotate ingress backoffice-bff -n yas nginx.ingress.kubernetes.io/upstream-vhost=backoffice-bff.yas.svc.cluster.local --overwrite
kubectl annotate ingress media          -n yas nginx.ingress.kubernetes.io/upstream-vhost=media.yas.svc.cluster.local          --overwrite
kubectl annotate ingress swagger-ui     -n yas nginx.ingress.kubernetes.io/upstream-vhost=swagger-ui.yas.svc.cluster.local     --overwrite
```

## 4. mTLS — STRICT (deliverable 1)

```bash
kubectl apply -f 10-peer-authentication-strict.yaml
```

Verify (evidence: `tests/mtls-test-results.txt`). The test clients are
Deployments (`50-mesh-test-clients.yaml`) so they survive node stop/start:

```bash
kubectl apply -f 50-mesh-test-clients.yaml   # curl-mesh, curl-as-bff (yas), mtls-test (default)

# meshed client -> 200 over mTLS
kubectl exec -n yas deploy/curl-mesh -c curl-mesh -- curl -s -o /dev/null -w '%{http_code}\n' http://product:8090/actuator/health

# NON-mesh client -> connection reset (plaintext rejected)
kubectl exec -n default deploy/mtls-test -- curl -v --max-time 10 http://product.yas:8090/actuator/health
```

## 5. Kiali + Prometheus/Tempo integration (deliverable 2)

```bash
kubectl apply -f 20-istio-prometheus-monitors.yaml   # envoy + istiod metrics into the existing Prometheus

helm repo add kiali https://kiali.org/helm-charts && helm repo update kiali
helm upgrade --install kiali-server kiali/kiali-server -n istio-system \
  --set auth.strategy=anonymous \
  --set external_services.prometheus.url=http://prometheus-kube-prometheus-prometheus.observability:9090 \
  --set external_services.tracing.enabled=true \
  --set external_services.tracing.provider=tempo \
  --set external_services.tracing.internal_url=http://tempo.observability:3200 \
  --set external_services.tracing.use_grpc=false

kubectl apply -f 21-kiali-ingress.yaml
```

Browse: add `<WORKER_EIP>  kiali.yas.local.com` to your hosts file →
**http://kiali.yas.local.com:30080/kiali** → Graph → namespace `yas`, enable
the *Security* display option to see the mTLS padlocks. Generate traffic first:

```bash
EIP=<WORKER_EIP>
for i in $(seq 1 100); do
  curl -s -o /dev/null -H "Host: storefront.yas.local.com" "http://$EIP:30080/api/product/storefront/products/featured?pageNo=0&pageSize=10"
  curl -s -o /dev/null -H "Host: storefront.yas.local.com" "http://$EIP:30080/api/search/storefront/catalog-search?keyword=laptop&page=0"
  curl -s -o /dev/null -H "Host: storefront.yas.local.com" "http://$EIP:30080/"
  sleep 2
done
```

Observed topology: `ingress-nginx → storefront-bff → {product, search, storefront-ui}`,
`product → media`, `search → kafka + elasticsearch`, services → postgres.

## 6. AuthorizationPolicy (deliverable 3 — connection policy)

Only the two BFFs may call `search`; identity = ServiceAccount from the mTLS
client certificate (this is why STRICT is a prerequisite).

```bash
kubectl apply -f 30-authorization-policy-search.yaml

# allowed identity (client Deployment running AS the storefront-bff ServiceAccount) -> 200
kubectl exec -n yas deploy/curl-as-bff -- curl -s -o /dev/null -w '%{http_code}\n' \
  'http://search/search/storefront/catalog-search?keyword=laptop&page=0'

# denied identity (default ServiceAccount) -> 403 "RBAC: access denied"
kubectl exec -n yas deploy/curl-mesh -- curl -s \
  'http://search/search/storefront/catalog-search?keyword=laptop&page=0'
```

Evidence: `tests/authorization-policy-test-results.txt`.

## 7. Retry policy (deliverable 3 — retryable)

Client sidecars retry 5xx from `tax` up to 3 times (`41-virtualservice-tax-retry.yaml`).
To demo, inject chaos server-side — the tax sidecar aborts 50% of inbound
requests with 500 (`42-envoyfilter-tax-fault-500.yaml`; server-side on purpose:
client-side VirtualService faults are never retried by design):

```bash
kubectl apply -f 40-telemetry-access-logging.yaml     # access logs to see attempts
kubectl apply -f 42-envoyfilter-tax-fault-500.yaml    # chaos ON

# WITHOUT retry policy: ~half the calls fail
kubectl delete virtualservice tax-retry -n yas --ignore-not-found
kubectl exec -n yas deploy/curl-mesh -- sh -c 'for i in $(seq 1 20); do curl -s -o /dev/null -w "%{http_code} " http://tax:8090/actuator/health; done; echo'

# WITH retry policy: 20/20 succeed against the same 50% fault rate
kubectl apply -f 41-virtualservice-tax-retry.yaml
kubectl exec -n yas deploy/curl-mesh -- sh -c 'for i in $(seq 1 20); do curl -s -o /dev/null -w "%{http_code} " http://tax:8090/actuator/health; done; echo'

# retry evidence: same x-request-id logged with N x "500 FI fault_filter_abort" + final "200 via_upstream"
kubectl logs -n yas deploy/tax -c istio-proxy --tail=40 | grep '"GET /actuator/health'

kubectl delete -f 42-envoyfilter-tax-fault-500.yaml   # chaos OFF — do not leave applied!
```

Result: without retries **8/20 OK**; with retries **20/20 OK**.
Evidence: `tests/retry-test-results.txt`.

## Files

| file | purpose |
|------|---------|
| `10-peer-authentication-strict.yaml` | namespace-wide STRICT mTLS |
| `20-istio-prometheus-monitors.yaml` | envoy/istiod metrics → existing Prometheus |
| `21-kiali-ingress.yaml` | Kiali UI at kiali.yas.local.com |
| `30-authorization-policy-search.yaml` | only BFFs may call search |
| `40-telemetry-access-logging.yaml` | Envoy access logs in yas |
| `41-virtualservice-tax-retry.yaml` | retry policy for tax |
| `42-envoyfilter-tax-fault-500.yaml` | demo-only 50% fault on tax (apply → demo → delete) |
| `50-mesh-test-clients.yaml` | curl test-client Deployments (survive node restarts) |
| `tests/*.txt` | captured test evidence for the report |

## Gotchas learned (read before touching)

- **Sidecar injection trigger is the pod LABEL** `sidecar.istio.io/inject: "true"`
  (the annotation of the same name does nothing on modern Istio).
- **Envoy routes HTTP by Host header.** Anything proxied into the mesh must carry
  the in-mesh authority (`svc.ns.svc.cluster.local`) — hence `upstream-vhost`
  on every Ingress. A wrong Host silently falls into the plaintext
  PassthroughCluster → 503 `upstream connect error ... connection termination`.
- **Single worker + hostPort**: rolling updates of ingress-nginx deadlock
  (new pod Pending on the port); delete the old pod to let it swap.
- Prometheus scrapes sidecars on port **15020** (merged metrics), which Istio
  always excludes from mTLS interception — scraping keeps working under STRICT.
- The retry-demo fault filter (`42-*`) must be **deleted after the demo**, or
  half of all real tax calls will be served a 500 (masked by retries, but still).
- Test clients `curl-mesh`, `curl-as-bff` (yas) and `mtls-test` (default) are
  Deployments (`50-mesh-test-clients.yaml`) so they come back automatically after a
  node stop/start. Remove when no longer needed: `kubectl delete -f 50-mesh-test-clients.yaml`.

## Teardown (mesh only, apps untouched)

```bash
kubectl delete -f .                       # policies, monitors, kiali ingress
helm uninstall kiali-server -n istio-system
kubectl label namespace yas istio-injection-
kubectl rollout restart deployment -n yas # pods come back 1/1, no sidecar
# revert ingress-nginx: remove the inject label + annotations, roll the pod
istioctl uninstall --purge -y && kubectl delete namespace istio-system
```
