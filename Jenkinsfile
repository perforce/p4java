pipeline {

    agent none

    stages {

        stage('Compile') {
            agent {
                label 'p4java'
            }
            steps {
                sh './gradlew clean assemble'

            }
        }

        stage('Verification') {
            parallel {

                stage('ubuntu') {
                    agent {
                        label 'p4java'
                    }
                    stages {
                        stage('Test') {
                            steps {
                                lock("eng-p4java-vm_lock") {
                                    sh './gradlew clean build'

                                }
                            }
                        }
                    }
                    post {
                        always {
                            report('UbuntuTestReport')

                        }
                    }
                }

                stage('win') {
                    agent {
                        label 'p4java-win'
                    }
                    stages {
                        stage('Test') {
                            steps {
                                lock("eng-p4java-vm_lock") {
                                    bat label: '', script: 'gradlew clean build'

                                }
                            }
                        }
                    }
                    post {
                        always {
                            report('WindowsTestReport')
                        }
                    }
                }
            }
        }
        
        stage('Launch system tests') {
            steps {
                build job: '/p4java-system-tests/main', wait: false
            }
        }
    }
}


void report(String name) {
    publishHTML target: [
            allowMissing         : false,
            alwaysLinkToLastBuild: true,
            keepAll              : true,
            reportDir            : 'build/reports/tests/test/',
            reportFiles          : 'index.html',
            reportName           : name
    ]
}
