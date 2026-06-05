pipeline {
    agent any

    environment {
        DOCKERHUB_IMAGE = "keyssong/nexus-multithread"
        IMAGE_TAG = "latest"
        DEPLOYMENT_FILE_COMPANY = "k8s/company/empresa-deployment.yaml"
        DEPLOYMENT_FILE_INVENTORY = "k8s/inventory/estoque-deployment.yaml"
    }

    triggers {
        pollSCM('* * * * *')
    }

    options {
        disableConcurrentBuilds()
    }

    stages {
        stage('Verificar Branch') {
            when {
                branch 'master'
            }
            steps {
                echo "Executando pipeline na branch master"
            }
        }

        stage('Checkout do Código') {
            steps {
                checkout scm
            }
        }

        stage('Build com Gradle') {
            steps {
                sh './gradlew clean bootJar'
            }
        }

        stage('Build da Imagem Docker') {
            steps {
                sh '''
                    docker build -t $DOCKERHUB_IMAGE:$IMAGE_TAG -f modules/nexus/Dockerfile .
                    docker tag $DOCKERHUB_IMAGE:$IMAGE_TAG $DOCKERHUB_IMAGE:latest
                '''
            }
        }

        stage('Push da Imagem para Docker Hub') {
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

        stage('Atualizar Manifestos K8s (GitOps)') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'GitHub',
                        usernameVariable: 'GIT_USER',
                        passwordVariable: 'GIT_TOKEN'
                    )
                ]) {
                    sh '''
                        git checkout master
                        git config user.email "jenkins@pipeline.com"
                        git config user.name "Jenkins"
                        git remote set-url origin https://$GIT_USER:$GIT_TOKEN@github.com/KeyssonG/nexus-multithread.git

                        # Atualiza ambos os manifestos
                        sed -i "s|image: .*|image: $DOCKERHUB_IMAGE:$IMAGE_TAG|" $DEPLOYMENT_FILE_COMPANY
                        sed -i "s|image: .*|image: $DOCKERHUB_IMAGE:$IMAGE_TAG|" $DEPLOYMENT_FILE_INVENTORY

                        git add k8s/

                        if ! git diff --cached --quiet; then
                            git commit -m "Atualiza imagem Docker para nexus-multithread:$IMAGE_TAG"
                            git push origin master
                            echo "Alterações detectadas e enviadas ao repositório."
                        else
                            echo "Nenhuma alteração detectada nos manifestos K8s"
                        fi
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "🚀 Pipeline concluída com sucesso! Imagem unificada atualizada."
        }
        failure {
            echo "❌ Erro na pipeline. Verifique os logs."
        }
    }
}
