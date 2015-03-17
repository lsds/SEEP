#!/bin/sh
# session hook script; write commands here to execute on the host at the
# specified state
echo "2" > k.txt
echo "1" > h.txt
mkdir lib
cp /home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/lib/seep-system-0.0.1-SNAPSHOT.jar lib
