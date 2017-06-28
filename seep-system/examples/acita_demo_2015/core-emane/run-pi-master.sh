#!/bin/bash
rm ../tmp/*
#cp static/pi_srcsink_constraints.txt ../tmp/mappingRecordIn.txt
#cp static/pi_k4fr_constraints.txt ../tmp/mappingRecordIn.txt
cp static/piadhoc_srcsink_constraints.txt ../tmp/mappingRecordIn.txt
cd ../tmp
java -classpath "../lib/*" uk.ac.imperial.lsds.seep.Main Master `pwd`/../dist/acita_demo_2015.jar Base
