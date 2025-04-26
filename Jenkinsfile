pipeline {
    agent any

    environment {
        BACKEND_IMAGE = 'your-nexus-repo/backend:latest'
        FRONTEND_IMAGE = 'your-nexus-repo/frontend:latest'
        DOCKER_CREDENTIAL_ID = 'your-docker-credentials-id' // set this in Jenkins credentials
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/yourusername/ci-cd-project.git'
            }
        }

        stage('Build Backend') {
            steps {
                sh 'cd backend && mvn clean package'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    docker.build("${BACKEND_IMAGE}", "backend/")
                    docker.build("${FRONTEND_IMAGE}", "frontend/")
                }
            }
        }

        stage('Push Images to Nexus') {
            steps {
                script {
                    docker.withRegistry('http://your-nexus-url:8082', "${DOCKER_CREDENTIAL_ID}") {
                        docker.image("${BACKEND_IMAGE}").push()
                        docker.image("${FRONTEND_IMAGE}").push()
                    }
                }
            }
        }

        stage('Update Kubernetes Manifests') {
            steps {
                // We'll generate YAMLs later
                echo 'Updating deployment YAML files...'
            }
        }
    }
}
