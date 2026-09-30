$JAVA_HOME = "C:\Program Files\Java\latest\jdk-21"
$env:JAVA_HOME = $JAVA_HOME
$env:PATH = "$JAVA_HOME\bin;$env:PATH"

$SDK_ROOT = "C:\Users\35416\Desktop\网络工程\网络拓扑图\FaceAttendanceGPSTotal\android-sdk"
$TOOLS_DIR = "$SDK_ROOT\cmdline-tools\bin"

New-Item -ItemType Directory -Path "$SDK_ROOT\licenses" -Force | Out-Null

Set-Content -Path "$SDK_ROOT\licenses\android-sdk-license" -Value "8933bad161af4178b1185d1a37fbf41ea5269c55"
Set-Content -Path "$SDK_ROOT\licenses\android-sdk-preview-license" -Value "84831b9409646a918e30573bab4c9c91346d8abd"
Set-Content -Path "$SDK_ROOT\licenses\google-gdk-license" -Value "33b6a2b64607f11b759f320ef9dff4ae5c47d97a"

& "$TOOLS_DIR\sdkmanager.bat" "platform-tools" "build-tools;33.0.0" "platforms;android-33" --sdk_root="$SDK_ROOT"