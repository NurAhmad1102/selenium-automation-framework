pipeline {
  agent any

  tools {
    jdk 'jdk-17'
    maven 'maven-3.9'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Test') {
      steps {
        bat 'mvn -B -ntp clean test -DsuiteXmlFile=src/test/resources/testng/testng.xml -Dbrowser=chrome'
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml,target/surefire-reports/junitreports/TEST-*.xml'

      publishHTML(target: [
        reportName: 'Extent Report',
        reportDir: 'reports',
        reportFiles: 'index.html',
        keepAll: true,
        alwaysLinkToLastBuild: true,
        allowMissing: true
      ])

      archiveArtifacts artifacts: 'reports/**,logs/**,target/surefire-reports/**', allowEmptyArchive: true
    }
  }
}