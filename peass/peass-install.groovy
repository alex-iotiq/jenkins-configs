pipeline {
    agent any
    stages {
        // (optional) for clean reinstall of peass if some dependencies broke for some reason
        // will delete all maven repos in local .m2 directory
        /*
        stage('remove local maven .m2 repositories'){
            steps{
                // use the path to your m2 repo (usually in /home/user/.m2)
                sh 'rm {PATH_TO_M2_REPO}/repo* -rf'
            }
        }
        */
        stage('clone peass and peass-ci') {
            steps{
                dir('peass') {
                    git branch: 'main', url: 'https://github.com/DaGeRe/peass.git'
                    // last working version
                    sh 'git checkout adb0c8f3ad604843c936eb3f7e2b357b9d4dbf2c'
                }
                dir('peass-ci') {
                    git branch: 'main', url: 'https://github.com/DaGeRe/peass-ci.git'
                    // last working version
                    sh 'git checkout 85b9c362da1717b1984611e8c7e4249174c3c5bc'
                }
            }
        }
        stage('build peass'){
            steps{
                dir('peass') {
                    // (optional) for cleaning maven repo dependencies
                    //sh 'mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false'
                    sh 'unset MAVEN_CONFIG && ./mvnw clean install -DskipTests'
                }
            }
        }
        stage('build peass-ci'){
            steps{
                dir('peass-ci') {
                    sh 'unset MAVEN_CONFIG && ./mvnw clean package -DskipTests'
                    // removing old peass-ci plugin from jenkins
                    sh 'rm $JENKINS_HOME/plugins/peass-ci* -rf'
                    // copying peass-ci plugin to jenkins
                    // jenkins needs a restart after finish
                    sh 'cp target/peass-ci.hpi $JENKINS_HOME/plugins/'
                }
            }
        }
    }
}