pipeline {
  agent any
  stages {
    stage('android-lint') {
      steps {
        sh '''echo "start android-lint"

./gradlew check'''
      }
    }
  }
}