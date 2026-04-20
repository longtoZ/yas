pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 1, unit: 'HOURS')
        disableConcurrentBuilds()
    }

    environment {
        // Member 1 owned components only.
        MEMBER1_BACKEND_SERVICES = "cart media location delivery storefront-bff"
        MEMBER1_FRONTEND_SERVICES = "storefront"
        MEMBER1_ALL_SERVICES = "cart media location delivery storefront-bff storefront"
        SMOKE_SERVICE = "cart"
        CHANGED_SERVICES = ""
        CHANGED_BACKEND_SERVICES = ""
        CHANGED_FRONTEND_SERVICES = ""
        TEST_FAILURES = "0"
        // Required for Testcontainers in a Docker-in-Docker setup to route to the host gateway.
        TESTCONTAINERS_HOST_OVERRIDE = ""
    }

    stages {

        // ============ CHECKOUT ============
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ============ INITIALIZE ============
        stage('Initialize') {
            steps {
                script {
                    env.GIT_COMMIT_MSG = sh(
                        script: "git log -1 --pretty=%B",
                        returnStdout: true
                    ).trim()

                    env.BUILD_TIMESTAMP = sh(
                        script: "date '+%Y%m%d_%H%M%S'",
                        returnStdout: true
                    ).trim()

                    // Dynamically retrieve the Docker host gateway IP for Testcontainers routing.
                    env.TESTCONTAINERS_HOST_OVERRIDE = sh(
                        script: 'ip route | awk \'/default/ { print $3 }\'',
                        returnStdout: true
                    ).trim()

                    echo """
========================================
  YAS Project - Member 1 CI Started
========================================

Branch: ${env.BRANCH_NAME}
Commit: ${env.GIT_COMMIT}
Message: ${env.GIT_COMMIT_MSG}
Build: #${env.BUILD_NUMBER}
Time: ${env.BUILD_TIMESTAMP}
Member 1 backend services: ${env.MEMBER1_BACKEND_SERVICES}
Member 1 frontend services: ${env.MEMBER1_FRONTEND_SERVICES}
Smoke service for Jenkinsfile/shared changes: ${env.SMOKE_SERVICE}
"""
                }
            }
        }

        // ============ DETECT CHANGES ============
        stage('Detect Changed Services') {
            steps {
                script {
                    sh '''
                        echo "Detecting Member 1 changed services..."

                        BACKEND_SERVICES="$MEMBER1_BACKEND_SERVICES"
                        FRONTEND_SERVICES="$MEMBER1_FRONTEND_SERVICES"
                        ALL_MEMBER1_SERVICES="$MEMBER1_ALL_SERVICES"

                        is_in_list() {
                            ITEM="$1"
                            LIST="$2"
                            for VALUE in $LIST; do
                                if [ "$VALUE" = "$ITEM" ]; then
                                    return 0
                                fi
                            done
                            return 1
                        }

                        append_unique() {
                            ITEM="$1"
                            LIST="$2"
                            if [ -z "$LIST" ]; then
                                echo "$ITEM"
                                return
                            fi
                            if is_in_list "$ITEM" "$LIST"; then
                                echo "$LIST"
                            else
                                echo "$LIST $ITEM"
                            fi
                        }

                        if [ "$BRANCH_NAME" = "main" ]; then
                            CHANGED_BACKEND_SERVICES="$BACKEND_SERVICES"
                            CHANGED_FRONTEND_SERVICES="$FRONTEND_SERVICES"
                            CHANGE_REASON="main-member1-validation"
                        else
                            git fetch origin main || true

                            CHANGED_FILES="$(git diff --name-only origin/main...HEAD --diff-filter=ACMRT || true)"
                            CHANGED_BACKEND_SERVICES=""
                            CHANGED_FRONTEND_SERVICES=""
                            HAS_MEMBER1_CHANGE="false"
                            HAS_SHARED_CHANGE="false"
                            HAS_OTHER_CHANGE="false"
                            CHANGE_REASON="no-member1-owned-change"

                            if [ -n "$CHANGED_FILES" ]; then
                                while IFS= read -r FILE; do
                                    [ -z "$FILE" ] && continue
                                    ROOT="${FILE%%/*}"

                                    if is_in_list "$ROOT" "$BACKEND_SERVICES"; then
                                        CHANGED_BACKEND_SERVICES="$(append_unique "$ROOT" "$CHANGED_BACKEND_SERVICES")"
                                        HAS_MEMBER1_CHANGE="true"
                                        CHANGE_REASON="member1-backend-change"
                                        continue
                                    fi

                                    if is_in_list "$ROOT" "$FRONTEND_SERVICES"; then
                                        CHANGED_FRONTEND_SERVICES="$(append_unique "$ROOT" "$CHANGED_FRONTEND_SERVICES")"
                                        HAS_MEMBER1_CHANGE="true"
                                        CHANGE_REASON="member1-frontend-change"
                                        continue
                                    fi

                                    # Jenkinsfile/shared changes belong to Member 1's Jenkins platform work.
                                    if [ "$FILE" = "Jenkinsfile" ] || [ "$FILE" = "pom.xml" ] || echo "$FILE" | grep -q "^\\.github/"; then
                                        HAS_SHARED_CHANGE="true"
                                        CHANGE_REASON="member1-jenkins-smoke"
                                        continue
                                    fi

                                    case "$ROOT" in
                                        checkstyle|deployment|docker|docs|identity|k8s|kafka|nginx|scripts|tempo-data)
                                            HAS_SHARED_CHANGE="true"
                                            CHANGE_REASON="member1-shared-smoke"
                                            ;;
                                        *)
                                            HAS_OTHER_CHANGE="true"
                                            ;;
                                    esac
                                done <<EOF
$CHANGED_FILES
EOF
                            fi

                            # For Jenkinsfile/shared-only branches, run cart so Build/Test are real evidence.
                            if [ "$HAS_MEMBER1_CHANGE" = "false" ] && [ "$HAS_SHARED_CHANGE" = "true" ]; then
                                CHANGED_BACKEND_SERVICES="$SMOKE_SERVICE"
                            fi

                            if [ "$HAS_MEMBER1_CHANGE" = "false" ] && [ "$HAS_SHARED_CHANGE" = "false" ] && [ "$HAS_OTHER_CHANGE" = "true" ]; then
                                CHANGE_REASON="other-member-change-skipped"
                            fi
                        fi

                        CHANGED_SERVICES="$(echo "$CHANGED_BACKEND_SERVICES $CHANGED_FRONTEND_SERVICES" | xargs || true)"

                        if [ -z "$CHANGED_SERVICES" ]; then
                            echo "CHANGED_SERVICES=none" > build.properties
                        else
                            echo "CHANGED_SERVICES=$CHANGED_SERVICES" > build.properties
                        fi
                        echo "CHANGED_BACKEND_SERVICES=$CHANGED_BACKEND_SERVICES" >> build.properties
                        echo "CHANGED_FRONTEND_SERVICES=$CHANGED_FRONTEND_SERVICES" >> build.properties
                        echo "CHANGE_REASON=$CHANGE_REASON" >> build.properties

                        echo "Changed files:"
                        if [ -n "${CHANGED_FILES:-}" ]; then
                            echo "$CHANGED_FILES"
                        else
                            echo "(none listed)"
                        fi
                        echo ""
                        cat build.properties
                    '''

                    env.CHANGED_SERVICES = sh(
                        script: "grep '^CHANGED_SERVICES=' build.properties | cut -d'=' -f2-",
                        returnStdout: true
                    ).trim()

                    env.CHANGED_BACKEND_SERVICES = sh(
                        script: "grep '^CHANGED_BACKEND_SERVICES=' build.properties | cut -d'=' -f2-",
                        returnStdout: true
                    ).trim()

                    env.CHANGED_FRONTEND_SERVICES = sh(
                        script: "grep '^CHANGED_FRONTEND_SERVICES=' build.properties | cut -d'=' -f2-",
                        returnStdout: true
                    ).trim()

                    env.CHANGE_REASON = sh(
                        script: "grep '^CHANGE_REASON=' build.properties | cut -d'=' -f2-",
                        returnStdout: true
                    ).trim()

                    currentBuild.description = "services=${env.CHANGED_SERVICES ?: 'none'} reason=${env.CHANGE_REASON ?: 'unknown'}"
                    echo "Changed services: ${env.CHANGED_SERVICES}"
                    echo "Change reason: ${env.CHANGE_REASON}"
                }
            }
        }

        // ============ BUILD ============
        stage('Build Services') {
            when {
                expression { (env.CHANGED_SERVICES ?: 'none') != 'none' }
            }
            steps {
                script {
                    sh '''
                        echo "Building Member 1 services..."

                        if [ -n "$CHANGED_BACKEND_SERVICES" ]; then
                            if [ -f "cart/mvnw" ]; then
                                echo "Building backend services: $CHANGED_BACKEND_SERVICES"
                                chmod +x cart/mvnw
                                cd cart && ./mvnw -f ../pom.xml clean install -pl "$CHANGED_BACKEND_SERVICES" -am -DskipTests
                            else
                                echo "ERROR: Maven wrapper not found in cart/ folder"
                                exit 1
                            fi
                        fi

                        if [ -n "$CHANGED_FRONTEND_SERVICES" ]; then
                            if command -v npm >/dev/null 2>&1; then
                                for SERVICE in $CHANGED_FRONTEND_SERVICES; do
                                    echo "Building frontend service: $SERVICE"
                                    cd "$WORKSPACE/$SERVICE" && npm ci && npm run build
                                done
                            else
                                echo "ERROR: npm not found. Install Node.js/npm on the Jenkins node to build storefront."
                                exit 1
                            fi
                        fi
                    '''
                }
            }
        }

        // ============ TEST ============
        stage('Run Tests') {
            when {
                expression { (env.CHANGED_SERVICES ?: 'none') != 'none' }
            }
            steps {
                script {
                    sh '''
                        echo "Running Member 1 tests..."

                        TEST_FAILURES=0

                        if [ -n "$CHANGED_BACKEND_SERVICES" ]; then
                            if [ -f "cart/mvnw" ]; then
                                echo "Testing backend services: $CHANGED_BACKEND_SERVICES"
                                chmod +x cart/mvnw
                                set +e
                                (cd cart && ./mvnw -f ../pom.xml verify -pl "$CHANGED_BACKEND_SERVICES" -am -fae -DfailIfNoTests=false)
                                RC=$?
                                set -e
                                if [ $RC -ne 0 ]; then
                                    TEST_FAILURES=1
                                    echo "Backend tests failed for one or more Member 1 services, continuing..."
                                fi
                            else
                                echo "ERROR: Maven wrapper not found in cart/ folder"
                                exit 1
                            fi
                        fi

                        if [ -n "$CHANGED_FRONTEND_SERVICES" ]; then
                            if command -v npm >/dev/null 2>&1; then
                                for SERVICE in $CHANGED_FRONTEND_SERVICES; do
                                    echo "Linting frontend service: $SERVICE"
                                    set +e
                                    (cd "$WORKSPACE/$SERVICE" && npm ci && npm run lint)
                                    RC=$?
                                    set -e
                                    if [ $RC -ne 0 ]; then
                                        TEST_FAILURES=1
                                        echo "Frontend lint failed for $SERVICE, continuing..."
                                    fi
                                done
                            else
                                echo "ERROR: npm not found. Install Node.js/npm on the Jenkins node to test storefront."
                                TEST_FAILURES=1
                            fi
                        fi

                        echo "TEST_FAILURES=$TEST_FAILURES" > test-status.properties
                    '''

                    env.TEST_FAILURES = sh(
                        script: "grep '^TEST_FAILURES=' test-status.properties | cut -d'=' -f2-",
                        returnStdout: true
                    ).trim()

                    if (env.TEST_FAILURES == '1') {
                        currentBuild.result = 'UNSTABLE'
                        echo 'One or more Member 1 test executions failed. Continuing to collect reports and run coverage checks.'
                    }
                }
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        // ============ COVERAGE CHECK ============
        stage('Check Coverage') {
            when {
                expression { (env.CHANGED_BACKEND_SERVICES ?: '').trim() != '' }
            }
            steps {
                script {
                    echo "Checking Member 1 backend coverage..."

                    sh '''
                        TOTAL_COVERED=0
                        TOTAL_MISSED=0

                        for SERVICE in $CHANGED_BACKEND_SERVICES; do
                            REPORT="$SERVICE/target/site/jacoco/jacoco.xml"
                            if [ -f "$REPORT" ]; then
                                COVERAGE_VALUES=$(python3 - "$REPORT" <<'PY'
import sys
import xml.etree.ElementTree as ET

report = sys.argv[1]
covered = 0
missed = 0

tree = ET.parse(report)
for counter in tree.iter('counter'):
    if counter.get('type') == 'LINE':
        covered += int(counter.get('covered', '0'))
        missed += int(counter.get('missed', '0'))

print(f"{covered} {missed}")
PY
)
                                COVERED=$(printf '%s\n' "$COVERAGE_VALUES" | awk '{print $1}')
                                MISSED=$(printf '%s\n' "$COVERAGE_VALUES" | awk '{print $2}')

                                TOTAL_COVERED=$((TOTAL_COVERED + COVERED))
                                TOTAL_MISSED=$((TOTAL_MISSED + MISSED))
                                echo "Service $SERVICE: $COVERED covered, $MISSED missed"
                            else
                                echo "No JaCoCo report found for $SERVICE at $REPORT"
                            fi
                        done

                        if [ $((TOTAL_COVERED + TOTAL_MISSED)) -eq 0 ]; then
                            echo "No coverage data found for selected Member 1 backend services."
                            exit 0
                        fi

                        TOTAL=$((TOTAL_COVERED + TOTAL_MISSED))
                        PERCENTAGE=$((TOTAL_COVERED * 100 / TOTAL))

                        echo "===================================="
                        echo "Member 1 Aggregate Coverage: $PERCENTAGE%"
                        echo "===================================="
                    '''
                }
            }
        }

        // ============ REPORT ============
        stage('Publish Reports') {
            when {
                expression { (env.CHANGED_SERVICES ?: 'none') != 'none' }
            }
            steps {
                script {
                    def services = (env.CHANGED_BACKEND_SERVICES ?: '').trim()
                        ? (env.CHANGED_BACKEND_SERVICES ?: '').trim().split(' ')
                        : []

                    services.collect { it.trim() }.findAll { it }.each { service ->
                        def reportDir = "${service}/target/site/jacoco"
                        if (fileExists("${reportDir}/index.html")) {
                            publishHTML([
                                reportDir: reportDir,
                                reportFiles: 'index.html',
                                reportName: "Coverage Report - ${service}",
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true
                            ])
                        }
                    }
                }

                archiveArtifacts artifacts: '**/target/*.jar, **/target/site/jacoco/**, build.properties, test-status.properties', allowEmptyArchive: true
            }
        }

        // ============ FINAL TEST GATE ============
        stage('Enforce Test Gate') {
            when {
                expression { (env.CHANGED_SERVICES ?: 'none') != 'none' }
            }
            steps {
                script {
                    if (env.TEST_FAILURES == '1') {
                        error('Test gate failed: one or more selected Member 1 services have failing tests or lint checks.')
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'build.properties, test-status.properties, **/target/surefire-reports/**, **/target/failsafe-reports/**, **/target/site/jacoco/**', allowEmptyArchive: true
            cleanWs()
        }

        success {
            echo "Pipeline SUCCESS"
        }

        unstable {
            echo "Pipeline UNSTABLE"
        }

        failure {
            echo "Pipeline FAILED"
        }
    }
}