#!/bin/bash
# ExamSeating Pro — build & run script

set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
OUT="$DIR/out"

echo "==> Compiling ExamSeating Pro..."
mkdir -p "$OUT"
javac -d "$OUT" "$DIR"/*.java && echo "==> Compiled successfully."

echo "==> Launching..."
cd "$OUT"
java Main "$@"
