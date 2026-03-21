@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java not found. Install JDK 17+ and make sure "java" is on your PATH.
    exit /b 1
)

set "JAVA_VER="
for /f "tokens=3 delims=.\" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    if not defined JAVA_VER set "JAVA_VER=%%v"
)

if defined JAVA_VER (
    set /a JAVA_VER_NUM=%JAVA_VER% >nul 2>nul
    if !JAVA_VER_NUM! LSS 11 (
        echo WARNING: Java 11+ required ^(detected version !JAVA_VER_NUM!^). The build may fail.
    )
)

set "WRAPPER_JAR=gradle\wrapper\gradle-wrapper.jar"
set "JAR_URL=https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar"

if not exist "%WRAPPER_JAR%" goto download_wrapper
for %%A in ("%WRAPPER_JAR%") do set "WRAPPER_SIZE=%%~zA"
if not defined WRAPPER_SIZE goto download_wrapper
if %WRAPPER_SIZE% LSS 10000 goto download_wrapper
goto run_game

:download_wrapper
echo Gradle wrapper JAR missing or invalid. Downloading...
if not exist "gradle\wrapper" mkdir "gradle\wrapper"
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "try { Invoke-WebRequest -UseBasicParsing '%JAR_URL%' -OutFile '%WRAPPER_JAR%' } catch { exit 1 }"
if errorlevel 1 (
    echo ERROR: Failed to download the Gradle wrapper JAR.
    echo Please install Gradle and run: gradle wrapper --gradle-version 8.5
    exit /b 1
)
echo Gradle wrapper downloaded.

:run_game
echo Building and launching Rogue Forge...
call gradlew.bat :lwjgl3:run %*
exit /b %errorlevel%
