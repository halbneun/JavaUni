@echo off
setlocal
rem First-run bootstrap: download official Gradle wrapper JAR to this project.
set "APP_HOME=%~dp0"
set "WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
if not exist "%WRAPPER_JAR%" (
  echo Downloading official Gradle 9.8.0 Wrapper JAR ^(first run only^)...
  powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-9.8.0-wrapper.jar' -OutFile '%WRAPPER_JAR%'"
  if errorlevel 1 (
    echo ERROR: Wrapper download failed. Check your Internet connection.
    exit /b 1
  )
)
java -cp "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
