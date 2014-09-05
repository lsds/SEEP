#!/bin/bash

DEFAULT_INPUT_FILE="datafile20seconds.dat"
DEFAULT_EXEC_MODE="cpu"
DEFAULT_L="1"
DEFAULT_CLASS="LRBRunner"
USAGE="usage: ./run.sh [cpu|gpu] [input filename] [L]"

# Classpath
JCP="."
JCP=${JCP}:src
JCP=${JCP}:lib/seep-streamsql-0.0.1-SNAPSHOT.jar
JCP=${JCP}:lib/seep-system-0.0.1-SNAPSHOT.jar

# Java library path
API="/mnt/data/cccad3/akolious/aparapi-read-only"
JLP="${API}/com.amd.aparapi.jni/dist"

# OPTS="-server -XX:+UseConcMarkSweepGC -Xms4g -Xmx8g"
OPTS="-server -XX:+UseConcMarkSweepGC -Xms4g -Xmx10g -Xloggc:test-gc.out"

if [ $# -gt 3 ]; then
	echo $USAGE
	exit 1
fi

# Set execution mode
EXEC_MODE=$DEFAULT_EXEC_MODE
[ $# -gt 0 ] && EXEC_MODE=$1
if [ "$EXEC_MODE" != "cpu" -a "$EXEC_MODE" != "gpu" ]; then
	echo $USAGE
	exit 1
fi
# Do not load aparapi library for CPU execution
[ "$EXEC_MODE" == "cpu" ] && JLP=""

# Select class
CLASS=$DEFAULT_CLASS
# [ "$EXEC_MODE" == "gpu" ] && CLASS="LRBGPURunner"

# Set input file
INPUT_FILE=$DEFAULT_INPUT_FILE
[ $# -gt 1 ] && INPUT_FILE=$2
if [ ! -f $INPUT_FILE ]; then
	echo "error: ${INPUT_FILE} does not exist"
	exit 1
fi

# Set L
L=$DEFAULT_L
[ $# -gt 2 ] && L=$3

java -Djava.library.path=$JLP $OPTS -cp $JCP $CLASS $INPUT_FILE $L 2>&1 >test.out &
seep_pid=$!

echo $(($(date +%s%N)/1000000)) >> test-cpu.out
top -b -n240 -d 1 | grep "Cpu" >> test-cpu.out

# mon_pid=$!
# sleep 180

kill -9 $seep_pid

echo "Done."
# kill -9 $mon_pid

sleep 10

echo "Bye."

exit 0
