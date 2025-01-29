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
    
    stage('Build images') {
      steps{
        script {
          devImage = docker.build( devRegistry, "-f ./docker/dev.dockerfile .")
          deplImage = docker.build( deplRegistry, "-f ./docker/depl.dockerfile .")
        }
      }
    }

    stage('Unit Tests and Code Coverage Test'){
      steps{
        script{
          catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
            sh 'sudo update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java'
            sh 'mvn clean test checkstyle:checkstyle pmd:pmd'
          }
        }
      }
      post{
        always {
          xunit (
            thresholds: [ skipped(failureThreshold: '0'), failed(failureThreshold: '0') ],
            tools: [ JUnit(pattern: 'target/surefire-reports/*.xml') ]
          )
          jacoco classPattern: 'target/classes', execPattern: 'target/jacoco.exec', sourcePattern: 'src/main/java', exclusionPattern:'**/*VertxEBProxy.class,**/Constants.class,**/*VertxProxyHandler.class,**/*Verticle.class,iudx/auditing/server/deploy/*.class,iudx/auditing/server/rabbitmq/RabbitMqService.class,iudx/auditing/server/querystrategy/AuditingServerStrategy.class,iudx/auditing/server/processor/MessageProcessService.class,iudx/auditing/server/postgres/PostgresService.class,iudx/auditing/server/immudb/ImmudbService.class,iudx/auditing/server/common/RabitMqConsumer.class,iudx/auditing/server/cache/*.class,iudx/auditing/server/rabbitmq/consumers/*.class,iudx/auditing/server/rabbitmq/RabbitMqServiceImpl.class'
          recordIssues(
            enabledForFailure: true,
            skipBlames: true,
            qualityGates: [[threshold:0, type: 'TOTAL', unstable: false]],
            tool: checkStyle(pattern: 'target/checkstyle-result.xml')
          )
          recordIssues(
            enabledForFailure: true,
            skipBlames: true,
            qualityGates: [[threshold:0, type: 'TOTAL', unstable: false]],
            tool: pmdParser(pattern: 'target/pmd.xml')
          )
        }
        failure{
          script{
            sh 'docker compose -f docker-compose.test.yml down --remove-orphans'
          }
          error "Test failure. Stopping pipeline execution!"
        }
        cleanup{
          script{
            sh 'sudo update-alternatives --set java /usr/lib/jvm/java-11-openjdk-amd64/bin/java'
            sh 'sudo rm -rf target/'
          }
        }        
      }
    }

    stage('Continuous Deployment') {
      when {
        allOf {
          anyOf {
            changeset "docker/**"
            changeset "docs/**"
            changeset "pom.xml"
            changeset "src/main/**"
            triggeredBy cause: 'UserIdCause'
          }
          expression {
            return env.GIT_BRANCH == 'origin/main';
          }
        }
      }
      stages {
        stage('Push Images') {
          steps {
            script {
              docker.withRegistry( registryUri, registryCredential ) {
                devImage.push("5.6.0-alpha-${env.GIT_HASH}")
                deplImage.push("5.6.0-alpha-${env.GIT_HASH}")
              }
            }
          }
        }
        stage('Docker Swarm deployment') {
          steps {
            script {
              sh "ssh azureuser@docker-swarm 'docker service update auditing_auditing --image ghcr.io/datakaveri/auditing-server-depl:5.6.0-alpha-${env.GIT_HASH}'"
              sh 'sleep 10'
            }
          }
          post{
            failure{
              error "Failed to deploy image in Docker Swarm"
            }
          }          
        }
      }
    }
  }
  post{
    failure{
      script{
        if (env.GIT_BRANCH == 'origin/main')
        emailext recipientProviders: [buildUser(), developers()], to: '$AS_RECIPIENTS, $DEFAULT_RECIPIENTS', subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!', body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:
Check console output at $BUILD_URL to view the results.'''
      }
    }
  }
}
