#!/bin/bash
#/sbin/route | grep '^n' | tr -s ' ' | cut -d' ' -f1,5 | grep -P ' \d+$' | sed "s/^/`hostname` /";

#echo "/all" | nc localhost 2006 | grep 'eth0' | tr -s '	' ' '| cut -d$' ' -f1,4 | sed 's/\/[0-9]\+//g' | sed "s/^/`hostname` /"
ss -i -t dst 10.0.0/24
