#!/bin/bash

#Assumes /proc/stat contains lines formatted as:
#cpu user nice system idle iowait irq softirq
#TODO: Can probably ignore individual core utilisations?
while true ; do
	txrx=$(cat /proc/stat | grep '^cpu ')
	now=$(date +%s%3N) 
	host=$(hostname)
	echo "$now $txrx" >> "$host-cpu-util.txt" 
	sleep 1
done
