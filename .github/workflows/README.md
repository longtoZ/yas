# Workflows index

GitHub Actions requires all workflow files to sit flat in this directory (no
subfolders), so grouping is done by filename prefix:

| prefix | count | what it is |
|--------|-------|------------|
| `ci-<service>` | 21 | Per-service test CI (assignment §3 upstream part): on PR/push to `main` touching the service — unit tests + coverage, Checkstyle, SonarCloud. |
| `image-<service>` | 20 | Per-service image CI (assignment §3): on **any branch** push touching the service — build jar/UI, then build & push `baohuyhuy/yas-<service>` to Docker Hub tagged with the **commit SHA** and the **branch name**. Thin callers of the `reusable-*` workflows. |
| `reusable-*` | 2 | The shared build logic. `reusable-build-push-image.yaml` = Maven services (jar → docker). `reusable-build-push-ui-image.yaml` = Next.js UIs (storefront-ui, backoffice-ui). |
| `cd-*` | 4 | Deployment jobs, all against the kubeadm cluster via the `KUBECONFIG` secret. |
| `security-*` | 4 | Scheduled/PR security scans: CodeQL, GitLeaks (nightly), OWASP dependency check, Snyk. |
| `actions/` | – | Local composite action (Java/Maven setup) used by the CI workflows. |

## The four CD workflows

- **`cd-developer-build.yaml`** (assignment §4, workflow name `developer_build`) —
  `workflow_dispatch` with a single `overrides` input (`"tax=dev_tax product=feat_x"`,
  space-separated; one field because dispatch caps at 10 inputs). Named services
  deploy `baohuyhuy/yas-<svc>:<branch>`, everything else the chart default `:main`.
  Prints NodePort access URLs in the run summary.
- **`cd-teardown.yaml`** (assignment §5) — `workflow_dispatch`; helm-uninstalls the
  chosen `services` (or `all`, infra/PVCs preserved) or deletes the whole namespace.
- **`cd-deploy-dev.yaml`** — on every push to `main`: stamps a commit marker in the
  `yas-deploy` GitOps repo → ArgoCD auto-syncs the `dev` namespace.
- **`cd-release-staging.yaml`** — on `v*` tag: builds all images tagged with the
  version (via the reusables), pushes, updates `yas-deploy` → ArgoCD deploys `staging`.

## Conventions

- Every `ci-*`/`image-*` workflow lists its own file and its reusable in `paths:`,
  so editing a workflow re-runs it.
- Docker tags: branch names are sanitized (`/` → `-`) to be valid tags.
- `ci-delivery.yaml` has no `image-*` counterpart — the delivery service is not
  deployed (not in the Updated_Requirements keep-list).
- Renaming a workflow file: update its `paths:` self-reference, all `uses:` lines
  pointing at it, and `docs/demo-guide.md`. The Actions UI keys history by
  workflow file, so old runs stay under the old name.
