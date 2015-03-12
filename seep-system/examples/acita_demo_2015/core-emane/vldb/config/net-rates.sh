#!/bin/bash
/sbin/route | grep '^n' | tr -s ' ' | cut -d' ' -f1,5 | grep -P ' \d+$' | sed "s/^/`hostname` /";
