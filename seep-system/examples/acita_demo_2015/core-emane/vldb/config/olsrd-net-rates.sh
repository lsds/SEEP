#!/bin/bash
#/sbin/route | grep '^n' | tr -s ' ' | cut -d' ' -f1,5 | grep -P ' \d+$' | sed "s/^/`hostname` /";

#echo "/route" | nc localhost 2006 | grep 'eth0\|wlan0' | tr -s '	' ' '| cut -d$' ' -f1,4 | sed 's/\/[0-9]\+//g' | sed "s/^/`hostname` /"
echo "/route" | nc localhost 2006 | grep 'eth0\|wlan0' | tr -s '	' ' '| cut -d$' ' -f1,4 | sed 's/\/[0-9]\+//g' | sed "s/^/`ifconfig wlan0 | grep 'inet addr:' | tr -s ' ' | cut -d ' ' -f 3 | cut -d ':' -f 2` /"
