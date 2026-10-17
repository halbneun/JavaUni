@echo off
setlocal
rem Bootstrap Gradle 9.8.1; to replace with official Wrapper run the wrapper task.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0gradle-bootstrap.ps1" %*
exit /b %ERRORLEVEL%
