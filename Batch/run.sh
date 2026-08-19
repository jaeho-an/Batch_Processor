#!/bin/bash

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

JAR_FILE="$BASE_DIR/jar/batch.jar"
CONF_FILE="$BASE_DIR/conf/application.yaml"
PID_FILE="$BASE_DIR/pid/batch.pid"
LOG_FILE="$BASE_DIR/log/batch.log"

echo "========================================"
echo " Batch Application Start"
echo "========================================"

if [ ! -f "$JAR_FILE" ]; then
    echo "[ERROR] JAR file not found."
    exit 1
fi

if [ ! -f "$CONF_FILE" ]; then
    echo "[ERROR] application.yaml not found."
    exit 1
fi

if [ -f "$PID_FILE" ]; then

    PID=$(cat "$PID_FILE")

    if ps -p "$PID" > /dev/null 2>&1; then
        echo "[ERROR] Batch is already running."
        echo "PID : $PID"
        exit 1
    else
        rm -f "$PID_FILE"
    fi
fi

nohup java -jar "$JAR_FILE" \
    --spring.config.additional-location="optional:file:$CONF_FILE" \
    > "$LOG_FILE" 2>&1 &

PID=$!

echo "$PID" > "$PID_FILE"

echo "Batch started."
echo "PID : $PID"
echo "LOG : $LOG_FILE"