#!/bin/bash

OPTS="-server -XX:+UseConcMarkSweepGC -XX:NewRatio=2 -XX:SurvivorRatio=16 -Xms6g -Xmx6g"
ARGS="-h 192.168.100.107 -f compressed-512-norm.dat"

java ${OPTS} TaskEventReader ${ARGS}

exit 0
