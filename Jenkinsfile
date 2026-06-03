pipeline {
    agent any

    environment {
        REGISTRY_CONTAINER_NAME = "adminserviceregistry"

        TARGET_CONTAINER_NAME = "hotelgateway"
        TARGET_IMAGE_NAME = "hotelgateway:latest"

        HOST_PORT = "9093"
        CONTAINER_PORT = "9093"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Docker Version') {
            steps {
                sh 'docker --version'
            }
        }

        stage('Check Registry') {
            steps {
                script {
                    def isRegistryRunning = sh(
                        script: "docker ps -q -f name=${REGISTRY_CONTAINER_NAME}",
                        returnStdout: true
                    ).trim()

                    if (!isRegistryRunning) {
                        error "${REGISTRY_CONTAINER_NAME} is not running. Aborting deployment."
                    }

                    echo "${REGISTRY_CONTAINER_NAME} is running. Proceeding..."
                }
            }
        }

        stage('Remove Existing Container and Image') {
            steps {
                sh "docker rm -f ${TARGET_CONTAINER_NAME} || true"
                sh "docker rmi -f ${TARGET_IMAGE_NAME} || true"
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "DOCKER_BUILDKIT=0 docker build --no-cache -t ${TARGET_IMAGE_NAME} ."
            }
        }

        stage('Run Container') {
            steps {
                sh """
                    docker run -d \
                    --name ${TARGET_CONTAINER_NAME} \
                    -p ${HOST_PORT}:${CONTAINER_PORT} \
                    --restart unless-stopped \
                    ${TARGET_IMAGE_NAME}
                """
            }
        }
    }

    post {
        always {
            echo '✅ Pipeline execution completed.'
        }
    }
}
