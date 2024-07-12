def FLOW = [
  [env: "beta",    clusterId: "jpe1-caas1-dev1", namespace: "${env.JOB_NAME.split('/')[3]}", image: "build"],
]
def ENV_LIST = FLOW.collect{ it.env }
def flow

pipeline {
    agent any

    environment {
        // Get app name from Jenkins job url.
        // e.g, /Tenant/caas-pipeline/Namespace/caas-jenkins/Apps/nginx/ => APP_NAME=nginx
        APP_NAME = "${env.JOB_NAME.split('/')[5]}"

        // CHANGEME "image: nginx" in a deployment.yaml is replaced with built image url by kustomize dynamically.
        // If you want to replace different image name in a deployment.yaml, please use same image name in a deployment yaml.
        IMAGE_NAME = 'producer-app-for-stg-kafka'

        MANIFEST_FILE = 'resource.yaml'
    }

    stages {
        stage('Initialization') {
          steps {
            script {
              flow = new com.rakuten.cpd.Flow(this, FLOW)
              flow.setCurrentEnv(params.ENVIRONMENT)
              env.K8S_CLUSTER_ID = flow.getClusterId()
              env.K8S_NAMESPACE  = flow.getNamespace()
            }
          }
        }
        stage('Build and Push image') {
          when {
            expression { flow.isBuildImage() }
          }
          steps {
            script {
              // BUILD_TIMESTAMP comes from https://plugins.jenkins.io/build-timestamp/ plugin
              // If you want to use different tag, change this logic please
              def tag = "${env.BUILD_TIMESTAMP}-${env.GIT_COMMIT[0..6]}-${params.ENVIRONMENT}"

              cpd.withDockerRegistry(env.K8S_CLUSTER_ID, env.K8S_NAMESPACE) {
                // Build docker image with method of Jenkins docker plugin
                // https://jenkins.io/doc/book/pipeline/docker/
                def img = docker.build("${env.K8S_NAMESPACE}/${APP_NAME}:${tag}")
                img.push()
                env.DOCKER_IMAGE = img.imageName()
              }
            }
          }
        }
        stage('Promote image') {
          when {
            expression { flow.isPromoteImage() }
          }
          steps {
            script {
              // Retag target tag with new env name suffix
              // promoteImage method expects that your docker image tag ends with environment name
              env.DOCKER_IMAGE = cpd.promoteImage(flow, env.APP_NAME)
            }
          }
        }

         stage ('Build resource yaml') {
          steps {
            script {
              // Update image url with built image url in above stage
              sh label: 'build resource.yaml with kustomize', script: """
              cd k8s
                ${KUSTOMIZE_HOME}/kustomize edit set image ${IMAGE_NAME}=${env.DOCKER_IMAGE}
                ${KUSTOMIZE_HOME}/kustomize build --output /home/jenkins/agent/workspace/Tenant/ccbd-sens-sandbox/Namespace/ccbd-sens-sandbox-kafka-test/Apps/producer-app-for-stg-kafka/${MANIFEST_FILE}
              cd ..
              """

              // cpd.getNameFromManifest only support single resource
              // If you want to check rollout status of multiple deployments, please set DEPLOY_NAME statically and call kubectl rollout status twice.
              env.DEPLOY_NAME = cpd.getNameFromManifest(file: env.MANIFEST_FILE, kind: 'Deployment')
            }
          }
          post {
            success {
              archiveArtifacts env.MANIFEST_FILE
            }
          }
        }

        stage ('Apply manifests') {
          steps {
            script {
              def APPLY_STATUS = cpd.kubectl("apply -f ${MANIFEST_FILE}")
              if (APPLY_STATUS > 0) {
                error("\u001B[31m Applyment is failed. Please check syntax of resource yaml, your permission or try to logout/login Jenkins to refresh token.\u001B[0m ")
              }
            }
          }
        }

        stage('Watch the status of the rollout') {
          when { expression { return env.DEPLOY_NAME } }
          steps {
            script {
              // Wait until deployment become ready
              def ROLLOUT_STATUS = cpd.kubectl("rollout status deployment ${DEPLOY_NAME}")
              if (ROLLOUT_STATUS > 0) {
                error("\u001B[31m Rollout deployment ${DEPLOY_NAME} is failed.\nref \u001B[0m https://kubernetes.io/docs/tasks/debug-application-cluster/debug-application/ ")
                cpd.kubectl("get events | grep ${DEPLOY_NAME}")
              }
            }
          }
          post {
            aborted {
              script {
                echo "\u001B[31m Rollout deployment ${DEPLOY_NAME} is timeout.\nref \u001B[0m https://kubernetes.io/docs/tasks/debug-application-cluster/debug-application/ "
                cpd.kubectl("get events | grep ${DEPLOY_NAME}")
              }
            }
          }
        }
    }
    post {
      always {
        // Clean up workspace to avoid mixing with the results of previous builds
        // If you want to debug, comment out following line please
        cleanWs()
      }
    }
}
