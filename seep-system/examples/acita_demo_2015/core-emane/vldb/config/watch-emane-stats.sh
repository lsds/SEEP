#!/bin/bash

while true ; do
	IFS=$'\n'
	for line in `emanesh $1 get stat '*' mac` ; do
		stat=`echo $line | tr -s ' ' | cut -d ' ' -f 4`
		echo `date +%s` $line >> $1-$stat-emane-stats.txt
	done
	unset IFS
	sleep 5
done

