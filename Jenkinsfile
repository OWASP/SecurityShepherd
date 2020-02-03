pipeline {
  agent {
    docker {
      image 'maven:3-alpine'
    }

  }
  stages {
    stage('pre-build') {
      steps {
        sh 'uname -a && pwd && ls -lah'
      }
    }

    stage('') {
      steps {
        sh 'mvn -Pdocker clean install -DskipTests'
      }
    }

  }
}