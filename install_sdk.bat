@echo off
set JAVA_HOME=C:\Program Files\Java\latest\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0android-sdk\cmdline-tools\bin"
echo y | sdkmanager.bat --licenses
echo y | sdkmanager.bat "platform-tools" "build-tools;33.0.0" "platforms;android-33" --sdk_root="%~dp0android-sdk"
pause