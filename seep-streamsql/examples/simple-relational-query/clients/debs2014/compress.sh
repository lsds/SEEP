#!/bin/bash

OPTS="-server -XX:+UseConcMarkSweepGC -Xms7g -Xmx7g"
ARGS="-f ${1} -b ${2}"

java ${OPTS} SmartGridCompressor ${ARGS}

exit 0
