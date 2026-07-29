pipeline {
    agent any

    environment {
        DOCKERHUB_IMAGE = "keyssong/nexus-multithread"
        IMAGE_TAG = "build-${BUILD_NUMBER}"
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

        stage('Deploy') {
            steps {
                echo "Imagem $env:DOCKERHUB_IMAGE:$env:IMAGE_TAG publicada no Docker Hub."
                echo "Execute manualmente: kubectl rollout restart deployment/nexus-deployment -n producao"
            }
        }
    }

    post {
        success {
            echo "Pipeline concluída com sucesso! Imagem $env:DOCKERHUB_IMAGE:$env:IMAGE_TAG publicada."
        }
        failure {
            echo "Erro na pipeline. Verifique os logs."
        }
    }
}
