pipeline {
  agent any

  options {
    disableConcurrentBuilds()
  }

  parameters {
    gitParameter(
      name: 'BRANCH',
      type: 'PT_BRANCH',
      defaultValue: 'development',
      branchFilter: 'origin/(.*)',
      sortMode: 'ASCENDING_SMART',
      selectedValue: 'TOP',
      description: 'Pilih branch Git yang ingin dieksekusi'
    )
    choice(
      name: 'TESTNG_XML',
      choices: [
        'sync-registry | [SYNC] Refresh Jenkins Dropdown UI Only (No Test Execution)'
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
    choice(
      name: 'RERUN_COUNT',
      choices: ['1', '0', '2'],
      description: 'Jumlah percobaan ulang otomatis untuk test yang gagal'
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

    stage('Init Workspace Env') {
      steps {
        script {
          env.SELENIUM_PROFILE_ROOT = "${pwd()}\\.selenium"
          echo "SELENIUM_PROFILE_ROOT=${env.SELENIUM_PROFILE_ROOT}"
        }
      }
    }

    stage('Pre-Cleanup') {
      steps {
        powershell '''
          $marker = "$env:SELENIUM_PROFILE_ROOT"
          if ([string]::IsNullOrWhiteSpace($marker)) {
            Write-Host "SELENIUM_PROFILE_ROOT is empty. Skipping targeted cleanup."
            exit 0
          }
          # Normalize path separators safely
          $marker = $marker.Replace('\\', '/')
          
          # Defensive guard against unsafe root paths
          if ($marker.Length -lt 10 -or -not ($marker -like "*/.selenium")) {
            Write-Host "Invalid or unsafe cleanup marker: $marker. Skipping targeted cleanup."
            exit 0
          }
          
          Write-Host "Target cleanup marker: $marker"
          $processFilter = "name='chrome.exe' or name='chromedriver.exe' or name='msedge.exe' or name='msedgedriver.exe' or name='firefox.exe' or name='geckodriver.exe'"
          
          # 1. Kill Leftover Processes
          $allProcesses = Get-CimInstance Win32_Process -Filter $processFilter
          
          $allProcesses |
            Where-Object {
              if ([string]::IsNullOrWhiteSpace($_.CommandLine)) {
                $false
              } else {
                $cmd = $_.CommandLine.Replace('\\', '/')
                $cmd.IndexOf($marker, [System.StringComparison]::OrdinalIgnoreCase) -ge 0
              }
            } |
            ForEach-Object {
              Write-Host "Stopping leftover process: $($_.Name) PID=$($_.ProcessId)"
              Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
            }
            
          # Refresh process list for directory safety check
          $activeProcesses = Get-CimInstance Win32_Process -Filter $processFilter
            
          # 2. Cleanup Old Build Folders (> 2 Days) - Safety Checked against active processes
          if (Test-Path "$env:SELENIUM_PROFILE_ROOT") {
            Get-ChildItem "$env:SELENIUM_PROFILE_ROOT" -Directory |
              Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-2) } |
              ForEach-Object {
                $folderMarker = $_.FullName.Replace('\\', '/')
                
                # Check if this folder is still referenced in any active command line
                $isActive = @($activeProcesses | Where-Object {
                  if ([string]::IsNullOrWhiteSpace($_.CommandLine)) {
                    $false
                  } else {
                    $cmd = $_.CommandLine.Replace('\\', '/')
                    $cmd.IndexOf($folderMarker, [System.StringComparison]::OrdinalIgnoreCase) -ge 0
                  }
                }).Count -gt 0
                
                if (-not $isActive) {
                  Write-Host "Removing old Selenium build folder: $($_.FullName)"
                  Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
                } else {
                  Write-Host "Skipping active Selenium build folder: $($_.FullName)"
                }
              }
          }
        '''
      }
    }

    stage('Sync & Resolve Suite Path') {
      steps {
        script {
          def rawSelected = (params.TESTNG_XML != null && !params.TESTNG_XML.toString().trim().isEmpty()) ? params.TESTNG_XML.toString().trim() : 'sync-registry'
          def selectedSuiteId = rawSelected.contains('|') ? rawSelected.split('\\|')[0].trim() : rawSelected
          def targetXml = selectedSuiteId
          def registryPath = 'src/test/resources/testng/suite-registry.json'

          if (fileExists(registryPath)) {
            try {
              def jsonText = readFile(file: registryPath)
              def slurper = new groovy.json.JsonSlurper()
              def registryData = slurper.parseText(jsonText)

              // Dynamic Self-Sync: Auto update Jenkins dropdown parameters while preserving Git Parameter BRANCH
              def allSuiteChoices = registryData.suites.collect { "${it.id} | ${it.name}" }
              if (allSuiteChoices && !allSuiteChoices.isEmpty()) {
                properties([
                  parameters([
                    gitParameter(
                      name: 'BRANCH',
                      type: 'PT_BRANCH',
                      defaultValue: 'development',
                      branchFilter: 'origin/(.*)',
                      sortMode: 'ASCENDING_SMART',
                      selectedValue: 'TOP',
                      description: 'Pilih branch Git yang ingin dieksekusi'
                    ),
                    choice(
                      name: 'TESTNG_XML',
                      choices: allSuiteChoices,
                      description: 'Pilih suite TestNG XML (Di-sync otomatis dari suite-registry.json)'
                    ),
                    choice(
                      name: 'BROWSER',
                      choices: ['chrome', 'firefox', 'edge'],
                      description: 'Pilih browser untuk pengujian'
                    ),
                    booleanParam(
                      name: 'HEADLESS',
                      defaultValue: true,
                      description: 'Jalankan pengujian dalam mode Headless (tanpa GUI)'
                    ),
                    choice(
                      name: 'RERUN_COUNT',
                      choices: ['1', '0', '2'],
                      description: 'Jumlah percobaan ulang otomatis untuk test yang gagal'
                    )
                  ])
                ])
              }

              // Handle sync-registry shortcut execution or initial null parameter run
              if (selectedSuiteId == 'sync-registry') {
                echo "=========================================================================="
                echo "[SYNC SUCCESS] Jenkins Dropdown UI parameter list has been updated!"
                echo "Future builds will present all latest suite options from suite-registry.json."
                echo "Skipping heavy test execution as requested."
                echo "=========================================================================="
                env.SKIP_TEST = 'true'
                return
              }

              def matchedSuite = registryData.suites.find { suite ->
                suite.id == selectedSuiteId || suite.path == selectedSuiteId
              }
              
              if (matchedSuite) {
                targetXml = matchedSuite.path
                echo "Resolved TestNG Suite from Registry [${matchedSuite.id}]: ${targetXml}"
              } else {
                echo "Using custom TestNG XML path: ${targetXml}"
              }
            } catch (Exception e) {
              echo "Warning: Could not parse ${registryPath}, using default path ${targetXml}. Error: ${e.message}"
            }
          }
          env.RESOLVED_SUITE_XML = targetXml
        }
      }
    }

    stage('Test') {
      when {
        expression { env.SKIP_TEST != 'true' }
      }
      steps {
        script {
          echo "=========================================================================="
          echo "=== [RUN 1] Starting Initial Test Execution: ${env.RESOLVED_SUITE_XML} ==="
          echo "=========================================================================="

          int testExitCode = bat(
            returnStatus: true,
            script: """
              call mvn -B -ntp clean test -DsuiteXmlFile=${env.RESOLVED_SUITE_XML} -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS}
            """
          )

          def failedXmlPath = 'target/surefire-reports/testng-failed.xml'
          int maxRerun = (params.RERUN_COUNT ?: '1') as int
          int rerunAttempt = 0
          boolean testPassed = (testExitCode == 0)

          while (!testPassed && rerunAttempt < maxRerun && fileExists(failedXmlPath)) {
            rerunAttempt++
            echo "=========================================================================="
            echo "=== [AUTO-RERUN ${rerunAttempt}/${maxRerun}] Executing Failed Tests Suite: ${failedXmlPath} ==="
            echo "=========================================================================="

            // Backup current Extent Report so initial suite results are preserved
            if (fileExists('reports/index.html')) {
              powershell """
                if (Test-Path 'reports') {
                  Copy-Item -Path 'reports' -Destination 'reports-run-${rerunAttempt}' -Recurse -Force
                }
              """
            }

            // Execute failed tests only (without clean to keep historical surefire reports)
            testExitCode = bat(
              returnStatus: true,
              script: """
                call mvn -B -ntp test -DsuiteXmlFile=${failedXmlPath} -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS}
              """
            )

            testPassed = (testExitCode == 0)
          }

          if (testPassed) {
            if (rerunAttempt > 0) {
              echo "=========================================================================="
              echo "[AUTO-RERUN SUCCESS] All failed tests PASSED after ${rerunAttempt} rerun attempt(s)."
              echo "=========================================================================="
            }
            currentBuild.result = 'SUCCESS'
          } else {
            echo "=========================================================================="
            echo "[TEST FAILED] Tests still failed after ${rerunAttempt} rerun attempt(s)."
            echo "=========================================================================="
            currentBuild.result = 'FAILURE'
          }
        }
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

      archiveArtifacts artifacts: 'reports/**,reports-run-*/**,logs/**,target/surefire-reports/**', allowEmptyArchive: true
    }
  }
}