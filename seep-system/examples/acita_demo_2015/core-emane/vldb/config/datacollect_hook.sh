#!/bin/sh
# session hook script; write commands here to execute on the host at the
# specified state
#scriptDir=%s
scriptDir=/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/core-emane
#timeStr=%s
timeStr=TODO
#k=%dk
k=1k
#mob=%.2fm
mob=0.00m
#session=%ds
session=TODOs
resultsDir=$scriptDir/log/$timeStr/$k/$mob/$session

expDir=$(pwd)

echo $expDir >> /tmp/datacollect.log
echo $scriptDir >> /tmp/datacollect.log
echo $timeStr >> /tmp/datacollect.log
echo $resultsDir >> /tmp/datacollect.log

mkdir -p $resultsDir

# Copy all log files to results dir
for d in n*.conf 
do
	cp $d/log/*.log $resultsDir	
	cp $d/mappingRecordOut.txt $resultsDir	
	cp $d/mappingRecordOut.txt $scriptDir/log/$timeStr/session${session}MappingRecord.txt
done
	

cd $scriptDir
#./gen_core_results.py --expDir log/$timeStr 
./gen_core_results.py --expDir $resultsDir
cd $expDir

