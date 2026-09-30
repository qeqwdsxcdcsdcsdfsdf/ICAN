@echo off
cd /d "%~dp0"
if %errorlevel% neq 0 (
    echo ERROR: Failed to change to script directory!
    pause
    exit /b 1
)

chcp 65001 >nul
title FaceAttendanceGPS - Start All

echo ============================================
echo     Face+GPS Attendance System - Start All
echo ============================================
echo.
echo [INFO] System will start the following services:
echo [INFO]   - Backend Service (Port: 8082)
echo [INFO]   - Frontend Admin (Port: 8080)
echo.
echo [INFO] Please ensure:
echo [INFO]   1. MySQL is running, database name: face_attendance_gps
echo [INFO]   2. Port 8080 and 8082 are not occupied
echo.

set "JAVA_HOME=%JAVA_HOME%"
if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME environment variable not set
    pause
    exit /b 1
)

echo [INFO] Checking Java version...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not installed or not configured
    pause
    exit /b 1
)

echo [INFO] Checking Node.js version...
node -v >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js not installed or not configured
    pause
    exit /b 1
)

echo.
echo [INFO] Killing port 8080 processes...
powershell -Command "Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }"
taskkill /F /IM node.exe 2>nul

echo [INFO] Killing port 8082 processes...
powershell -Command "Get-NetTCPConnection -LocalPort 8082 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }"
taskkill /F /IM java.exe 2>nul

echo.
echo [INFO] Starting backend service in new window...
start cmd /k "cd /d ""%~dp002-SpringBoot-Server"" && call mvnw.cmd spring-boot:run"

echo [INFO] Waiting for backend to start...
ping -n 9 127.0.0.1 >nul

echo [INFO] Starting frontend service in new window...
start cmd /k "cd /d ""%~dp003-PC-Admin-Vue"" && call npm run dev"

echo.
echo ============================================
echo     Start completed!
echo ============================================
echo.
echo [INFO] Backend Service: http://localhost:8082
echo [INFO] Frontend Admin: http://localhost:8080
echo.
echo [INFO] Press any key to exit...
pause >nul