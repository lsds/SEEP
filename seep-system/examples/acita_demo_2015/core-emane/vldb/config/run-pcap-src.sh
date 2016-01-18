#!/bin/bash
tcpdump -i eth0 -s 0 host `hostname` -w `hostname`.cap
