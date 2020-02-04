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
        sh 'mvn package -DskipTests'
        sh 'mkdir deploy'
        sh 'cp /var/lib/jenkins/workspace/SecurityShepherd_master/target/owaspSecurityShepherd.war ../deploy/secshape.war'
      }
    }
    
        stage('Deploy') {
      agent {
        docker {
          image 'tomcat:8.5.50-jdk11-openjdk'
          args '-v /var/lib/jenkins/workspace/SecurityShepherd_master/deploy:/usr/local/tomcat/webapps -p 9999:8080'
        }

      }
      steps {
        sh 'pwd'
      }
    }

  }
}
