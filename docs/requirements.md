# Project 01: CI System Implementation

## I. Description

In this course, students are required to build a **CI/CD pipeline and monitoring system** to deploy, operate, and monitor the system:

👉 https://github.com/nashtech-garage/yas

**YAS (Yet Another Shop)** is a personal project aimed at practicing how to build a typical **microservices application** using Java.

### Technologies and Frameworks

- Java 21  
- Spring Boot 3.2  
- Testcontainers  
- Next.js  
- Keycloak  
- Kafka  
- Elasticsearch  
- Kubernetes (K8s)  
- GitHub Actions  
- SonarCloud  
- OpenTelemetry  
- Grafana, Loki, Prometheus, Tempo  

---

## II. Requirements

This is the first project in the DevOps course series.  
In this project, students are required to use **Jenkins** to build a **CI (Continuous Integration) pipeline** with the following requirements:

### 1.
Students may use:
- GitHub Actions  
- GitLab CI/CD  
- Jenkins  

### 2.
Fork a new repository from:  
👉 https://github.com/nashtech-garage/yas  
for your team.

### 3.
Configure GitHub to:
- Disallow direct push to the `main` branch  
- Each Pull Request must:
  - Have at least **2 reviewers approving**
  - Pass CI  
- Only then can it be merged into the `main` branch  

### 4.
Configure Jenkins so that it can:
- Scan repositories  
- Run pipelines for each branch  

### 5.
The pipeline must include at least **2 phases**:
- Test  
- Build  

The **Test phase must**:
- Upload test results  
- Report test coverage  

### 6.
Since the system uses a **monorepo model**, you must configure CI (GitHub Actions, GitLab CI/CD, or Jenkins) so that:

- The pipeline is triggered **only for the specific service** that has changes  

**Example:**
- If a developer modifies `Media-service`  
  → Only build and test `Media-service`  
  → Do NOT build and test the entire system  

---

## Advanced Requirements

### a.
Add unit tests to increase test coverage.

- Create separate branches to add test cases  
- Each branch corresponds to a service:
  - Media  
  - Product  
  - Cart  
  - etc.

### b.
Adjust the pipeline so that it only passes when:
- Test coverage **> 70%**

### c.
Use the following tools to scan for:
- Security vulnerabilities  
- Code quality  

Tools:
- Gitleaks  
- SonarQube  
- Snyk  

---

## III. Regulations

### 1.
This is a **group project of 4 students**

### 2.
Duration: **3 weeks**  
(Start date: March 17, 2026)

### 3. Submission

Students must submit a report file including:

#### a.
Link to the team’s GitHub repository  
- The repository must contain at least **one open Pull Request**

#### b.
Screenshots of:
- Jenkins jobs  
- Gitleaks  
- SonarQube  
- Snyk  
- Other configurations  

#### c.
File naming format:

`MSSV1_MSSV2_MSSV3.docx`

- Student IDs must be sorted in ascending order  

**Examples:**
- 23120000_23120001_23120002.docx  
- 23120000_23120001.docx  
- 23120000.docx  