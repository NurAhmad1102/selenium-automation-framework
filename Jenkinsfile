pipeline {
  agent any

  parameters {
    choice(
      name: 'TESTNG_XML',
      choices: [
        'src/test/resources/testng/testng.xml',
        'src/test/resources/testng/testng_debug.xml',
        'src/test/resources/testng/testng_FO_debug.xml',
        'src/test/resources/testng/testng_fo_od_smoke.xml'
      ],
      description: 'Pilih file TestNG XML yang ingin dijalankan'
    )
    choice(
      name: 'BROWSER',
      choices: ['chrome', 'firefox', 'edge'],
      description: 'Pilih browser untuk pengujian'
    )
    booleanParam(
      name: 'HEADLESS',
      defaultValue: true,
      description: 'Jalankan pengujian dalam mode Headless (tanpa GUI)'
    )
  }

  tools {
    jdk 'JDK-17'
    maven 'apache-maven-3.9.15'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Test') {
      steps {
        bat """
          mvn -B -ntp clean test -DsuiteXmlFile=${params.TESTNG_XML} -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS}
          set EXIT_CODE=%ERRORLEVEL%
          taskkill /F /IM chromedriver.exe /T 2>nul
          taskkill /F /IM chrome.exe /T 2>nul
          exit %EXIT_CODE%
        """
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