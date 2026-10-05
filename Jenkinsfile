pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME = "andrei195/rng-app"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                // run tests inside a Maven + JDK 25 container: the Jenkins agent only needs Docker
                sh '''
                    docker run --rm \
                      -v "$PWD":/app \
                      -v "$HOME/.m2":/root/.m2 \
                      -w /app \
                      maven:3.9-eclipse-temurin-25 \
                      mvn -B test
                '''
            }
            post {
                always {
                    // show test results in the Jenkins UI, even when tests fail
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('Docker Push') {
            steps {
                // single quotes: the shell reads the secret at runtime, Groovy never interpolates it
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                sh "docker push ${IMAGE_NAME}:latest"
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                // swap :latest for this build's tag on the fly, so K8s sees a new image and rolls it out
                sh "sed 's|${IMAGE_NAME}:latest|${IMAGE_NAME}:${IMAGE_TAG}|' deployment.yml | kubectl apply -f -"
                // wait for the new pods to become ready; fails the pipeline if they don't
                sh "kubectl rollout status deployment/rng-deployment --timeout=120s"
            }
        }
    }

    post {
        success {
            echo 'Pipeline finished successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
