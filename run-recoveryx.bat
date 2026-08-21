@echo off
setlocal
title RecoveryX Pro Launcher

echo ========================================================
echo               RecoveryX Pro - Data Recovery
echo ========================================================
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12"
set "PATH=%JAVA_HOME%\bin;%PATH%"

set "JAR_PATH=%~dp0modules\recovery-ui\target\recovery-ui-1.0.0-SNAPSHOT.jar"

if not exist "%JAR_PATH%" (
    echo [ERROR] Application JAR not found at:
    echo %JAR_PATH%
    echo.
    echo Please build the project first using Maven.
    pause
    exit /b 1
)

if "%~1"=="" (
    echo Launching RecoveryX Pro Desktop Application...
    echo.
    start "" javaw -jar "%JAR_PATH%"
) else (
    echo Running RecoveryX Pro CLI...
    echo.
    java -jar "%JAR_PATH%" %*
)

endlocal
