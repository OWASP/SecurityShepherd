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
        
      }
    }
    
        stage('Deploy') {
                steps {
                sh 'sudo cp /var/lib/jenkins/workspace/SecurityShepherd_master@2/target/owaspSecurityShepherd.war /var/lib/jenkins/workspace/deploy/secshape.war'
                sh 'docker run -d -v /var/lib/jenkins/workspace/deploy:/usr/local/tomcat/webapps -p 9999:8080 tomcat:8.5'
      }
           
            

    }

  }
}
