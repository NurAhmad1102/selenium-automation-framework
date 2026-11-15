# CI/CD Pipeline Integration Guide

This guide describes how to integrate the test automation framework into continuous integration pipelines (GitHub Actions, Jenkins, GitLab CI).

---

## 1. Running Headless in CI

The framework natively supports headless Chrome, Edge, and Firefox. Configure headless mode via:

1. **System Property**: `-Dheadless=true`
2. **Environment Variable**: `HEADLESS=true`
3. **Configuration File**: `headless=true` in `application.properties`

```bash
mvn test -Dheadless=true -Dbrowser=chrome
```

---

## 2. GitHub Actions Workflow

Create `.github/workflows/ci.yml` in your repository root:

```yaml
name: Automation Framework CI

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'

      - name: Install Google Chrome
        run: |
          sudo apt-get update
          sudo apt-get install -y wget curl unzip libxi6 libgconf-2-4 libnss3
          wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
          sudo sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list'
          sudo apt-get update
          sudo apt-get install -y google-chrome-stable

      - name: Compile & Run Unit Tests
        run: mvn clean test -pl framework-core

      - name: Run Sample E2E Suite
        run: mvn test -pl framework-sample -Dheadless=true

      - name: Archive Test Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: extent-and-surefire-reports
          path: |
            reports/
            framework-sample/target/surefire-reports/
```

---

## 3. Generic Jenkinsfile Pipeline

For self-hosted Jenkins agents:

```groovy
pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    environment {
        BROWSER_HEADLESS = 'true'
        BROWSER = 'chrome'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn clean test-compile'
            }
        }

        stage('Core Tests & Architecture Guard') {
            steps {
                sh 'mvn test -pl framework-core'
            }
        }

        stage('Sample E2E Execution') {
            steps {
                sh 'mvn test -pl framework-sample -Dheadless=true'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'reports/**/*.html,**/target/surefire-reports/**/*', allowEmptyArchive: true
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
    }
}
```

---

## 4. Test Reruns & Flakiness Handling

To automatically retry flaky tests in CI, Surefire's `rerunFailingTestsCount` can be specified:

```bash
mvn test -DrerunFailingTestsCount=2
```
