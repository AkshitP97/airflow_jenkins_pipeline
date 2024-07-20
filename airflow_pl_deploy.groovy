pipeline {
    agent any
    
    parameters {
        string(name: 'BRANCH_NAME', defaultValue: 'main', description: 'Branch to fetch the code from')
        password(name: 'ACCESS_TOKEN', defaultValue: '', description: 'Access Token for Google Cloud')
        string(name: 'FILE_TO_DEPLOY', defaultValue: '', description: 'File to deploy to GCS')
        string(name: 'GCS_BUCKET', defaultValue: '', description: 'Google Cloud Storage Bucket Name')
    }
    
    environment {
        GIT_REPO = 'https://github.com/AkshitP97/airflow.git' // Replace with your GitHub repository URL
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                git url: "${GIT_REPO}", branch: "${params.BRANCH_NAME}"
            }
        }
        
        stage('List Files and Directories') {
            steps {
                script {
                    def files = sh(script: 'ls -R', returnStdout: true).trim()
                    echo "Files and directories:\n${files}"
                }
            }
        }
        
        stage('Deploy to GCS') {
            steps {
                script {
                    if (params.FILE_TO_DEPLOY == '') {
                        error 'No file specified for deployment'
                    }
                    
                    // Install gsutil if not already installed
                    sh 'curl https://sdk.cloud.google.com | bash'
                    sh 'exec -l $SHELL'
                    sh 'gcloud init'

                    // Use the provided access token for authentication
                    sh """
                        echo "${params.ACCESS_TOKEN}" | gcloud auth activate-refresh-token
                    """

                    // Deploy the selected file to the GCS bucket
                    sh "gsutil cp ${params.FILE_TO_DEPLOY} gs://${params.GCS_BUCKET}/"
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed.'
        }
    }
}