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
                    // git branch: 'main', url: 'https://github.com/DaGeRe/peass.git'
                    git branch: 'develop', url: 'https://github.com/DaGeRe/peass.git'
                    // last working version
                    // sh 'git checkout d1aed977f0fbcbe181500a728a0c0133e686ecfc'
                }
                dir('peass-ci') {
                    //  branch: 'main', url: 'https://github.com/DaGeRe/peass-ci.git'
                    git branch: 'develop', url: 'https://github.com/jenkinsci/peass-ci-plugin'
                    // last working version
                    // sh 'git checkout ef18d7be2d0a344b087b9ab5565d408772c91041'
                }
            }
        }
        stage('build peass'){
            steps{
                dir('peass') {
                    // (optional) for cleaning maven repo dependencies
                    //sh 'mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false'
                    sh 'unset MAVEN_CONFIG && ./mvnw clean install -DskipTests -P buildStarter'
                    sh 'unset MAVEN_CONFIG && ./mvnw clean package -DskipTests'
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
