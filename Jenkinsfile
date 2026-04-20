pipeline {
  agent any

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Gitleaks') {
      steps {
        sh '''
          docker run --rm \
            -v "$PWD:/repo" \
            zricethezav/gitleaks:latest detect \
            --source=/repo \
            --config=/repo/gitleaks.toml \
            --no-git \
            --report-format=json \
            --report-path=/repo/gitleaks-report.json
        '''
      }
      post {
        always {
          archiveArtifacts artifacts: 'gitleaks-report.json', allowEmptyArchive: true
        }
      }
    }

    stage('SonarQube') {
      steps {
        withCredentials([string(credentialsId: 'YAS-SonarQube-Token', variable: 'SONAR_TOKEN')]) {
          sh '''
            docker run --rm \
              -v "$PWD:/workspace" -w /workspace \
              -v maven-repo:/root/.m2 \
              maven:3.9.14-eclipse-temurin-25 \
              mvn -B -DskipTests clean verify sonar:sonar \
                -Dsonar.host.url=http://host.docker.internal:9000 \
                -Dsonar.projectKey=YAS-Project \
                -Dsonar.login=$SONAR_TOKEN
          '''
        }
      }
    }

    stage('Snyk Security Scan') {
      steps {
        withCredentials([string(credentialsId: 'YAS-Snyk-Token', variable: 'SNYK_TOKEN')]) {
          sh '''
            docker run --rm \
              -e SNYK_TOKEN=${SNYK_TOKEN} \
              -v "$PWD:/workspace" -w /workspace \
              snyk/snyk:maven snyk test \
                --severity-threshold=high \
                --json-file-output=snyk-report.json
            '''
        }
      }
    }
  }
}