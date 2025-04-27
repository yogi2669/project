pipeline {
    agent any

    environment {
        NEXUS_URL = 'http://13.127.197.254:8081' // Nexus URL
        NEXUS_REPO = 'my-maven-releases'         // Nexus repository name
        ARTIFACT_PATH = 'target/backend-0.0.1-SNAPSHOT.jar' // Path to the artifact
        GITHUB_REPO = 'https://github.com/JaiBhargav/project' // GitHub repo
        BRANCH = 'master'                        // GitHub branch
        DEPLOYMENT_FILE_PATH = 'manifests/deployment.yml' // Kubernetes deployment YAML
        BACKEND_DIR = 'backend'                  // Backend code directory
        VERSION = '1.0.0'                        // Application version
    }

    stages {
        stage('Checkout') {
            steps {
                git url: GITHUB_REPO, branch: BRANCH
            }
        }

        stage('Build') {
            steps {
                dir("${BACKEND_DIR}") {
                    sh 'mvn clean install -DskipTests'
                }
            }
        }

        stage('Create Maven Settings.xml') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'maven-cred', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    script {
                        def settingsXmlContent = """
                            <settings>
                              <servers>
                                <server>
                                  <id>nexus-releases</id>
                                  <username>${env.NEXUS_USERNAME}</username>
                                  <password>${env.NEXUS_PASSWORD}</password>
                                </server>
                              </servers>
                            </settings>
                        """
                        writeFile file: "${env.HOME}/.m2/settings.xml", text: settingsXmlContent
                    }
                }
            }
        }

        stage('Publish Artifact to Nexus') {
            steps {
                dir("${BACKEND_DIR}") {
                    sh """
                        mvn deploy:deploy-file \
                        -Dfile=${ARTIFACT_PATH} \
                        -DrepositoryId=nexus-releases \
                        -Durl=${NEXUS_URL}/repository/${NEXUS_REPO}/ \
                        -DgroupId=com.aitechie \
                        -DartifactId=backend \
                        -Dversion=${VERSION} \
                        -Dpackaging=jar
                    """
                }
            }
        }

        stage('Update Deployment YAML') {
            steps {
                script {
                    def imageName = "bhargavjupalli/backend-app:${VERSION}"
                    sh """
                        sed -i 's|image:.*|image: ${imageName}|' ${DEPLOYMENT_FILE_PATH}
                    """
                }
            }
        }

        stage('Push Updated Deployment YAML to GitHub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-cred', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                    sh """
                        git config user.name "${GIT_USERNAME}"
                        git config user.email "${GIT_USERNAME}@example.com"
                        git add ${DEPLOYMENT_FILE_PATH}
                        git commit -m "Update deployment image to backend-app:${VERSION}" || echo "No changes to commit"
                        git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/JaiBhargav/project.git HEAD:${BRANCH}
                    """
                }
            }
        }

        stage('Deploy to Kubernetes via ArgoCD') {
            steps {
                echo "Waiting for ArgoCD to detect changes and deploy automatically... 🚀"
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed. Please check logs."
        }
    }
}
