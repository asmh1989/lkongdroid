pipeline {
  agent any
  stages {
    stage('android-lint') {
      steps {
        echo 'start android lint ...'
        sh './gradlew check'
      }
    }
    stage('build') {
      steps {
        echo 'start build ...'
        sh './gradlew assembleRelease'
      }
    }

  }
  post {
    success {
      echo 'post success ...'
    }
    failure {
      echo 'post failure ...'
      mail to: 'sunmh@justsy.com', subject: 'The Pipeline failed :('
    }
    unstable {
      echo 'post unstable ...'
    }
    aborted {
      echo 'post aborted ...'
    }
  }

}