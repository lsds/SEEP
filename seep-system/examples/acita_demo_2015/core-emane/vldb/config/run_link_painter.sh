#!/bin/bash

source demo_env.sh

echo "Starting link painter on $LINK_PAINTER_ADDR:$LINK_PAINTER_PORT"
./link_painter.py --addr "$LINK_PAINTER_ADDR" --port $LINK_PAINTER_PORT --num_nodes 6
