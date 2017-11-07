#!/bin/bash
INF=""
for i in "wlan1" "wlan0" "eth0" 
do
	found=$(cat /proc/net/dev | grep $i)
	echo "Found = $found"
	if [ "" != "$found" ] ; then
		INF="$i"
		break
	fi
done

#On linux, /proc/net/dev has the format below
#Inter-|   Receive                                               |  Transmit
# face | bytes packets errs drop fifo frame compressed multicast | bytes packets errs drop fifo colls carrier compressed

if [ "$INF" == "" ] ; then
	echo "No valid interface found on $(hostname)."
	exit
else
	echo "Recording network utilization for $INF on $(hostname)" 
fi

net_log="/tmp/$(hostname)-net-util.txt" 
cpu_log="/tmp/$(hostname)-cpu-util.txt" 

echo "##########" >> "$net_log" 
echo "##########" >> "$cpu_log" 
while true 
do
	txrx=$(cat /proc/net/dev | grep $INF | tr -s " ")
	cpu=$(cat /proc/stat | grep '^cpu ' | tr -s " ")

	now=$(date +%s%3N) 

	echo "$now$txrx" >> "$net_log" 
	echo "$now $cpu" >> "$cpu_log" 

	echo "$now$txrx"
	echo "$now $cpu"

	sleep 1
done
