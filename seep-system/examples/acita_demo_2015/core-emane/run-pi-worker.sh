#!/bin/bash

if [ -z $1 ] 
then
	echo "Need port for worker."
	exit
fi

cd ../tmp

java -classpath "../lib/*" uk.ac.imperial.lsds.seep.Main  Worker $1 2>&1 | tee "worker$1.log"

#for i in $(seq 1 $1) ; do
# echo "350$i"
	#java -classpath "../lib/*" uk.ac.imperial.lsds.seep.Main  Worker "350$i" 2>&1 | tee "worker$i.log"
#done
