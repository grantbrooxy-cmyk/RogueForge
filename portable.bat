@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd /d "%~dp0"

if not defined GRADLE_USER_HOME (
    set "GRADLE_USER_HOME=%~dp0.gradle-user-home"
)
if not exist "%GRADLE_USER_HOME%" (
    mkdir "%GRADLE_USER_HOME%" >nul 2>nul
)
if not exist "%GRADLE_USER_HOME%" (
    echo ERROR: Failed to create Gradle user home at "%GRADLE_USER_HOME%".
    pause
    exit /b 1
)

set "APP_IMAGE_DIR=%~dp0lwjgl3\build\app-image\RogueForge"
set "ZIP_DIR=%~dp0lwjgl3\build\portable"
set "ZIP_PATH=%ZIP_DIR%\RogueForge-portable.zip"

echo Building portable Rogue Forge app image...
call "%~dp0gradlew.bat" :lwjgl3:packageAppImage %*
set "BUILD_EXIT_CODE=%errorlevel%"
if not "%BUILD_EXIT_CODE%"=="0" (
    echo.
    echo ERROR: Portable app build failed.
    echo Gradle exited with code %BUILD_EXIT_CODE%.
    pause
    exit /b %BUILD_EXIT_CODE%
)

if not exist "%APP_IMAGE_DIR%" (
    echo.
    echo ERROR: Expected app image was not created at "%APP_IMAGE_DIR%".
    pause
    exit /b 1
)

if not exist "%ZIP_DIR%" (
    mkdir "%ZIP_DIR%" >nul 2>nul
)
if not exist "%ZIP_DIR%" (
    echo.
    echo ERROR: Failed to create output directory "%ZIP_DIR%".
    pause
    exit /b 1
)

if exist "%ZIP_PATH%" (
    del /f /q "%ZIP_PATH%" >nul 2>nul
)

echo Creating portable archive...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Compress-Archive -Path '%APP_IMAGE_DIR%\*' -DestinationPath '%ZIP_PATH%' -Force"
if errorlevel 1 (
    echo.
    echo ERROR: Failed to create "%ZIP_PATH%".
    pause
    exit /b 1
)

echo.
echo Portable build complete.
echo App folder: "%APP_IMAGE_DIR%"
echo Zip file:   "%ZIP_PATH%"
pause
exit /b 0
