pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = credentials('DOCKER_REGISTRY')
        GITHUB_USERNAME = credentials('GITHUB_USERNAME')
        GHCR_TOKEN      = credentials('GHCR_TOKEN')
        IMAGE_TAG       = "latest"
        NAMESPACE       = "bank-app"
        CODE_SERVICES   = "accounts-service transfers-service cash-service notification-service blocker-service exchange-service front-ui exchange-generator"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Tests') {
            steps {
                script {
                    def buildStages = CODE_SERVICES.split().collectEntries { svc ->
                        ["Build ${svc}": {
                            dir(svc) {
                                sh './gradlew clean build -x contractTest'
                            }
                        }]
                    }
                    parallel buildStages
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def dockerStages = CODE_SERVICES.split().collectEntries { svc ->
                        ["Docker ${svc}": {
                            sh "docker build -t ${svc}:${IMAGE_TAG} ${svc}"
                        }]
                    }
                    parallel dockerStages
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    sh "echo ${GHCR_TOKEN} | docker login ghcr.io -u ${GITHUB_USERNAME} --password-stdin"

                    def pushStages = CODE_SERVICES.split().collectEntries { svc ->
                        ["Push ${svc}": {
                            sh """
                                docker tag ${svc}:${IMAGE_TAG} ghcr.io/${GITHUB_USERNAME}/${svc}:${IMAGE_TAG}
                                docker push ghcr.io/${GITHUB_USERNAME}/${svc}:${IMAGE_TAG}
                            """
                        }]
                    }
                    parallel pushStages
                }
            }
        }

        stage('Helm Deploy Umbrella Chart') {
            steps {
                sh """
                    helm upgrade --install full-app ./helm \\
                    --namespace ${NAMESPACE} \\
                    --create-namespace \\
                    --set keycloak.url=http://keycloak:8080 \\
                    --set image.tag=${IMAGE_TAG} \\
                    --wait --timeout=5m
                """
            }
        }

        stage('Verify Pods') {
            steps {
                script {
                    def HELM_SERVICES = [
                        'accounts',
                        'transfers',
                        'cash',
                        'notification',
                        'blocker',
                        'exchange',
                        'front',
                        'exchange-generator',
                        'keycloak'
                    ]

                    HELM_SERVICES.each { svc ->
                        sh """
                            echo "⏳ Waiting for ${svc} pod to be ready..."
                            kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=${svc} -n ${NAMESPACE} --timeout=120s
                        """
                    }
                    echo "✅ All pods are ready"
                }
            }
        }

        stage('Set Ports for Local Access') {
            steps {
                sh '''
                    NAMESPACE=bank-app

                    declare -A SERVICES_PORTS=(
                        ["keycloak"]=8080
                        ["accounts-service"]=8081
                        ["transfers-service"]=8082
                        ["cash-service"]=8083
                        ["notification-service"]=8084
                        ["front-ui"]=8085
                        ["blocker-service"]=8086
                        ["exchange-service"]=8087
                        ["exchange-generator"]=8088
                    )

                    for svc in "${!SERVICES_PORTS[@]}"; do
                        port=${SERVICES_PORTS[$svc]}
                        echo "🔌 Forwarding $svc → localhost:$port"
                        nohup kubectl port-forward -n "$NAMESPACE" "svc/$svc" "$port:$port" >/dev/null 2>&1 &
                    done

                    echo "✅ All port-forwards started in background"
                    sleep 5
                '''
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
