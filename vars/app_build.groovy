def call() {
    def podYaml = '''
apiVersion: v1
kind: Pod
metadata:
  name: android-build-pod
  labels:
    role: build-agent
spec:
  restartPolicy: Never
  containers:
    - name: android-builder
      image: mobiledevops/android-sdk-image:34.0.0
      command:
        - cat
      tty: true
      securityContext:
        runAsUser: 0
      resources:
        requests:
          cpu: "1"
          memory: "1Gi"
        limits:
          cpu: "4"
          memory: "3Gi"
'''

    podTemplate(
        label: 'android-builder-agent',
        yaml: podYaml
    ) {
        node('android-builder-agent') {
            try {
                container('android-builder') {
                    stage('Checkout Code') {
                        checkout scm
                    }

                    stage('Build APK') {
                        echo "Building APK inside Kubernetes Pod"

                        sh '''
                            set -e

                            # Find gradlew dynamically in workspace
                            GRADLEW_PATH=$(find . -maxdepth 3 -name gradlew | head -n 1)

                            if [ -n "$GRADLEW_PATH" ]; then
                                echo "Using project wrapper at: $GRADLEW_PATH"
                                cd "$(dirname "$GRADLEW_PATH")"
                                chmod +x gradlew
                                ./gradlew assembleRelease \
                                    --no-daemon \
                                    -Dorg.gradle.workers.max=2 \
                                    -Dorg.gradle.jvmargs="-Xmx1800m -XX:MaxMetaspaceSize=384m"
                            else
                                echo "gradlew script not found in repo; using pre-installed system Gradle"
                                gradle assembleRelease \
                                    --no-daemon \
                                    -Dorg.gradle.workers.max=2 \
                                    -Dorg.gradle.jvmargs="-Xmx1800m -XX:MaxMetaspaceSize=384m"
                            fi
                        '''
                    }

                    stage('Archive Artifacts') {
                        archiveArtifacts artifacts: '**/*.apk', 
                                         fingerprint: true, 
                                         onlyIfSuccessful: true
                    }
                }
            } finally {
                deleteDir()
            }
        }
    }
}
