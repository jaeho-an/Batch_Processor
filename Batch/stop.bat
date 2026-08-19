@echo off
setlocal

set BASE_DIR=%~dp0
set PID_FILE=%BASE_DIR%pid\batch.pid

echo ========================================
echo  Batch Application Stop
echo ========================================

if not exist "%PID_FILE%" (
    echo Batch is not running.
    exit /b 0
)

set /p PID=<"%PID_FILE%"

echo PID : %PID%

REM 프로세스 존재 여부 확인
tasklist /FI "PID eq %PID%" | findstr "%PID%" > nul

if errorlevel 1 (
    echo Batch process not found.
    del "%PID_FILE%"
    exit /b 0
)

echo Stopping Batch...

taskkill /PID %PID% /T /F > nul

if errorlevel 1 (
    echo [ERROR] Failed to stop Batch.
    exit /b 1
)

del "%PID_FILE%"

echo Batch stopped.

endlocal