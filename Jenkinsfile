pipeline {
    agent any

    environment {
        NEXUS_URL = 'http://13.127.197.254:8081' // Nexus URL (still there for Maven JAR push)
        NEXUS_REPO_JAR = 'my-maven-releases'     // Nexus repository for JAR artifacts
        ARTIFACT_PATH = 'target/backend-0.0.1-SNAPSHOT.jar' // Path to the artifact
        GITHUB_REPO = 'https://github.com/JaiBhargav/project' // GitHub repo
        BRANCH = 'master'                        // GitHub branch
        DEPLOYMENT_FILE_PATH = 'manifests/deployment.yml' // Kubernetes deployment YAML
        BACKEND_DIR = 'backend'                  // Backend code directory
        VERSION = '1.0.0'                        // Application version
        DOCKER_IMAGE_NAME = 'backend-app'        // Docker image name
        DOCKER_HUB_USER = 'bhargavjupalli' // 🔥 New: DockerHub username (replace this)
    }

    stages {
        stage('Checkout') {
            steps {
                git url: GITHUB_REPO, branch: BRANCH
            }
        }

        stage('Build Maven App') {
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

        stage('Publish Artifact to Nexus (JAR)') {
            steps {
                dir("${BACKEND_DIR}") {
                    sh """
                        mvn deploy:deploy-file \
                        -Dfile=${ARTIFACT_PATH} \
                        -DrepositoryId=nexus-releases \
                        -Durl=${NEXUS_URL}/repository/${NEXUS_REPO_JAR}/ \
                        -DgroupId=com.aitechie \
                        -DartifactId=backend \
                        -Dversion=${VERSION} \
                        -Dpackaging=jar
                    """
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir("${BACKEND_DIR}") {
                    script {
                        sh """
                            docker build -t ${DOCKER_HUB_USER}/${DOCKER_IMAGE_NAME}:${VERSION} .
                        """
                    }
                }
            }
        }

        stage('Login to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-cred', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    script {
                        sh """
                            echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
                        """
                    }
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    sh """
                        docker push ${DOCKER_HUB_USER}/${DOCKER_IMAGE_NAME}:${VERSION}
                    """
                }
            }
        }

        stage('Update Deployment YAML') {
            steps {
                script {
                    def imageName = "${DOCKER_HUB_USER}/${DOCKER_IMAGE_NAME}:${VERSION}"
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
                        git commit -m "Update deployment image to ${DOCKER_IMAGE_NAME}:${VERSION}" || echo "No changes to commit"
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
