def call(Map config = [:]) {
    config.nodeLabel = config.nodeLabel ?: 'production'
    config.mavenVersion = config.mavenVersion ?: 'Maven-3.5.3'
    config.jdkVersion = config.jdkVersion ?: 'JDK-1.8'
    config.mavenAdditionalPROpts = config.mavenAdditionalPROpts ?: ''
    config.mavenAdditionalMainlineOpts = config.mavenAdditionalMainlineOpts ?: ''
    config.mavenAdditionalTagOpts = config.mavenAdditionalTagOpts ?: ''
    config.checkoutSubmodules = config.checkoutSubmodules ?: false
    config.trackingSubmodules = config.trackingSubmodules ?: true
    config.integrationTestBranch = config.integrationTestBranch ?: ''

    pipeline {
        options {
            buildDiscarder(logRotator(numToKeepStr: '5'))
          	disableConcurrentBuilds()
            timestamps()
            timeout(time: 1, unit: 'HOURS')
        }
        agent {
            node {
                label config.nodeLabel
            }
        }
        environment {
            NODE_LABEL = "${config.nodeLabel}"
            _JAVA_OPTIONS = "-Djava.io.tmp=$WORKSPACE/.tmp"
            MAVEN_OPTS = '-Xmx2G -XX:MaxPermSize=256M'
            MAVEN_OPTIONS = "--batch-mode --errors --fail-at-end -Dsurefire.useFile=false -Dexecutor.number=$EXECUTOR_NUMBER -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
            JDK_VERSION = "${config.jdkVersion}"
            MAVEN_VERSION = "${config.mavenVersion}"
        }
        tools {
            maven config.mavenVersion
            jdk config.jdkVersion
        }
        triggers {
            snapshotDependencies()
        }
        stages {
            stage("prepare") {
                steps {
                    cleanWs()
                  	checkout scm
                }
            }
            stage('build-pull-request') {
                when {
                    changeRequest()
                }
                steps {
                    echo 'Building for change request'
                  	wrap([$class: 'Xvfb', autoDisplayName: true, timeout: 15]) {
                      withMaven(maven: config.mavenVersion, mavenLocalRepo: "$WORKSPACE/.repository", options: [artifactsPublisher(disabled: true)]) {
                          script {
                              sh "mvn $env.MAVEN_OPTIONS -Ppull-request,ci-full,hsqldb-mem -Dmaven.javadoc.skip=true -Dexecutor.number=$EXECUTOR_NUMBER ${config.mavenAdditionalPROpts} verify"
                          }
                      }
                    }
                }
            }
            stage('build-mainline') {
                when {
                    anyOf {
                        branch 'rambus'
                        branch 'develop'
                        branch 'develop-s3h3'
                        branch 'master'
                        branch 'release/*'
                        branch 'maintenance/*'
                        branch 'feature/vaultify-shop-whitelabel'
                        branch '1.*'
                        branch '6.*'
                    }
                }
                steps {
                    echo 'Building mainline branch'
                  	wrap([$class: 'Xvfb', autoDisplayName: true, timeout: 15]) {
                      withMaven(maven: config.mavenVersion, mavenLocalRepo: "$WORKSPACE/.repository", options: [artifactsPublisher(disabled: true)]) {
                          script {
                              sh "mvn $env.MAVEN_OPTIONS -Pci-full,docker,boot -Dmaven.javadoc.skip=true -DskipTests ${config.mavenAdditionalMainlineOpts} deploy"
                          }
                      }
                    }
                    fingerprint '**/target/*.jar'
                }
            }

            stage('build-tag') {
                when {
                    buildingTag()
                }
                steps {
                    echo 'Building from a tag'
                  	wrap([$class: 'Xvfb', autoDisplayName: true, timeout: 15]) {
                    // can use shared maven repository
                      withMaven(maven: config.mavenVersion, mavenLocalRepo: "$WORKSPACE/.repository", options: [artifactsPublisher(disabled: true)]) {
                          script {
                              sh "mvn $env.MAVEN_OPTIONS -Pci-full,docker,boot -DskipTests -DdistributionRepository=releases ${config.mavenAdditionalTagOpts} deploy"
                          }
                      }
                    }
                }
            }

            stage('Configure And Run CI/CD') {
                when {
                    allOf {
                        anyOf {
                            branch 'develop'
                            branch 'develop-s3h3'
                            branch 'release/*'
                            branch 'maintenance/*'
                            branch 'mergeback/*'
                        }
                        expression{config.integrationTestBranch.length() != 0}
                    }
                }
                steps {
                    script {
                        echo "Triggering  CreateOrUpdate-Project-CICD-Pipeline with JOB_NAME ${env.JOB_NAME} Integration_Branch ${config.integrationTestBranch}"
                        def jobResult = build job: "/Projects/CreateOrUpdate-Project-CICD-Pipeline", propagate: true, parameters: [
                                [$class: 'StringParameterValue', name: 'branchName', value: env.BRANCH_NAME],
                                [$class: 'StringParameterValue', name: 'systemTestBranchName', value: config.integrationTestBranch],
                                [$class: 'StringParameterValue', name: 'jobName', value: env.JOB_NAME],
                                [$class: 'StringParameterValue', name: 'selectProject', value: 'SKIP']
                        ]

                        build job: jobResult.getBuildVariables().CI_JOB_NAME, propagate: false, wait: false, parameters: [
                                [$class: 'StringParameterValue', name: 'skipMavenBuild', value: 'true']
                        ]

                    }
                }
            }
        
        }//stages
        post {
            always {
                cleanWs()
            }
        }
    }//pipeline
}//call