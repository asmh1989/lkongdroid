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
      mail (to: 'sunmh@justsy.com',
         subject: "Job '${env.JOB_NAME}' (${env.BUILD_NUMBER}) is pipeline failed",
         body: "Please go to ${env.BUILD_URL}.")
    }
    unstable {
      echo 'post unstable ...'
    }
    aborted {
      echo 'post aborted ...'
    }
  }

}