#!/bin/bash

OPTS="-server -XX:+UseConcMarkSweepGC -Xms10g -Xmx12g"
ARGS="-h 192.168.100.107 -f compressed-1024.dat -b 1024"

java ${OPTS} SmartGridReader ${ARGS}

exit 0
