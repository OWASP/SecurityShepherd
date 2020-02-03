pipeline {
    agent none
    stages {
        stage('SecurityScan') {
            steps {
                sh 'docker -ti run kondukto/kondukto-cli --help'
            }
        }
        stage('Build') {
            agent {
                docker { image 'maven:3-alpine' }
            }
            steps {
                sh 'mvn -Pdocker clean install -DskipTests'
            }
        }
    }
}
