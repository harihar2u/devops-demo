def call(Map config = [:]) {
    config.nodeLabel = config.nodeLabel ?: 'helm'

    pipeline {
        options {
            buildDiscarder(logRotator(numToKeepStr: '5'))
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
        }
        stages {
            stage("prepare") {
                steps {
                    cleanWs()
                    checkout([$class                           : 'GitSCM',
                              branches                         : scm.branches,
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[
                                                                          $class             : 'SubmoduleOption',
                                                                          disableSubmodules  : true,
                                                                          parentCredentials  : true,
                                                                          recursiveSubmodules: false,
                                                                          reference          : '',
                                                                          trackingSubmodules : false
                                                                  ]],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : scm.userRemoteConfigs])
                }
            }
            stage('build-pull-request') {
                when {
                    changeRequest()
                }
                steps {
                    echo 'Building for PR'

                    script {
                        echo 'Checking for template generation..'
                        sh "helm template --namespace default ."
                        echo 'Checking for if chart version exists..'
                        def chart = readYaml file: 'Chart.yaml'
                        httpRequest url: "https://nr-helm.rmbspay.net/api/charts/${chart.name}/${chart.version}", validResponseCodes: '404', consoleLogResponseBody: true, ignoreSslErrors: true, timeout: 5
                    }
                }
            }
            stage('build-mainline') {
                when {
                    anyOf {
                        branch 'develop'
                        branch 'master'
                        branch 'release/*'
                        branch 'maintenance/*'
                    }
                }
                steps {
                    echo 'Building mainline branch'

                    withCredentials([usernamePassword(credentialsId: 'helm_repository', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                        script {
                            echo 'Checking for template generation..'
                            sh "helm template --namespace default ."
                            def chart = readYaml file: 'Chart.yaml'
                            def response = httpRequest url: "https://nr-helm.rmbspay.net/api/charts/${chart.name}/${chart.version}", validResponseCodes: '100:499', consoleLogResponseBody: true, ignoreSslErrors: true, timeout: 5
                            if (200 == response.getStatus()) {
                                echo 'Chart with this version already exists at the repository so skip pushing..'
                            } else {
                                echo 'Pushing chart into the Helm repository..'
                                sh "helm push -u \"${USERNAME}\" -p \"${PASSWORD}\" . https://nr-helm.rmbspay.net"
                            }
                        }
                    }
                }
            }
            stage('build-tag') {
                when {
                    buildingTag()
                }
                steps {
                    echo 'Building from a tag'

                    withCredentials([usernamePassword(credentialsId: 'helm_repository', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                        script {
                            echo 'Checking for template generation..'
                            sh "helm template --namespace default ."
                            def chart = readYaml file: 'Chart.yaml'
                            def response = httpRequest url: "https://nr-helm.rmbspay.net/api/charts/${chart.name}/${chart.version}", validResponseCodes: '100:499', consoleLogResponseBody: true, ignoreSslErrors: true, timeout: 5
                            if (200 == response.getStatus()) {
                                echo 'Chart with this version already exists at the repository so skip pushing..'
                            } else {
                                echo 'Pushing chart into the Helm repository..'
                                sh "helm push -u \"${USERNAME}\" -p \"${PASSWORD}\" . https://nr-helm.rmbspay.net"
                            }
                        }
                    }
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