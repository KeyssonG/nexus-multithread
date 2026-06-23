pipeline {
    agent { label 'docker' }

    environment {
        DOCKERHUB_IMAGE = "keyssong/nexus-multithread"
        IMAGE_TAG = "latest"
    }

    triggers {
        pollSCM('* * * * *')
    }

    options {
        disableConcurrentBuilds()
    }

    stages {
        stage('Build Docker') {
            steps {
                sh '''
                    apt-get update -qq && apt-get install -y -qq docker.io
                    docker build -t $DOCKERHUB_IMAGE:$IMAGE_TAG -f modules/nexus/Dockerfile .
                    docker tag $DOCKERHUB_IMAGE:$IMAGE_TAG $DOCKERHUB_IMAGE:latest
                '''
            }
        }

        stage('Push Docker Hub') {
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

        stage('Atualizar Manifesto (GitOps)') {
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

                        sed -i "s|image: .*|image: $DOCKERHUB_IMAGE:$IMAGE_TAG|" k8s/nexus-deployment.yaml

                        git add k8s/

                        if ! git diff --cached --quiet; then
                            git commit -m "Atualiza imagem Docker para $IMAGE_TAG"
                            git push origin master
                            echo "Manifesto atualizado."
                        else
                            echo "Nenhuma alteracao no manifesto."
                        fi
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline concluida com sucesso!"
        }
        failure {
            echo "Falha na pipeline. Verifique os logs."
        }
    }
}
