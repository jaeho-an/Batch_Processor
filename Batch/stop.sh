#!/bin/bash

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_FILE="$BASE_DIR/pid/batch.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "Batch is not running."
    exit 0
fi

PID=$(cat "$PID_FILE")

if ps -p "$PID" > /dev/null 2>&1; then

    echo "Stopping Batch..."
    kill "$PID"

    sleep 2

    if ps -p "$PID" > /dev/null 2>&1; then
        echo "Force stopping Batch..."
        kill -9 "$PID"
    fi

else
    echo "Batch process not found."
fi

rm -f "$PID_FILE"

echo "Batch stopped."