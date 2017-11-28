#!/bin/bash
if [ "$1" == "" ] ; then
	echo "No experiment directory given"
	exit
fi


for f in `find $1 -type f -name '*worker*.log' | xargs grep -H -c 'FINISHED' | grep 0$ | cut -d':' -f1`; do sudo rm $f ; done
