def runCommand(String unixCommand, String windowsCommand = unixCommand) {
    if (isUnix()) {
        sh unixCommand
    } else {
        bat windowsCommand
    }
}

def runCommandOutput(String unixCommand, String windowsCommand = unixCommand) {
    if (isUnix()) {
        return sh(script: unixCommand, returnStdout: true).trim()
    }

    return bat(script: windowsCommand, returnStdout: true).trim()
}

String sanitizeServiceName(String value, String fallback = 'cart') {
    String trimmed = value?.trim()
    if (!trimmed || trimmed == 'null') {
        return fallback
    }

    return trimmed
}

pipeline {
    agent any

    options {
        timestamps()
        skipDefaultCheckout(true)
    }

    environment {
        DEFAULT_SERVICE = 'cart'
        SELECTED_SERVICE = ''
        SELECTED_TYPE = ''
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Change') {
            steps {
                script {
                    def serviceTypes = [
                        cart          : 'backend',
                        media         : 'backend',
                        location      : 'backend',
                        delivery      : 'backend',
                        'storefront-bff': 'backend',
                        storefront    : 'frontend'
                    ]
                    def sharedRoots = [
                        '.github',
                        'checkstyle',
                        'deployment',
                        'docker',
                        'docs',
                        'identity',
                        'k8s',
                        'kafka',
                        'nginx',
                        'scripts',
                        'tempo-data'
                    ] as Set

                    String defaultService = sanitizeServiceName(env.DEFAULT_SERVICE, 'cart')
                    String changedOutput = runCommandOutput(
                        'git diff --name-only HEAD~1 HEAD || git ls-files',
                        '@echo off\r\ngit diff --name-only HEAD~1 HEAD || git ls-files'
                    )

                    List<String> changedFiles = changedOutput ? changedOutput.readLines().findAll { it?.trim() } : []
                    String detectedService = defaultService

                    if (changedFiles) {
                        for (String filePath : changedFiles) {
                            String root = filePath.tokenize('/\\\\') ? filePath.tokenize('/\\\\')[0] : filePath

                            if (serviceTypes.containsKey(root)) {
                                detectedService = root
                                break
                            }

                            if (sharedRoots.contains(root) || ['pom.xml', 'Jenkinsfile'].contains(filePath)) {
                                detectedService = defaultService
                            }
                        }
                    }

                    env.SELECTED_SERVICE = sanitizeServiceName(detectedService, defaultService)
                    env.SELECTED_TYPE = serviceTypes[env.SELECTED_SERVICE] ?: 'backend'
                    currentBuild.description = "service=${env.SELECTED_SERVICE}"

                    echo "Changed files:\n${changedFiles ? changedFiles.join('\n') : '(no changed files detected)'}"
                    echo "Selected service: ${env.SELECTED_SERVICE} (${env.SELECTED_TYPE})"
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    String selectedService = sanitizeServiceName(env.SELECTED_SERVICE, sanitizeServiceName(env.DEFAULT_SERVICE, 'cart'))
                    String selectedType = env.SELECTED_TYPE ?: 'backend'

                    if (selectedType == 'frontend') {
                        runCommand(
                            "cd ${selectedService} && npm ci && npm run lint && npx prettier --check .",
                            "@echo off\r\ncd /d ${selectedService}\r\nnpm ci\r\nnpm run lint\r\nnpx prettier --check ."
                        )
                    } else {
                        runCommand(
                            "mvn -B -ntp clean verify -pl ${selectedService} -am",
                            "@echo off\r\nmvn -B -ntp clean verify -pl ${selectedService} -am"
                        )
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST*.xml, **/target/failsafe-reports/TEST*.xml'
                }
            }
        }

        stage('Coverage') {
            steps {
                script {
                    String selectedService = sanitizeServiceName(env.SELECTED_SERVICE, sanitizeServiceName(env.DEFAULT_SERVICE, 'cart'))
                    String selectedType = env.SELECTED_TYPE ?: 'backend'

                    if (selectedType != 'backend') {
                        echo "Coverage publication is skipped for frontend bootstrap runs."
                        return
                    }

                    String jacocoReport = "${selectedService}/target/site/jacoco/jacoco.xml"
                    if (!fileExists(jacocoReport)) {
                        echo "No JaCoCo report found at ${jacocoReport}."
                        return
                    }

                    recordCoverage(
                        tools: [[parser: 'JACOCO', pattern: jacocoReport]],
                        id: 'jacoco',
                        name: 'JaCoCo Coverage'
                    )
                    archiveArtifacts artifacts: jacocoReport, allowEmptyArchive: true
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    String selectedService = sanitizeServiceName(env.SELECTED_SERVICE, sanitizeServiceName(env.DEFAULT_SERVICE, 'cart'))
                    String selectedType = env.SELECTED_TYPE ?: 'backend'

                    if (selectedType == 'frontend') {
                        runCommand(
                            "cd ${selectedService} && npm run build",
                            "@echo off\r\ncd /d ${selectedService}\r\nnpm run build"
                        )
                    } else {
                        runCommand(
                            "mvn -B -ntp -DskipTests install -pl ${selectedService} -am",
                            "@echo off\r\nmvn -B -ntp -DskipTests install -pl ${selectedService} -am"
                        )
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
    }
}
