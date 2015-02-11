#!/bin/bash
source demo_env.sh
echo "Link state monitor watching $LINK_STATE_LOG"
cat /dev/null > $LINK_STATE_LOG
echo "Link state server listening on $LINK_STATE_ADDR:$LINK_STATE_PORT"
tail -f $LINK_STATE_LOG | ./link_state_server.py --port $LINK_STATE_PORT --addr "$LINK_STATE_ADDR"
