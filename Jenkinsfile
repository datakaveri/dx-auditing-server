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
    stage('Trivy Code Scan (Dependencies)') {
      steps {
        script {
          sh '''
            trivy fs --scanners vuln,secret,misconfig --output trivy-fs-report.txt .
          '''
        }
      }
    }
    
    stage('Build images') {
      steps{
        script {
          devImage = docker.build( devRegistry, "-f ./docker/dev.dockerfile .")
          deplImage = docker.build( deplRegistry, "-f ./docker/depl.dockerfile .")
        }
      }
    }
    stage('Trivy Docker Image Scan') {
      steps {
        script {
          sh "trivy image --output trivy-dev-image-report.txt ${devImage.imageName()}"
          sh "trivy image --output trivy-depl-image-report.txt ${deplImage.imageName()}"
        }
      }
    }
    stage('Archive Trivy Reports') {
      steps {
        archiveArtifacts artifacts: 'trivy-*.txt', allowEmptyArchive: true
        publishHTML(target: [
          allowMissing: true,
          keepAll: true,
          reportDir: '.',
          reportFiles: 'trivy-fs-report.txt, trivy-dev-image-report.txt, trivy-depl-image-report.txt',
          reportName: 'Trivy Reports'
        ])
      }
      post {
        success {
          echo "Trivy scan and Docker build completed successfully."
        }
        failure {
          echo "Trivy scan or Docker build failed!"
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

    stage('Push Images') {
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
          return env.GIT_BRANCH == 'origin/5.6.0';
        }
      }
  	}
      steps {
        script {
          docker.withRegistry( registryUri, registryCredential ) {
            devImage.push("5.6.0-${env.GIT_HASH}")
            deplImage.push("5.6.0-${env.GIT_HASH}")
          }
        }
      }
    }

  }
  post{
    failure{
      script{
        if (env.GIT_BRANCH == 'origin/5.6.0')
        emailext recipientProviders: [buildUser(), developers()], to: '$AS_RECIPIENTS, $DEFAULT_RECIPIENTS', subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!', body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:
Check console output at $BUILD_URL to view the results.'''
      }
    }
  }
}