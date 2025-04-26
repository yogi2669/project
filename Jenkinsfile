pipeline {
    agent any

    environment {
        NEXUS_URL = 'http://13.233.88.239:8081'   // Nexus URL
        NEXUS_REPO = 'my-maven-releases'          // Nexus repository name
        ARTIFACT_PATH = 'target/backend-0.0.1-SNAPSHOT.jar' // Relative to backend directory
        GITHUB_REPO = 'https://github.com/JaiBhargav/project'  // GitHub repo URL
        BRANCH = 'master'                         // GitHub branch
        DEPLOYMENT_FILE_PATH = 'deployment.yaml'  // Path to your deployment.yaml
        BACKEND_DIR = 'backend'                   // Backend application folder
    }

    stages {
        stage('Checkout') {
            steps {
                git url: "${GITHUB_REPO}", branch: "${BRANCH}"
            }
        }

        stage('Build') {
            steps {
                dir("${BACKEND_DIR}") {
                    sh 'mvn clean install -DskipTests'
                }
            }
        }

        stage('Publish Artifact to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'maven-cred', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    dir("${BACKEND_DIR}") {
                        sh """
                            mvn deploy:deploy-file \
                            -Dfile=${ARTIFACT_PATH} \
                            -DrepositoryId=nexus-releases \
                            -Durl=${NEXUS_URL}/repository/${NEXUS_REPO}/ \
                            -DgroupId=com.example \
                            -DartifactId=backend-app \
                            -Dversion=1.0.0 \
                            -Dpackaging=jar \
                            -DgeneratePom=true \
                            -Dusername=$NEXUS_USERNAME \
                            -Dpassword=$NEXUS_PASSWORD
                        """
                    }
                }
            }
        }

        stage('Update Deployment YAML') {
            steps {
                dir("${BACKEND_DIR}") {
                    script {
                        def version = "1.0.0"
                        def imageName = "bhargavjupalli/backend-app:${version}"
                        sh """
                            sed -i 's|image:.*|image: ${imageName}|' ${DEPLOYMENT_FILE_PATH}
                        """
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                dir("${BACKEND_DIR}") {
                    script {
                        kubernetesDeploy(
                            kubeconfigId: 'my-kubeconfig',
                            configs: "${DEPLOYMENT_FILE_PATH}",
                            enableConfigSubstitution: true
                        )
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completed successfully!"
        }

        failure {
            echo "❌ Pipeline failed. Please check the logs for more details."
        }
    }
}
