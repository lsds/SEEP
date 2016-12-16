#!/bin/bash

grep -rPIn SNK.*latency *.log | grep -Po ',ts=\d+,' | grep -Po '\d+' | sort -n | uniq | awk '$1!=p+1{print p+1"-"$1-1}{p=$1}' > gaps.txt
