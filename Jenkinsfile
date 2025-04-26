pipeline {
    agent any

    environment {
        // Define environment variables for Nexus credentials and repository details
        NEXUS_URL = 'http://13.233.88.239:8081'  // Nexus URL
        NEXUS_REPO = 'my-maven-releases'         // Nexus repository name
        ARTIFACT_PATH = 'target/myapp.jar'       // Path to the artifact in your project
        GITHUB_REPO = 'https://github.com/JaiBhargav/project'  // GitHub repo URL
        BRANCH = 'master'                       // GitHub branch to build
        DEPLOYMENT_FILE_PATH = 'deployment.yml'  // Path to your deployment.yaml file
        BACKEND_DIR = 'backend'  // Directory where the backend application exists
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
                // Change to the 'backend' directory and run Maven to build the application
                dir("${BACKEND_DIR}") {
                    // Run Maven to build the application
                    script {
                        sh 'mvn clean install -DskipTests'
                    }
                }
            }
        }

        stage('Upload Artifact to Nexus') {
            steps {
                dir('backend') {    // again inside backend
                    withCredentials([usernamePassword(credentialsId: "${maven-cred}", usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                        sh '''
                        mvn deploy:deploy-file \
                          -DgroupId=com.aitechie \
                          -DartifactId=backend \
                          -Dversion=0.0.1-SNAPSHOT \
                          -Dpackaging=jar \
                          -Dfile=target/myapp.jar \
                          -DrepositoryId=nexus \
                          -Durl=$NEXUS_URL \
                          -DgeneratePom=true \
                          -DrepositoryLayout=default \
                          -Dusername=$NEXUS_USER \
                          -Dpassword=$NEXUS_PASS
                        '''
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
