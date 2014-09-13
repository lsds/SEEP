#!/bin/bash
source demo_env.sh
echo "Link state monitor watching $LINK_STATE_LOG"
cat /dev/null > $LINK_STATE_LOG
tail -f $LINK_STATE_LOG | ./link_state_server.py
