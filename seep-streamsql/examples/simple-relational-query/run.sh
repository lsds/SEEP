#!/bin/bash

USAGE="usage: ./run.sh [class name]"

# Classpath
JCP="."
JCP=${JCP}:src
JCP=${JCP}:lib/seep-streamsql-0.0.1-SNAPSHOT.jar
JCP=${JCP}:lib/seep-system-0.0.1-SNAPSHOT.jar

OPTS="-server -XX:+UseConcMarkSweepGC -XX:NewRatio=2 -Xms16g -Xmx16g -Xloggc:test-gc.out"

# if [ $# -gt 1 ]; then
#    echo $USAGE
#    exit 1
# fi

CLASS=$1

java $OPTS -Djava.library.path="/mnt/data/cccad3/akolious/aparapi/com.amd.aparapi.jni/dist" -cp $JCP $CLASS $2 # 2>&1 >test.out &

# seep_pid=$!
# top -b -n120 -d 1 | grep "Cpu" >> test-cpu.out
# kill -9 $seep_pid

echo "Done."
sleep 1
echo "Bye."

exit 0
