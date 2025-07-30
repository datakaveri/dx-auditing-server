pipeline {
  environment {
    devRegistry = 'ghcr.io/datakaveri/auditing-server-dev'
    deplRegistry = 'ghcr.io/datakaveri/auditing-server-depl'
    registryUri = 'https://ghcr.io'
    registryCredential = 'datakaveri-ghcr'
    GIT_HASH = GIT_COMMIT.take(7)
  }

  agent {
    node {
      label 'slave1'
    }
  }

  stages {
    stage('Conditional Execution') {
      when {
        expression {
          def causes = currentBuild.rawBuild.getCauses()
          def userTriggered = causes.any {
            it.toString().toLowerCase().contains('user')
          }
          def comment = env.ghprbCommentBody
          def isPRComment = comment && comment != "null" && !comment.trim().isEmpty()
          return userTriggered || isPRComment || isImportantChange()
        }
      }
      stages {
        stage('Trivy Code Scan (Dependencies)') {
          steps {
            sh 'trivy fs --scanners vuln,secret,misconfig --output trivy-fs-report.txt .'
          }
        }

        stage('Build images') {
          steps {
            script {
              devImage = docker.build(devRegistry, "-f ./docker/dev.dockerfile .")
              deplImage = docker.build(deplRegistry, "-f ./docker/depl.dockerfile .")
            }
          }
        }

        stage('Unit Tests and Code Coverage Test') {
          steps {
            script {
              sh 'sudo update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java'
              sh 'cp /home/ubuntu/configs/audit-config-test.json ./secrets/all-verticles-configs/audit-config-test.json'
              sh 'cp /home/ubuntu/configs/keystore-auditing.jks ./secrets/all-verticles-configs/keystore-auditing.jks'
              sh 'mvn clean test checkstyle:checkstyle pmd:pmd'
            }
          }
          post {
            always {
              xunit (
                thresholds: [ skipped(failureThreshold: '0'), failed(failureThreshold: '0') ],
                tools: [ JUnit(pattern: 'target/surefire-reports/*.xml') ]
              )
              jacoco classPattern: 'target/classes', execPattern: 'target/jacoco.exec', sourcePattern: 'src/main/java', exclusionPattern:'**/*VertxEBProxy.class,...'
              recordIssues(enabledForFailure: true, tool: checkStyle(pattern: 'target/checkstyle-result.xml'))
              recordIssues(enabledForFailure: true, tool: pmdParser(pattern: 'target/pmd.xml'))
            }
            failure {
              sh 'docker compose -f docker-compose.test.yml down --remove-orphans'
              error "Test failure. Stopping pipeline execution!"
            }
            cleanup {
              sh 'sudo update-alternatives --set java /usr/lib/jvm/java-11-openjdk-amd64/bin/java'
              sh 'sudo rm -rf target/'
            }
          }
        }

        stage('Continuous Deployment') {
          when {
            expression { env.GIT_BRANCH == 'origin/master' }
          }
          stages {
            stage('Push Images') {
              steps {
                script {
                  docker.withRegistry(registryUri, registryCredential) {
                    devImage.push("6.0.0-alpha-${env.GIT_HASH}")
                    deplImage.push("6.0.0-alpha-${env.GIT_HASH}")
                  }
                }
              }
            }

            stage('Docker Swarm deployment') {
              steps {
                sh "ssh azureuser@docker-swarm 'docker service update auditing_auditing --image ghcr.io/datakaveri/auditing-server-depl:6.0.0-alpha-${env.GIT_HASH}'"
                sh 'sleep 10'
              }
              post {
                failure {
                  error "Failed to deploy image in Docker Swarm"
                }
              }
            }
          }
        }
      }
    }
  }

  post {
    failure {
      script {
        if (env.GIT_BRANCH == 'origin/main') {
          emailext recipientProviders: [buildUser(), developers()], to: '$AS_RECIPIENTS, $DEFAULT_RECIPIENTS',
            subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!',
            body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:
Check console output at $BUILD_URL to view the results.'''
        }
      }
    }
  }
}

def isImportantChange() {
  def paths = ['docker/', 'docs/', 'pom.xml', 'src/main/']
  return currentBuild.changeSets.any { cs ->
    cs.items.any { item ->
      item.affectedPaths.any { path ->
        paths.any { imp -> path.startsWith(imp) || path == imp }
      }
    }
  }
}
