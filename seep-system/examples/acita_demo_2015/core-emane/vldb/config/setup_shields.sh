#!/bin/bash
first=3
last=14
for i in $(seq $first 1 $last); do
	base_core=$((($i-$first) * 2))
	cores="$base_core,$((base_core+1)),$((base_core+32)),$((base_core+32+1))"
	echo "node$i, base=$base_core, cores=$cores"
#	sudo cset set -s "node$i" -c $cores 
	sudo cset set -d "node$i"
done

sys_last=16
sys_cores=""
for j in $(seq $(($last+1)) $((sys_last+$first-1))); do
	base_core=$((($j-$first) * 2))
	cores="$base_core,$((base_core+1)),$((base_core+32)),$((base_core+32+1))"
	if [ "$sys_cores" == "" ]
	then
		sys_cores="$cores" 
	else
		sys_cores="$sys_cores,$cores"
	fi
	echo "sys$j, base=$base_core, cores=$cores, sys_cores=$sys_cores"
done

echo "Creating sys shield with cores $sys_cores"
#sudo cset set -s manual_sys -c $sys_cores && sudo cset proc --move --fromset=root --toset=manual_sys -k
sudo cset set -d manual_sys

