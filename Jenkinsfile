pipeline {
  agent none
  stages {
    stage('SecScan') {
      steps {
        sh 'docker -ti run kondukto/kondukto-cli --help'
      }
    }

    stage('Build') {
      steps {
        sh 'docker run -ti -v $HOME/.m2:/root/.m2 maven:3-alpine mvn -Pdocker clean install -DskipTest'
      }
    }

  }
}