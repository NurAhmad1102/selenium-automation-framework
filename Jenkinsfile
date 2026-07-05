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

    stage('Test') {
      steps {
        script {
          int testExitCode = bat(
            returnStatus: true,
            script: """
              call mvn -B -ntp clean test -DsuiteXmlFile=${params.TESTNG_XML} -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS}
            """
          )

          if (testExitCode != 0) {
            currentBuild.result = 'UNSTABLE'
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

      archiveArtifacts artifacts: 'reports/**,logs/**,target/surefire-reports/**', allowEmptyArchive: true
    }
  }
}