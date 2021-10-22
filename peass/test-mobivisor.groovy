pipeline {
    agent any
    stages {
        // for clean clone
        stage('remove current project'){
            steps{
                sh 'rm -rf ${JENKINS_HOME}/workspace/${JOB_NAME}/*'
                sh 'rm -rf ${JENKINS_HOME}/workspace/${JOB_NAME}/.git*'
                sh 'rm -rf ${JENKINS_HOME}/workspace/${JOB_NAME}/.gradle*'
            }
        }
        // {MOBIVISOR_GITLAB} = path to AndroidMdmAgent project
        stage('clone'){
            steps{
                git branch: 'master', url: '${MOBIVISOR_GITLAB}', credentialsId: 'bormdm'
            }
        }
        // for clean rerun
        // or use "Clean Peass-CI Cache" on the job page
        stage('delete peass results'){
            steps{
                script{
                    sh 'rm -rf ${JENKINS_HOME}/jobs/${JOB_NAME}/peass-data'
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
        stage('filter passing tests'){
            steps{
                script{
                    sh 'git -c sequence.editor="sed -i \'\'" rebase -i "HEAD~2" --exec "cp -fr ${JENKINS_HOME}/peass_scripts/build.gradle ${WORKSPACE}/app && python3 ${JENKINS_HOME}/peass_scripts/find_unit_tests.py ${WORKSPACE}/app/src && git add ${WORKSPACE}/app/build.gradle ${WORKSPACE}/app/src/test/* && git commit --amend --no-edit --allow-empty" -X "theirs"'
//                    sh 'sh ${JENKINS_HOME}/peass_scripts/change_history.sh 2 ${WORKSPACE}/app/src'
//                     sh 'python3 ${JENKINS_HOME}/peass_scripts/find_unit_tests.py "${WORKSPACE}/app/src"'
//                     sh 'sh ${JENKINS_HOME}/peass_scripts/git_edit_setup.sh'
//                     sh 'git edit-peass HEAD~3 "${JENKINS_HOME}/peass_scripts/build.gradle" "${WORKSPACE}/app"'
                }
            }
        }
        stage('performance test') {
            // it's possible to generate the pipeline script with the "pipeline syntax" link under the pipeline script -> sample step: Measure: Versionsperformance messen
            // this is the fastest configuration possible, for more accurate measurements you need more VMs / iterations / repetitions / warmup
            // with includes it's possible to cherry pick the tests to make it faster
            steps {
              measure VMs: 2, generateCoverageSelection: true, createDefaultConstructor: true, includes: 'com.boryazilim.android.bormdm.app.test.LogTest#testVerbose', iterations: 1, measurementMode: 'COMPLETE', repetitions: 1, testGoal: 'testBorDebugUnitTest', timeout: 10, updateSnapshotDependencies: false, useSampling: false, useSourceInstrumentation: false, warmup: 1, displayLogs: 'true', displayRCALogs: 'true', displayRTSLogs: 'true', redirectToNull: false
            }
        }
    }
}
