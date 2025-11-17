pipeline {
    agent { label 'Main' }

    tools {
      maven 'maven3.9'
      jdk 'JDK 17'
    }

    stages {
        stage('Build and Test') {
            steps {
                script {
                    pom = readMavenPom file: 'pom.xml'
                    env.ARTIFACT_ID = pom.artifactId
                    echo "Starting Build and Test for ${env.ARTIFACT_ID}..."
                    withMaven(mavenSettingsConfig: 'acbda4ab-411f-45e7-aeb2-0c2541d292d0') {
                        sh "mvn clean verify"
                    }
                }
            }
            post {
                success {
                    echo "...Build and Test Succeeded for ${currentBuild.number}: ${currentBuild.currentResult}"
                }
                unsuccessful {
                    echo "...Build and Test Failed for ${currentBuild.number}: ${currentBuild.currentResult}"
                }
            }
        }
        stage('Snyk Scan') {
                steps {
                    snykSecurity(
                        failOnError: false,
                        failOnIssues: false,
                        organisation: 'mulesoft-PvGk6krUaynVbx5LEDiDat',
                        snykInstallation: 'snyk@latest',
                        snykTokenId: 'Snyk_API'
                    )
                }
        }
        stage('Deploy Artifact') {
            steps {
                script {
                    echo "Starting Deploy Artifact with id ${env.ARTIFACT_ID}..."

                    withMaven(mavenSettingsConfig: 'acbda4ab-411f-45e7-aeb2-0c2541d292d0') {
                        sh "mvn -U --batch-mode clean deploy"
                    }

                    echo "Artifact Deployed: ${currentBuild.currentResult}"
                }
            }
            post {
                success {
                    echo "...Deploy Artifact Succeeded for ${currentBuild.number}: ${currentBuild.currentResult}"
                }
                unsuccessful {
                    echo "...Deploy Artifact Failed for ${currentBuild.number}: ${currentBuild.currentResult}"
                }
            }
        }
    }
}