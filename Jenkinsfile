pipeline {
  agent none
  stages {
    stage('SecurityScan') {
      agent {
        docker {
          image 'kondukto/kondukto-cli'
        }

      }
      steps {
        sh '--help'
      }
    }

    stage('Build') {
      agent {
        docker {
          image 'maven:3-alpine'
        }

      }
      steps {
        sh 'mvn -Pdocker clean install -DskipTests'
      }
    }

  }
}
