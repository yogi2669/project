pipeline {
    agent any

    environment {
        // Define environment variables for Nexus credentials and repository details
        NEXUS_URL = 'http://13.233.88.239:8081'  // Nexus URL
        NEXUS_REPO = 'my-maven-releases'         // Nexus repository name
        ARTIFACT_PATH = 'target/myapp.jar'       // Path to the artifact in your project
        GITHUB_REPO = 'https://github.com/JaiBhargav/project'  // GitHub repo URL
        BRANCH = 'master'                       // GitHub branch to build
        DEPLOYMENT_FILE_PATH = 'deployment.yaml'  // Path to your deployment.yaml file
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout the code from GitHub
                git url: GITHUB_REPO, branch: BRANCH
            }
        }

        stage('Build') {
            steps {
                // Run Maven to build the application
                script {
                    sh 'mvn clean install -DskipTests'
                }
            }
        }

        stage('Publish Artifact to Nexus') {
            steps {
                // Push the artifact to Nexus
                withCredentials([usernamePassword(credentialsId: 'maven-creds', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    script {
                        // Use Maven deploy command to push the JAR file to Nexus
                        sh """
                            mvn deploy:deploy-file \
                            -Dfile=${ARTIFACT_PATH} \
                            -DrepositoryId=nexus-releases \
                            -Durl=${NEXUS_URL}/repository/${NEXUS_REPO}/ \
                            -DgroupId=com.example \
                            -DartifactId=backend-app \
                            -Dversion=1.0.0 \
                            -Dpackaging=jar \
                            -Dusername=$NEXUS_USERNAME \
                            -Dpassword=$NEXUS_PASSWORD
                        """
                    }
                }
            }
        }

        stage('Update Deployment YAML') {
            steps {
                // Update deployment.yaml (e.g., you can replace the version or image name dynamically)
                script {
                    def version = "1.0.0"  // Specify the version or fetch from the artifact
                    def imageName = "bhargavjupalli/backend-app:${version}"
                    sh """
                        sed -i 's|image:.*|image: ${imageName}|' ${DEPLOYMENT_FILE_PATH}
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                // Deploy to Kubernetes (assuming ArgoCD is watching the deployment file)
                script {
                    // Update the Kubernetes cluster with the modified deployment.yaml file
                    kubernetesDeploy(
                        kubeconfigId: 'my-kubeconfig',
                        configs: "${DEPLOYMENT_FILE_PATH}",
                        enableConfigSubstitution: true
                    )
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully!"
        }

        failure {
            echo "Pipeline failed. Please check the logs for errors."
        }
    }
}
