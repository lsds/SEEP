#!/bin/bash
tcpdump -i eth0 -s 0 -n host 10.0.0.8 -w n`hostname`.cap
