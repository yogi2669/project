pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/JaiBhargav/project.git'
        GIT_BRANCH = 'master'
        DOCKER_HUB_USER = 'bhargavjupalli'
        DOCKER_IMAGE = 'backend-app'
        NEXUS_URL = '13.233.88.239:8081'
        NEXUS_REPO = 'my-maven-releases'
        MAVEN_CRED_ID = 'maven-credentials'       // Jenkins credentials ID for Nexus
        DOCKER_CRED_ID = 'dockerhub-credentials'  // Jenkins credentials ID for DockerHub
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO}"
            }
        }

        stage('Build Maven App') {
            steps {
                dir('backend') { // Go inside 'backend' folder
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Upload to Nexus') {
            steps {
                dir('backend') {
                    withCredentials([usernamePassword(credentialsId: "${maven-cred}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh '''
                            mvn deploy:deploy-file \
                              -DgroupId=com.aitechie \
                              -DartifactId=backend \
                              -Dversion=0.0.1-SNAPSHOT \
                              -Dpackaging=jar \
                              -Dfile=target/backend-0.0.1-SNAPSHOT.jar \
                              -DrepositoryId=nexus \
                              -Durl=http://${NEXUS_URL}/repository/${NEXUS_REPO}/ \
                              -DgeneratePom=true \
                              -DretryFailedDeploymentCount=3 \
                              -Dusername=$USERNAME \
                              -Dpassword=$PASSWORD
                        '''
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('backend') {
                    sh '''
                        echo "FROM openjdk:17-jdk-slim" > Dockerfile
                        echo "COPY target/myapp.jar app.jar" >> Dockerfile
                        echo 'ENTRYPOINT ["java", "-jar", "/app.jar"]' >> Dockerfile
                        docker build -t ${DOCKER_HUB_USER}/${DOCKER_IMAGE}:latest .
                    '''
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CRED_ID}", usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push ${DOCKER_HUB_USER}/${DOCKER_IMAGE}:latest
                        docker logout
                    '''
                }
            }
        }

        stage('Update Deployment YAML') {
            steps {
                dir('backend') {
                    sh '''
                        sed -i 's|image: .*|image: ${DOCKER_HUB_USER}/${DOCKER_IMAGE}:latest|' deployment.yaml
                    '''
                }
            }
        }
    }
}
