#!/bin/bash 

#iperf -c n3 -t 1000000 -i 1 -u -b 7m 
echo "iperf src starting sending to node $1"
if [ "$1" == "all" ] 
then
	iperf -u -c $1 -b 40M -t 100000 -i 10 
else
	iperf -c $1 -t 10000 -i 10 -w 32728 -N
fi

#iperf -c $1 -t 10000 -i 10 -w 32728 -N -u -b 7M 

#iperf -u -c $1 -b 40M -l 1024 -w 1024 -t 100000 -i 2
echo "iperf src finished sending to node $1"
