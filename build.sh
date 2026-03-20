#!/bin/bash
# build.sh — H++ / Novel build script
# Usage: ./build.sh        (build only)
#        ./build.sh run <script>  (build and run)

set -e

SRC_DIR="src/main/java"
OUT_DIR="out"
JAR="hplusplus.jar"
MAIN="com.hplusplus.Main"

echo "[ H++ ] Compiling Novel interpreter..."
mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" > .sources.tmp
javac -d "$OUT_DIR" --release 21 @.sources.tmp
rm .sources.tmp

echo "[ H++ ] Packaging..."
jar --create --file "$JAR" --main-class "$MAIN" -C "$OUT_DIR" .

echo "[ H++ ] Build OK — $JAR"

if [ "$1" = "run" ]; then
    shift
    java -jar "$JAR" run "$@"
fi
