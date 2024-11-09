def COLOR_MAP = [
    'SUCCESS': 'good', 
    'FAILURE': 'danger',
]
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/socloccoc/nodejs-random-color.git'
            }
        }

        stage('Build') {
            steps {
                sh 'docker build -t nodejs-random-color:ver-${BUILD_ID} .'
            }
        }
        stage('Upload image to ECR') {
            steps {
                sh 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 230467294357.dkr.ecr.us-east-1.amazonaws.com'
                sh 'docker tag nodejs-random-color:latest 230467294357.dkr.ecr.us-east-1.amazonaws.com/nodejs-random-color:ver-${BUILD_ID}'
                sh 'docker push 230467294357.dkr.ecr.us-east-1.amazonaws.com/nodejs-random-color:ver-${BUILD_ID}'
            }
        }
    }
    post {
        always {
            echo 'Build has been finished! Notify to Slack'
            slackSend channel: '#daidv-test',
                color: COLOR_MAP[currentBuild.currentResult],
                message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} \n More info at: ${env.BUILD_URL}"
        }
    }
}
