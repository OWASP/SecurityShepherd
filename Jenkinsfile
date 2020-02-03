pipeline {
  agent any
  stages {
    stage('SecScan') {
      steps {
        sh 'docker run kondukto/kondukto-cli --help'
      }
    }

    stage('Build') {
      steps {
        sh 'docker run -v $HOME/.m2:/root/.m2 maven:3-alpine mvn -P clean install -DskipTest'
      }
    }

  }
}