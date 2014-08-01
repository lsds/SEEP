#!/bin/bash

DEFAULT_INPUT_FILE="datafile20seconds.dat"
DEFAULT_EXEC_MODE="cpu"
USAGE="usage: ./run.sh [cpu|gpu] [input filename]"

# Classpath
JCP="."
JCP=${JCP}:src
JCP=${JCP}:lib/seep-streamsql-0.0.1-SNAPSHOT.jar
JCP=${JCP}:lib/seep-system-0.0.1-SNAPSHOT.jar

# Java library path
API="/mnt/data/cccad3/akolious/aparapi-read-only"
JLP="${API}/com.amd.aparapi.jni/dist"

OPTS="-server -XX:+UseConcMarkSweepGC -Xms8g -Xmx8g"

if [ $# -gt 2 ]; then
	echo $USAGE
	exit 1
fi

# Set execution mode
EXEC_MODE=$DEFAULT_EXEC_MODE
[ $# -gt 0 ] && EXEC_MODE=$1
echo "[DBG] mode is $EXEC_MODE"
if [ "$EXEC_MODE" != "cpu" -a "$EXEC_MODE" != "gpu" ]; then
	echo $USAGE
	exit 1
fi

# Do not load aparapi library for CPU execution
if [ "$EXEC_MODE" == "cpu" ]; then
	JLP=""
fi

# Set input file
INPUT_FILE=$DEFAULT_INPUT_FILE
[ $# -gt 1 ] && INPUT_FILE=$2
if [ ! -f $INPUT_FILE ]; then
	echo "error: ${INPUT_FILE} does not exist"
	exit 1
fi

java -Djava.library.path=$JLP $OPTS -cp $JCP LRBRunner $INPUT_FILE

exit 0
