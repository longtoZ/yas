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
        withCredentials([string(credentialsId: 'YAS-SonarQube-Key', variable: 'SONAR_TOKEN')]) {
          sh '''
            mvn -B -DskipTests sonar:sonar \
              -Dsonar.host.url=http://localhost:9000 \
              -Dsonar.projectKey="YAS Project" \
              -Dsonar.login=$SONAR_TOKEN
          '''
        }
      }
    }
  }
}