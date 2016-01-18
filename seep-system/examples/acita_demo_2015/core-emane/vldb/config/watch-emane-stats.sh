#!/bin/bash

while true ; do
	required=`cat emane-required-stats.txt | tr '\n' ' '`
	stats=`emanesh $1 get stat '*' mac $required`
	#emanesh $1 clear stat '*' mac $required
	IFS=$'\n'
	for line in $stats ; do
		stat=`echo $line | tr -s ' ' | cut -d ' ' -f 4`
		echo `date +%s` $line >> $1-$stat-emane-stats.txt
	done
	unset IFS
	sleep 5	
#	IFS=$'\n'
#	for line in `emanesh $1 get stat '*' mac` ; do
#		stat=`echo $line | tr -s ' ' | cut -d ' ' -f 4`
#		echo `date +%s` $line >> $1-$stat-emane-stats.txt
#	done
#	unset IFS
#	sleep 5
done

