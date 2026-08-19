@echo off
setlocal

set BASE_DIR=%~dp0

set JAR_FILE=%BASE_DIR%jar\batch.jar
set CONF_FILE=%BASE_DIR%conf\application.yaml
set PID_FILE=%BASE_DIR%pid\batch.pid
set LOG_FILE=%BASE_DIR%log\batch.log

echo ========================================
echo  Batch Application Start
echo ========================================

if not exist "%JAR_FILE%" (
    echo [ERROR] JAR file not found.
    echo %JAR_FILE%
    exit /b 1
)

if not exist "%CONF_FILE%" (
    echo [ERROR] application.yaml not found.
    echo %CONF_FILE%
    exit /b 1
)

if not exist "%BASE_DIR%pid" mkdir "%BASE_DIR%pid"
if not exist "%BASE_DIR%log" mkdir "%BASE_DIR%log"

REM 기존 PID 확인
if exist "%PID_FILE%" (

    set /p PID=<"%PID_FILE%"

    tasklist /FI "PID eq %PID%" | findstr "%PID%" > nul

    if not errorlevel 1 (
        echo [ERROR] Batch is already running.
        echo PID : %PID%
        exit /b 1
    )

    del "%PID_FILE%"
)

echo Starting Batch Application...

start "Batch Application" /b cmd /c ^
"java -jar "%JAR_FILE%" --spring.config.additional-location="optional:file:%CONF_FILE%" > "%LOG_FILE%" 2>&1"

timeout /t 2 /nobreak > nul

REM batch.jar를 실행한 Java 프로세스 PID 검색
for /f "delims=" %%a in ('powershell -NoProfile -Command "(Get-CimInstance Win32_Process -Filter \"name = 'java.exe'\").Where({$_.CommandLine -like '*batch.jar*'}).ProcessId"') do (
    set PID=%%a
    goto :FOUND
)

echo [ERROR] Failed to find Batch Java process.
exit /b 1

:FOUND

echo %PID% > "%PID_FILE%"

echo.
echo Batch started.
echo PID : %PID%
echo LOG : %LOG_FILE%

endlocal