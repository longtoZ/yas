# Team Task Distribution for Project 01 - YAS CI Implementation

## 1. Scope and Constraints

This plan is based on:

- [requirements.md]
- the source tree under `yas`

The assignment requires the team to build a Jenkins-based CI solution for a fork of the `yas` monorepo and submit evidence after 3 weeks.

The local `yas` source currently contains:

- 20 backend Maven modules in the root reactor
- 1 additional Maven project outside the root reactor: `automation-ui`
- 2 Node.js frontends: `backoffice` and `storefront`
- existing GitHub Actions examples under `yas/.github/workflows`
- JaCoCo, Failsafe, Checkstyle, and OWASP dependency-check hooks already present in Maven configuration

The main missing pieces relative to the assignment are:

- Jenkins pipeline design and Jenkinsfiles or shared pipeline config
- GitHub branch protection in the team fork
- Jenkins multibranch scanning
- Jenkins-side changed-service detection for the monorepo
- explicit coverage gate above 70%
- SonarQube or SonarCloud integration
- Snyk integration
- final evidence collection and report preparation

## 2. Requirement Coverage Matrix

Every requirement below has a primary owner and a backup reviewer so nothing is unassigned.

| Requirement from `requirements.md` | Concrete deliverable | Primary owner | Backup reviewer |
| --- | --- | --- | --- |
| Fork team repository from `yas` | Team fork created, all members added, repo URL ready for report | Member 2 | Member 1 |
| Disallow direct push to `main` | Branch protection rule screenshot and working restriction | Member 2 | Member 4 |
| Require 2 approvals per PR | Branch protection setting and review rule tested on one PR | Member 2 | Member 1 |
| Require passing CI before merge | GitHub required status checks linked to Jenkins jobs | Member 1 | Member 2 |
| Jenkins scans repositories and runs each branch | Jenkins multibranch pipeline, webhook, branch indexing screenshot | Member 1 | Member 3 |
| Pipeline has `Test` and `Build` phases | Jenkins stages visible in every service pipeline | Member 1 | Member 4 |
| Upload test results | JUnit reports archived in Jenkins | Member 1 | Member 3 |
| Report test coverage | JaCoCo report archived and shown in PR or Jenkins | Member 4 | Member 1 |
| Trigger only the changed service | Change-detection script and test cases for changed-path scenarios | Member 4 | Member 1 |
| Add unit tests to increase coverage | Separate service branches with test PRs | Members 1-4 by service ownership | Cross-review by the other 3 |
| Coverage must be > 70% | Pipeline gate and proof on priority services | Member 4 | Member 3 |
| Gitleaks scan | Jenkins or repo job output and screenshot | Member 3 | Member 2 |
| SonarQube or SonarCloud scan | Quality scan output and screenshot | Member 3 | Member 1 |
| Snyk scan | Dependency scan output and screenshot | Member 3 | Member 4 |
| Report must contain screenshots and one open PR | Shared evidence folder and submission checklist | Member 2 | Member 4 |

## 3. Full Repo Coverage Plan

To ensure the task plan covers the whole project, all buildable components in `yas` are assigned below.

### Buildable components found in the repo

- Backend Maven modules in root reactor:
  - `backoffice-bff`
  - `cart`
  - `common-library`
  - `customer`
  - `delivery`
  - `inventory`
  - `location`
  - `media`
  - `order`
  - `payment`
  - `payment-paypal`
  - `product`
  - `promotion`
  - `rating`
  - `recommendation`
  - `sampledata`
  - `search`
  - `storefront-bff`
  - `tax`
  - `webhook`
- Additional Maven project:
  - `automation-ui`
- Node.js frontends:
  - `backoffice`
  - `storefront`

### Shared non-build areas that still affect CI

- `.github`
- `checkstyle`
- `deployment`
- `docker`
- `docs`
- `identity`
- `k8s`
- `kafka`
- `nginx`
- `scripts`
- `tempo-data`

If any of these shared areas changes, Jenkins should not treat it as a single-service change. It should run the shared validation path defined by Member 4 and Member 1.

## 4. Balanced Task Distribution for 4 Members

The workload is balanced by effort, not only by service count:

- Member 1 and Member 2 each own one mandatory cross-project stream, so they get slightly fewer heavy services.
- Member 3 and Member 4 each own one advanced cross-project stream, plus a larger service set.
- Every member owns 1 cross-project stream and 5 to 6 buildable components.

### Member 1: Jenkins platform owner

Cross-project responsibility:

- Jenkins server setup
- Jenkins credentials and GitHub webhook
- Multibranch pipeline
- JUnit artifact publishing
- Jenkins screenshots for the report

Owned buildable components:

- `cart`
- `media`
- `location`
- `delivery`
- `storefront-bff`
- `storefront`

Specific tasks:

- Create Jenkins job structure for branch discovery.
- Define the default stages for all services:
  - `Checkout`
  - `Detect Change`
  - `Test`
  - `Coverage`
  - `Build`
- Make sure Jenkins stores test XML and build logs for all owned components.
- Add or improve tests for `cart`, `media`, `location`, `delivery`, and `storefront-bff` until each owned backend service used in demo is above 70%.
- Ensure `storefront` frontend pipeline has install, lint, and build steps integrated into Jenkins.
- Open separate service branches for test additions:
  - `test/cart-coverage`
  - `test/media-coverage`
  - `test/location-coverage`
  - `test/delivery-coverage`
  - `test/storefront-bff-coverage`

Required outputs:

- Jenkins multibranch pipeline running
- screenshot of branch indexing
- screenshot of one successful pipeline with `Test` and `Build`
- JUnit artifact archived for at least one owned service

### Member 2: GitHub governance and submission owner

Cross-project responsibility:

- Team fork setup
- branch protection
- reviewer policy
- PR template
- CODEOWNERS
- report evidence tracking
- final screenshot checklist

Owned buildable components:

- `backoffice`
- `backoffice-bff`
- `customer`
- `rating`
- `tax`

Specific tasks:

- Create the team fork and verify all members have access.
- Configure `main` protection:
  - disable direct push
  - require 2 approvals
  - require CI checks before merge
- Define branch naming:
  - `infra/...`
  - `test/<service-name>-coverage`
  - `fix/...`
  - `report/...`
- Create PR template with checklist:
  - changed service name
  - test evidence
  - coverage evidence
  - reviewers assigned
- Add or improve tests for `backoffice-bff`, `customer`, `rating`, and `tax`.
- Ensure `backoffice` frontend pipeline has install, lint, build, and optional audit steps.
- Maintain the evidence table for screenshots and open PR link.

Required outputs:

- screenshot of branch protection rule
- screenshot showing required reviewers and required checks
- one open PR link ready for submission
- completed evidence checklist

### Member 3: Security and quality owner

Cross-project responsibility:

- Gitleaks integration
- SonarQube or SonarCloud integration
- Snyk integration
- quality evidence screenshots

Owned buildable components:

- `product`
- `inventory`
- `recommendation`
- `common-library`
- `automation-ui`
- `sampledata`

Specific tasks:

- Add Gitleaks stage and make sure the team fork can run it successfully.
- Integrate SonarQube or SonarCloud for branch or PR analysis.
- Integrate Snyk dependency scanning.
- Add or improve tests for `product`, `inventory`, `recommendation`, `common-library`, `automation-ui`, and `sampledata`.
- For `common-library`, define how downstream services are revalidated when shared code changes.
- Work with Member 1 so scan results and test artifacts are visible from Jenkins or linked in PRs.

Required outputs:

- screenshot of successful Gitleaks run
- screenshot of Sonar quality result
- screenshot of Snyk result
- at least 3 service PRs with increased coverage

### Member 4: Monorepo routing and coverage gate owner

Cross-project responsibility:

- changed-service detection logic
- exception rules for shared paths
- coverage threshold enforcement above 70%
- JaCoCo policy

Owned buildable components:

- `order`
- `payment`
- `payment-paypal`
- `promotion`
- `search`
- `webhook`

Specific tasks:

- Design the change-detection matrix used by Jenkins:
  - if only `media/**` changes, run only `media`
  - if only `backoffice/**` changes, run only `backoffice`
  - if `common-library/**` or root `pom.xml` changes, run all affected backend services
  - if `.github`, `checkstyle`, `docker`, `deployment`, `k8s`, `identity`, `kafka`, `nginx`, `scripts`, or `tempo-data` changes, run shared validation path
- Implement the coverage gate so the pipeline fails when coverage is `<= 70%`.
- Add or improve tests for `order`, `payment`, `payment-paypal`, `promotion`, `search`, and `webhook`.
- Verify JaCoCo XML exists for all owned backend services.
- Pair with Member 1 to connect path detection and pipeline routing in Jenkins.

Required outputs:

- change-detection matrix documented in repo
- pipeline run proving service-specific trigger behavior
- coverage gate failure screenshot
- coverage gate success screenshot

## 5. Service Ownership Matrix

| Area | Owner | PR branch examples |
| --- | --- | --- |
| `cart` | Member 1 | `test/cart-coverage` |
| `media` | Member 1 | `test/media-coverage` |
| `location` | Member 1 | `test/location-coverage` |
| `delivery` | Member 1 | `test/delivery-coverage` |
| `storefront-bff` | Member 1 | `test/storefront-bff-coverage` |
| `storefront` | Member 1 | `test/storefront-ci-hardening` |
| `backoffice` | Member 2 | `test/backoffice-ci-hardening` |
| `backoffice-bff` | Member 2 | `test/backoffice-bff-coverage` |
| `customer` | Member 2 | `test/customer-coverage` |
| `rating` | Member 2 | `test/rating-coverage` |
| `tax` | Member 2 | `test/tax-coverage` |
| `product` | Member 3 | `test/product-coverage` |
| `inventory` | Member 3 | `test/inventory-coverage` |
| `recommendation` | Member 3 | `test/recommendation-coverage` |
| `common-library` | Member 3 | `test/common-library-coverage` |
| `automation-ui` | Member 3 | `test/automation-ui-coverage` |
| `sampledata` | Member 3 | `test/sampledata-coverage` |
| `order` | Member 4 | `test/order-coverage` |
| `payment` | Member 4 | `test/payment-coverage` |
| `payment-paypal` | Member 4 | `test/payment-paypal-coverage` |
| `promotion` | Member 4 | `test/promotion-coverage` |
| `search` | Member 4 | `test/search-coverage` |
| `webhook` | Member 4 | `test/webhook-coverage` |

## 6. Coordination Rules

### Branch and PR rules

- No direct work on `main`.
- One service improvement must stay in one service branch.
- Infra work must stay in dedicated infra branches.
- Every PR must include:
  - affected service or shared area
  - before and after coverage if relevant
  - Jenkins build URL or screenshot
  - assigned 2 reviewers

### Reviewer assignment

To satisfy the 2-approval rule and spread knowledge:

- Member 1 reviews all PRs that affect Jenkins behavior.
- Member 2 reviews all PRs for policy, naming, and submission readiness.
- Member 3 reviews Member 4's service PRs.
- Member 4 reviews Member 3's service PRs.
- Member 1 and Member 2 cross-review each other's infra PRs.
- Service PRs from Member 1 are reviewed by Members 2 and 4.
- Service PRs from Member 2 are reviewed by Members 1 and 3.

### Daily coordination checklist

Every daily sync should answer exactly these points:

1. Which branch is blocked and why?
2. Which owned services are still below 70% coverage?
3. Did Jenkins detect the correct changed path yesterday?
4. Which screenshot or evidence is still missing?
5. Which PRs need reviewers today?

### Integration order

Merge in this order to reduce rework:

1. `infra/jenkins-bootstrap`
2. `infra/github-governance`
3. `infra/change-detection`
4. quality integrations
5. service test branches
6. report and evidence branch

## 7. Detailed 3-Week Schedule

### Week 1: Platform foundation

Member 1:

- set up Jenkins
- configure multibranch pipeline
- connect GitHub webhook

Member 2:

- create team fork
- configure branch protection
- create PR template and CODEOWNERS

Member 3:

- prepare Sonar and Snyk accounts or tokens
- add first version of Gitleaks, Sonar, and Snyk stages

Member 4:

- design changed-path matrix
- define coverage gate logic
- test JaCoCo output structure

Team milestone:

- one service branch successfully discovered and built by Jenkins

### Week 2: Service coverage and selective CI

Member 1:

- finish owned service tests
- validate `storefront` and `storefront-bff` CI paths

Member 2:

- finish owned service tests
- confirm required checks block merge when failing

Member 3:

- finish owned service tests
- stabilize quality tool outputs

Member 4:

- finish owned service tests
- enforce coverage threshold in pipeline
- validate changed-service trigger with at least 3 scenarios

Team milestone:

- service-specific CI works for frontend, backend, and shared-path cases

### Week 3: Hardening and submission

Member 1:

- capture Jenkins screenshots
- fix flaky pipeline behavior

Member 2:

- prepare report evidence table
- verify at least one PR remains open for submission

Member 3:

- capture Gitleaks, Sonar, and Snyk screenshots
- fix remaining quality failures

Member 4:

- capture coverage gate screenshots
- verify all demo services are above 70%

Team milestone:

- all mandatory requirements satisfied
- advanced requirements demonstrated
- report artifacts complete

## 8. Final Submission Checklist

Before submission, the team must confirm all items below:

- team fork URL is ready
- `main` cannot be pushed directly
- PR requires 2 approvals
- PR requires passing Jenkins checks
- Jenkins scans branches automatically
- Jenkins pipeline includes `Test` and `Build`
- test results are uploaded
- coverage is reported
- changed service triggers only its own pipeline
- separate test branches exist for service test additions
- coverage gate above 70% is enforced
- Gitleaks evidence exists
- SonarQube or SonarCloud evidence exists
- Snyk evidence exists
- at least one PR is open in the repository
- screenshots for Jenkins, Gitleaks, Sonar, Snyk, and GitHub settings are collected

## 9. Key Assumptions

- This plan assumes Jenkins, Sonar, and Snyk credentials will be available to the team.
- GitHub branch protection and secrets are external settings, so they must be configured in the fork, not only documented in code.
- Shared folders such as `.github`, `common-library`, `docker`, `checkstyle`, and `k8s` are treated as multi-service impact areas.
- "Equal tasks" here means equal effort and accountability, not identical numbers of Java classes or files.
