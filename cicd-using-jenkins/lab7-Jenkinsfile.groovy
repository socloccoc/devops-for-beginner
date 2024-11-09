pipeline {
    agent any
    environment {
		SONAR_PROJECT_KEY = 'complete-cicd'
		SONAR_SCANNER_HOME = tool 'SonarQubeScanner'
	}
    stages {
        // stage('Checkout') {
        //     steps {
        //         git 'https://github.com/hoanglinhdigital/nodejs-random-color.git'
        //     }
        // }

        stage('Build') {
            steps {
                sh 'docker build -t nodejs-random-color:ver-${BUILD_ID} .'
            }
        }
        stage('Upload image to ECR') {
            steps {
                sh 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 230467294357.dkr.ecr.us-east-1.amazonaws.com'
                sh 'docker tag nodejs-random-color:ver-${BUILD_ID} 230467294357.dkr.ecr.us-east-1.amazonaws.com/nodejs-random-color:ver-${BUILD_ID}'
                sh 'docker push 230467294357.dkr.ecr.us-east-1.amazonaws.com/nodejs-random-color:ver-${BUILD_ID}'
            }
        }
        stage('SonarQube Analysis'){
			steps {
				withCredentials([string(credentialsId: 'jenkins', variable: 'SONAR_TOKEN')]) {
    					
					withSonarQubeEnv('SonarQube') {
    						sh """
						${SONAR_SCANNER_HOME}/bin/sonar-scanner \
						-Dsonar.projectKey=${SONAR_PROJECT_KEY} \
						-Dsonar.sources=. \
						-Dsonar.host.url=http://35.197.147.42:9000 \
						-Dsonar.login=${SONAR_TOKEN}
						"""
					}
				}
			}
		}
    }
}
