@echo off
echo ===============================================
echo   人脸+GPS考勤系统 - Android打包脚本
echo ===============================================
echo.

cd /d "%~dp0"

echo [1/3] 正在清理构建缓存...
call gradlew clean
if %errorlevel% neq 0 (
    echo 清理失败，请检查环境配置
    pause
    exit /b 1
)

echo.
echo [2/3] 正在构建Release版本...
call gradlew assembleRelease
if %errorlevel% neq 0 (
    echo 构建失败，请检查代码错误
    pause
    exit /b 1
)

echo.
echo [3/3] 构建完成！
echo.
echo APK文件位置: app\build\outputs\apk\release\app-release.apk
echo.
echo 请将APK文件复制到手机上进行安装
echo.
echo 安装方式：
echo 1. 将APK文件复制到手机存储中
echo 2. 在手机上打开文件管理器，找到APK文件
echo 3. 点击APK文件进行安装（需要允许"未知来源"安装）
echo.

pause