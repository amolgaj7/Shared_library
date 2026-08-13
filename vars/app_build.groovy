def call() {
stage('build') {
    echo "Building the project"

    sh '''
        set -e

        echo "===== Jenkins Environment ====="
        whoami
        hostname
        pwd

        echo "===== Configure Java ====="
        export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
        export PATH=$JAVA_HOME/bin:$PATH

        echo "JAVA_HOME=$JAVA_HOME"
        java --version

        echo "===== Configure Gradle ====="
        export GRADLE_HOME=/opt/gradle/current
        export PATH=$GRADLE_HOME/bin:$PATH

        echo "GRADLE_HOME=$GRADLE_HOME"
        which gradle
        gradle --version

        echo "===== Configure Android SDK ====="
        export ANDROID_HOME=/opt/android-sdk
        export ANDROID_SDK_ROOT=/opt/android-sdk

        export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
        export PATH=$ANDROID_HOME/platform-tools:$PATH
        export PATH=$ANDROID_HOME/emulator:$PATH

        echo "ANDROID_HOME=$ANDROID_HOME"
        echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
        echo "PATH=$PATH"

        echo "===== Verify Android SDK Directory ====="

        if [ ! -d "$ANDROID_HOME" ]; then
            echo "ERROR: Android SDK directory does not exist:"
            echo "$ANDROID_HOME"
            exit 1
        fi

        ls -ld "$ANDROID_HOME"

        echo "===== Verify Android SDK Command Line Tools ====="

        if [ ! -d "$ANDROID_HOME/cmdline-tools/latest/bin" ]; then
            echo "ERROR: Android SDK command-line tools not found:"
            echo "$ANDROID_HOME/cmdline-tools/latest/bin"
            exit 1
        fi

        echo "===== SDK Manager ====="

        which sdkmanager
        sdkmanager --version

        echo "===== ADB ====="

        which adb
        adb version

        echo "===== Android SDK Components ====="

        echo "Checking installed SDK components..."

        sdkmanager --list | grep -E 'build-tools;33.0.1|platforms;android-34' || true

        echo "===== Accept Android SDK Licenses ====="

        yes | sdkmanager --licenses >/dev/null || true

        echo "===== Install Required Android SDK Components ====="

        sdkmanager "platforms;android-34" "build-tools;33.0.1"

        echo "===== Verify Android Platform ====="

        if [ ! -d "$ANDROID_HOME/platforms/android-34" ]; then
            echo "ERROR: Android platform 34 is missing."
            exit 1
        fi

        echo "Android platform 34 found."

        echo "===== Verify Android Build Tools ====="

        if [ ! -d "$ANDROID_HOME/build-tools/33.0.1" ]; then
            echo "ERROR: Android Build Tools 33.0.1 is missing."
            exit 1
        fi

        echo "Android Build Tools 33.0.1 found."

        echo "===== Project Directory ====="

        cd CalculatorApp

        echo "Project directory:"
        pwd

        echo "===== Project Files ====="

        ls -la

        echo "===== Gradle Wrapper Verification ====="

        chmod +x gradlew

        ls -lh gradle/wrapper/

        test -f gradle/wrapper/gradle-wrapper.jar
        test -f gradle/wrapper/gradle-wrapper.properties

        echo "Gradle wrapper files verified successfully."

        echo "===== Create local.properties ====="

        echo "sdk.dir=$ANDROID_HOME" > local.properties

        cat local.properties

        echo "===== Verify Android SDK From Project ====="

        test -d "$ANDROID_HOME/platforms/android-34"
        test -d "$ANDROID_HOME/build-tools/33.0.1"

        echo "Android SDK verification successful."

        echo "===== Gradle Version ====="

        which gradle
        gradle --version

        echo "===== Cleaning Project ====="

        gradle clean --no-daemon

        echo "===== Building APK ====="

        gradle assembleRelease --no-daemon

        echo "===== APK Generated ====="

        ls -lh app/build/outputs/apk/release/

        echo "===== Build Completed Successfully ====="
    '''
}

}
