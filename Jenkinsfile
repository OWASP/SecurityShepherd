pipeline {
  agent none
  stages {
    stage('SecurityScan') {
      agent {
        docker {
          image 'kondukto/kondukto-cli:dev'
        }

      }
      steps {
        sh 'kdt --help'
      }
    }

    stage('Build') {
      agent {
        docker {
          image 'maven:3-alpine'
        }

      }
      steps {
        sh 'mvn package'
      }
    }

  }
}