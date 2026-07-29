pipeline {
    agent any

    environment {
        DOCKERHUB_IMAGE = "keyssong/nexus-multithread"
        IMAGE_TAG = "build-${BUILD_NUMBER}"
        DEPLOYMENT_FILE = "k8s/nexus-deployment.yaml"
        DOCKER_PATH = "C:\\Users\\keyss\\AppData\\Local\\Programs\\Rancher Desktop\\resources\\resources\\win32\\bin"
    }

    triggers {
        pollSCM('H/5 * * * *')
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

        stage('Build da Imagem Docker') {
            steps {
                powershell script: '''
                    $env:Path = "$env:DOCKER_PATH;$env:Path"
                    docker build -t "${env:DOCKERHUB_IMAGE}:${env:IMAGE_TAG}" -t "${env:DOCKERHUB_IMAGE}:latest" -f modules/nexus/Dockerfile .
                '''
            }
        }

        stage('Push da Imagem para Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'DockerHub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    powershell script: '''
                        $env:Path = "$env:DOCKER_PATH;$env:Path"
                        docker login -u "$env:DOCKER_USER" --password "$env:DOCKER_PASS"
                        docker push "${env:DOCKERHUB_IMAGE}:${env:IMAGE_TAG}"
                        docker push "${env:DOCKERHUB_IMAGE}:latest"
                    '''
                }
            }
        }

        stage('Atualizar deployment.yaml (GitOps)') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'GitHub',
                        usernameVariable: 'GIT_USER',
                        passwordVariable: 'GIT_TOKEN'
                    )
                ]) {
                    powershell script: '''
                        $repoUrl = (git remote get-url origin) -replace 'https://', "https://$env:GIT_USER`:$env:GIT_TOKEN@"
                        git remote set-url origin $repoUrl

                        git fetch origin
                        git checkout master
                        git reset --hard origin/master

                        (Get-Content -Path $env:DEPLOYMENT_FILE) -replace 'image: .*', "image: $env:DOCKERHUB_IMAGE`:$env:IMAGE_TAG" | Set-Content -Path $env:DEPLOYMENT_FILE

                        git add $env:DEPLOYMENT_FILE

                        git diff --cached --quiet; if ($LASTEXITCODE -ne 0) {
                            git -c user.name=Jenkins -c user.email=jenkins@pipeline.com commit -m "Atualiza imagem Docker para ${env:IMAGE_TAG} [skip ci]"
                            git push origin master
                            echo "Deployment atualizado via GitOps."
                        } else {
                            echo "Nenhuma alteração no deployment.yaml"
                        }
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline concluída com sucesso! Imagem atualizada e GitOps acionado."
        }
        failure {
            echo "Erro na pipeline. Verifique os logs."
        }
    }
}
