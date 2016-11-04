#!/bin/bash

echo "iperf $1 sink starting listening."
echo "Received $# arguments"
if [ "$1" == "udp" ]
then
	iperf -s -u -i 1
else
	iperf -s -i 1
fi
#iperf -s -u -i 1
#iperf -s -i 10
#iperf -s -i 10 -w 32728 -N -u
#iperf -s -i 10 -w 32728 -N 
#iperf -s -i 10 -w 32728 -N 
#iperf -u -s -i 2 -l 1024
