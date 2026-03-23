@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd /d "%~dp0"

set "JAVA_CMD="
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    )
)

if not defined JAVA_CMD (
    where java >nul 2>nul
    if errorlevel 1 (
        echo ERROR: Java not found.
        echo Install JDK 17+ and make sure either JAVA_HOME or PATH points to it.
        pause
        exit /b 1
    )
    set "JAVA_CMD=java"
)

set "JAVA_VERSION_LINE="
for /f "usebackq delims=" %%v in (`"%JAVA_CMD%" -version 2^>^&1`) do (
    if not defined JAVA_VERSION_LINE (
        set "JAVA_VERSION_LINE=%%v"
    )
)

set "JAVA_VER="
for /f "tokens=2 delims=\" %%v in ("!JAVA_VERSION_LINE!") do (
    set "JAVA_VER=%%v"
)
for /f "tokens=1 delims=.-_" %%v in ("!JAVA_VER!") do (
    set "JAVA_VER=%%v"
)

if defined JAVA_VER (
    2>nul set /a JAVA_VER_NUM=!JAVA_VER!
    if not errorlevel 1 (
        if !JAVA_VER_NUM! LSS 17 (
            echo WARNING: JDK 17+ is recommended. Detected Java !JAVA_VER_NUM!.
        )
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
    pause
    exit /b 1
)
echo Gradle wrapper downloaded.

:run_game
echo Building and launching Rogue Forge...
call "%~dp0gradlew.bat" :lwjgl3:run %*
set "EXIT_CODE=%errorlevel%"
echo.
echo Rogue Forge exited with code %EXIT_CODE%.
pause
exit /b %EXIT_CODE%
