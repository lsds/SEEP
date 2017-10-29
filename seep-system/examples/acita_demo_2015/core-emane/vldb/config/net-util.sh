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

while true 
do
	txrx=$(cat /proc/net/dev | grep $INF | tr -s " ")
	now=$(date +%s%3N) 
	log="$(hostname)-net-util.txt" 
	echo "$now$txrx" >> "$log" 
	sleep 1
done
