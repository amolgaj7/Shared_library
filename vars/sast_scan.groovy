def call() {

    echo "===== Starting SAST Analysis with SonarQube ====="

    dir('CalculatorApp') {

        echo "===== Building Android Application ====="

        sh '''
            chmod +x gradlew
            ./gradlew clean assembleRelease --no-daemon
        '''

        echo "===== Running SonarQube SAST Scan ====="

        withCredentials([
            string(
                credentialsId: 'sonarqube',
                variable: 'SONAR_TOKEN'
            )
        ]) {

            sh '''
                sonar-scanner \
                  -Dsonar.projectKey=CalculatorApp \
                  -Dsonar.projectName=CalculatorApp \
                  -Dsonar.sources=app/src/main \
                  -Dsonar.java.binaries=app/build/intermediates/javac/release/classes \
                  -Dsonar.sourceEncoding=UTF-8 \
                  -Dsonar.host.url=http://sonarqube:9000 \
                  -Dsonar.token=$SONAR_TOKEN
            '''
        }

        echo "===== SonarQube SAST Scan Completed ====="
    }
}