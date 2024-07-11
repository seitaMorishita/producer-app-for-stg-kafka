def FLOW = [
  [env: "beta",    clusterId: "jpe1-caas1-dev1", namespace: "${env.JOB_NAME.split('/')[3]}", image: "build"],
]
def ENV_LIST = FLOW.collect{ it.env }
def flow

pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'producer-app-for-stg-kafka:latest'
        K8S_NAMESPACE = 'default'
    }

    tools {
        maven 'apache-maven-3.0.4'
    }

    stages {
        stage('Checkout') {
            steps {
                // ソースコードをリポジトリからチェックアウト
                checkout scm
            }
        }
        stage('Build') {
            steps {
                script {
                    // Mavenを使用してアプリケーションをビルド
                    sh 'mvn clean package'
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    // Dockerイメージをビルド
                    sh "docker build -t ${DOCKER_IMAGE} ."
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    // DockerイメージをDockerレジストリにプッシュ
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh "echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin"
                        sh "docker tag ${DOCKER_IMAGE} ${DOCKER_USERNAME}/${DOCKER_IMAGE}"
                        sh "docker push ${DOCKER_USERNAME}/${DOCKER_IMAGE}"
                    }
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    // Kubernetesにデプロイ
                    sh 'kubectl apply -k k8s/'
                }
            }
        }
    }
}
