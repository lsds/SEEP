#!/bin/bash
#/sbin/route | grep '^n' | tr -s ' ' | cut -d' ' -f1,5 | grep -P ' \d+$' | sed "s/^/`hostname` /";

#echo "/link" | nc localhost 2006 | grep 'eth0' | tr -s '	' ' '| cut -d$' ' -f1,4 | sed 's/\/[0-9]\+//g' | sed "s/^/`hostname` /"

sleep 60 
#sed -n -e '/Table: Links/,/Table: Routes/p' links.txt | grep '10.0.0' | tr -s '\t' ' '| cut -d$' ' -f1,2 
#cat links.txt | sed -n -e '/Table: Links/,/Table: Routes/p' | grep '10.0.0' | tr -s '\t' ' '| cut -d$' ' -f1,2 
echo "/link" | nc localhost 2006 | sed -n -e '/Table: Links/,/Table: Routes/p' | grep '10.0.0' | tr -s '\t' ' '| cut -d$' ' -f1,2 > links-$1.txt
