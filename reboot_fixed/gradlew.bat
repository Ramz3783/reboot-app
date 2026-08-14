@echo off
setlocal
set "APP_HOME=%~dp0"
set "GRADLE_VERSION=8.9"
set "DIST_NAME=gradle-%GRADLE_VERSION%-bin.zip"
set "DIST_URL=https://services.gradle.org/distributions/%DIST_NAME%"
if "%GRADLE_USER_HOME%"=="" set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "DIST_DIR=%GRADLE_USER_HOME%\wrapper\dists\gradle-%GRADLE_VERSION%-bin"
set "GRADLE_BIN=%DIST_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat"
if exist "%GRADLE_BIN%" goto run
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
set "ZIP=%DIST_DIR%\%DIST_NAME%"
if not exist "%ZIP%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri '%DIST_URL%' -OutFile '%ZIP%'"
  if errorlevel 1 exit /b %errorlevel%
)
if exist "%DIST_DIR%\gradle-%GRADLE_VERSION%" rmdir /s /q "%DIST_DIR%\gradle-%GRADLE_VERSION%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%DIST_DIR%'"
if errorlevel 1 exit /b %errorlevel%
:run
call "%GRADLE_BIN%" %*
exit /b %errorlevel%
