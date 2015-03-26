#!/bin/bash
echo "/top" | nc localhost 2006 | grep '^10\.' | tr -s '	' ' ' | cut -d$' ' -f1,2,5
