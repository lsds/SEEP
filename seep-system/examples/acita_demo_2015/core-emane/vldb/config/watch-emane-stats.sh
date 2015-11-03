#!/bin/bash

while true ; do
	emanesh $1 get stat '*' mac avgTimedEventLatencyRatio | xargs echo `date +%s` >> $1-emane-stats.txt
	sleep 1
done
