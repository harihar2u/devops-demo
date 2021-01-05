def call() { 
    pipeline {
        options {
            buildDiscarder(logRotator(numToKeepStr: '3'))
        }
        triggers {
            snapshotDependencies()
        }
        agent {
            node {
                label 'docker'
            }
        }
        tools {
            maven 'Maven-3.6.1'
            jdk 'JDK-1.8'
        }
        stages {
            stage('build') {
                steps {
                    cleanWs()

                    checkout scm

                    withMaven(maven: 'Maven-3.6.1', mavenLocalRepo: "$WORKSPACE/.repository", mavenSettingsConfig: '84695cd2-9e5f-4ecd-b5e8-14e0e7292776', options: [artifactsPublisher(disabled: true)]) {
                      sh "mvn --settings ${MVN_SETTINGS} deploy"
                    }
                    
                    fingerprint '**/target/*.jar'
                }
            }
        }
        post {
            always {
                cleanWs()
            }
        }
    }
}