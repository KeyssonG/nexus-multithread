pipeline {
    agent any

    environment {
        DOCKERHUB_IMAGE = "keyssong/nexus-multithread"
        IMAGE_TAG = "build-${BUILD_NUMBER}"
        GITOPS_REPO = "KeyssonG/k8s-gitops"
        GITOPS_BRANCH = "main"
        DEPLOYMENT_FILE = "nexus/nexus-deployment.yaml"
        DOCKER_PATH = "${env.DOCKER_PATH}"
    }

    triggers {
        pollSCM('H/1 * * * *')
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
                        if ($LASTEXITCODE -ne 0) { throw "Falha no docker login" }

                        docker push "${env:DOCKERHUB_IMAGE}:${env:IMAGE_TAG}"
                        if ($LASTEXITCODE -ne 0) { throw "Falha no push da imagem ${env:DOCKERHUB_IMAGE}:${env:IMAGE_TAG}" }

                        docker push "${env:DOCKERHUB_IMAGE}:latest"
                        if ($LASTEXITCODE -ne 0) { throw "Falha no push da imagem ${env:DOCKERHUB_IMAGE}:latest" }
                    '''
                }
            }
        }

        stage('GitOps - Atualizar deployment.yaml') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'GitHub',
                        usernameVariable: 'GIT_USER',
                        passwordVariable: 'GIT_TOKEN'
                    )
                ]) {
                    powershell script: '''
                        $gitopsUrl = "https://$env:GIT_USER`:$env:GIT_TOKEN@github.com/$env:GITOPS_REPO.git"
                        $gitopsDir = "$env:WORKSPACE\\k8s-gitops"

                        if (Test-Path $gitopsDir) { Remove-Item -Recurse -Force $gitopsDir }
                        git clone --quiet $gitopsUrl $gitopsDir

                        $manifest = "$gitopsDir\\$env:DEPLOYMENT_FILE"
                        (Get-Content -Path $manifest) -replace 'image: .*', "image: $env:DOCKERHUB_IMAGE`:$env:IMAGE_TAG" | Set-Content -Path $manifest

                        git -C $gitopsDir add $env:DEPLOYMENT_FILE

                        git -C $gitopsDir diff --cached --quiet; if ($LASTEXITCODE -ne 0) {
                            git -C $gitopsDir -c user.name=Jenkins -c user.email=jenkins@pipeline.com commit -m "nexus: atualiza imagem Docker para ${env:IMAGE_TAG}"
                            git -C $gitopsDir push origin $env:GITOPS_BRANCH
                            echo "Deployment.yaml atualizado via GitOps (repo $env:GITOPS_REPO)."
                        } else {
                            echo "Nenhuma alteração no deployment.yaml"
                        }

                        git -C $gitopsDir remote set-url origin "https://github.com/$env:GITOPS_REPO.git"
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline concluída com sucesso! Imagem $env:DOCKERHUB_IMAGE:$env:IMAGE_TAG publicada e GitOps atualizado."
        }
        failure {
            echo "Erro na pipeline. Verifique os logs."
        }
    }
}
