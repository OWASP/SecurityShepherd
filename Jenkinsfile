pipeline {
  agent any
  stages {
     stage('Prepare Environment') {
      steps {
        sh 'cd ..'
        sh 'ls'
        sh 'rm -rf SecurityShepherd_master*'
      }
    }
    stage('SecurityScan - SAST') {
      agent {
        docker {
          image 'kondukto/kondukto-cli:dev'
          args '-e KONDUKTO_HOST=http://192.168.1.38:8088 -e KONDUKTO_TOKEN=U1U2dlA4SmhFN1BaTFc3ZkRhVVVBTzNEakNtQlBNV3cweHlsaDB2Z284N0ROOURxRE9iUmJ1WlFRT0Jk'
        }

      }
      steps {
        sh 'kdt scan -p SecuritySheppard -t findsecbugs'
      }
    }

    stage('SecurityScan - SCA') {
      agent {
        docker {
          image 'kondukto/kondukto-cli:dev'
          args '-e KONDUKTO_HOST=http://192.168.1.38:8088 -e KONDUKTO_TOKEN=U1U2dlA4SmhFN1BaTFc3ZkRhVVVBTzNEakNtQlBNV3cweHlsaDB2Z284N0ROOURxRE9iUmJ1WlFRT0Jk'
        }

      }
      steps {
        sh 'kdt scan -p SecuritySheppard -t dependencycheck'
      }
    }
    
    stage('Build') {
      agent {
        docker {
          image 'maven:3-alpine'
        }

      }
      steps {
        sh 'mvn package -DskipTests'
        
      }
    }
    
        stage('Deploy') {
                steps {
                sh 'sudo cp /var/lib/jenkins/workspace/SecurityShepherd_master@2/target/owaspSecurityShepherd.war /var/lib/jenkins/workspace/deploy/secshape.war'
                sh 'docker rm sechape --force || true' 
                sh 'docker run --name secshape -d -v /var/lib/jenkins/workspace/deploy:/usr/local/tomcat/webapps -p 9999:8080 tomcat:8.5'
      }
           
            

    }

  }
}
