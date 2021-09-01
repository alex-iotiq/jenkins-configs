pipeline {
    agent any
    stages {
        
        stage('clone'){
            steps{
                git url: 'https://github.com/alex-iotiq/android-example-2'
                //git url: '${MOBIVISOR}'
            }
        }
        
        stage('remove peass-data'){
            steps{
                sh 'rm -rf ${JENKINS_HOME}/jobs/${JOB_NAME}/peass-data'
            }
        }
        stage('delete peass results'){
            steps{
                script{
                    sh '''temp_job_folder="${JENKINS_HOME}/workspace/${JOB_NAME}_fullPeass"
                    if [ -d $temp_job_folder ]
                    	then
                        	echo "removing ${temp_job_folder}"
                            rm -rf $temp_job_folder
                        else
                        	echo "no ${temp_job_folder} yet"
                    fi'''
                }
            }
        }
    
        stage('Test') {
            steps {
              measure VMs: 2, createDefaultConstructor: false, generateCoverageSelection: true, iterations: 1, measurementMode: 'COMPLETE', repetitions: 1, testGoal: 'testRelease', timeout: 10, updateSnapshotDependencies: false, useSampling: false, useSourceInstrumentation: false, displayLogs: 'true', displayRCALogs: 'true', displayRTSLogs: 'true'
            }
        }/*
        stage('gradle builds'){
            steps{
                sh '${JENKINS_HOME}/workspace/peass/gradle_passed.sh ${JENKINS_HOME}/workspace/${JOB_NAME}_fullPeass/${JOB_NAME}_peass/logs'
            }
        }
        
        stage('show rca log'){
            steps{
                sh 'cat ${JENKINS_HOME}/workspace/${JOB_NAME}_fullPeass/rca_*.txt'
            }
        }*/
    }
}