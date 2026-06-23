pipeline {
    agent any

    environment {
        DOCKERHUB_IMAGE = "keyssong/nexus-multithread"
        IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        BRANCH_ENV = "${BRANCH_NAME}"
    }

    triggers {
        pollSCM('* * * * *')
    }

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout true
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Testes') {
            parallel {
                stage('Testes Unitários') {
                    steps {
                        sh './gradlew test --no-daemon'
                    }
                }
                stage('Validação SonarQube') {
                    steps {
                        sh './gradlew sonarqube --no-daemon || echo "SonarQube nao configurado"'
                    }
                }
            }
        }

        stage('Build') {
            parallel {
                stage('Build Gradle') {
                    steps {
                        sh './gradlew clean bootJar --no-daemon'
                    }
                }
                stage('Scan Trivy') {
                    steps {
                        sh '''
                            docker run --rm -v "$WORKSPACE:/app" aquasec/trivy:latest fs /app \
                              --severity HIGH,CRITICAL --exit-code 0 || true
                        '''
                    }
                }
            }
        }

        stage('Build Docker') {
            steps {
                sh '''
                    docker build -t $DOCKERHUB_IMAGE:$IMAGE_TAG -f modules/nexus/Dockerfile .
                    docker tag $DOCKERHUB_IMAGE:$IMAGE_TAG $DOCKERHUB_IMAGE:latest
                '''
            }
        }

        stage('Push Docker Hub') {
            when {
                anyOf {
                    branch 'dev'
                    branch 'homol'
                    branch 'master'
                }
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $DOCKERHUB_IMAGE:$IMAGE_TAG
                        docker push $DOCKERHUB_IMAGE:latest
                    '''
                }
            }
        }

        stage('Deploy K8s') {
            when {
                anyOf {
                    branch 'dev'
                    branch 'homol'
                    branch 'master'
                }
            }
            steps {
                withCredentials([
                    kubeconfigContent(
                        credentialsId: 'k8s-kubeconfig',
                        variable: 'KUBECONFIG_CONTENT'
                    )
                ]) {
                    sh '''
                        echo "$KUBECONFIG_CONTENT" > kubeconfig
                        export KUBECONFIG=kubeconfig
                        kubectl set image deployment/nexus-deployment -n nexus \
                          nexus=$DOCKERHUB_IMAGE:$IMAGE_TAG
                        kubectl rollout status deployment/nexus-deployment -n nexus \
                          --timeout=5m
                    '''
                }
            }
        }

        stage('Atualizar Manifesto (GitOps)') {
            when {
                anyOf {
                    branch 'dev'
                    branch 'homol'
                    branch 'master'
                }
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'GitHub',
                        usernameVariable: 'GIT_USER',
                        passwordVariable: 'GIT_TOKEN'
                    )
                ]) {
                    sh '''
                        BRANCH=$(git rev-parse --abbrev-ref HEAD)
                        ENV_DIR="k8s/overlays/$BRANCH"

                        git config user.email "jenkins@pipeline.com"
                        git config user.name "Jenkins"
                        git remote set-url origin \
                          https://$GIT_USER:$GIT_TOKEN@github.com/KeyssonG/nexus-multithread.git

                        if [ -f "$ENV_DIR/kustomization.yaml" ]; then
                            sed -i "s|newTag: .*|newTag: $IMAGE_TAG|" "$ENV_DIR/kustomization.yaml"
                        else
                            sed -i "s|image: .*|image: $DOCKERHUB_IMAGE:$IMAGE_TAG|" k8s/nexus
                        fi

                        git add k8s/

                        if ! git diff --cached --quiet; then
                            git commit -m "Atualiza imagem para $IMAGE_TAG [skip ci]"
                            git push origin $BRANCH
                            echo "Manifesto atualizado em $BRANCH."
                        else
                            echo "Nenhuma alteracao no manifesto."
                        fi
                    '''
                }
            }
        }

        stage('Notificação') {
            when {
                anyOf {
                    branch 'dev'
                    branch 'homol'
                    branch 'master'
                }
            }
            steps {
                sh '''
                    curl -X POST -H "Content-Type: application/json" \
                      -d '{"text": "Pipeline *$JOB_NAME* concluida\\n\
                        Imagem: $DOCKERHUB_IMAGE:$IMAGE_TAG\\n\
                        Branch: $BRANCH_NAME"}' \
                      "$SLACK_WEBHOOK_URL" || true
                '''
            }
        }
    }

    post {
        always {
            sh 'docker image prune -f || true'
            sh 'rm -f kubeconfig || true'
        }
        success {
            echo "Pipeline concluida com sucesso! ($DOCKERHUB_IMAGE:$IMAGE_TAG)"
        }
        failure {
            echo "Falha na pipeline. Verifique os logs."
        }
    }
}
