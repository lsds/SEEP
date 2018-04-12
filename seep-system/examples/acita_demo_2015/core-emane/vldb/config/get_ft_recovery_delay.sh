#!/bin/bash

for f in 0 1 2 3 4 ; do grep 'Suceeded\|performing hard cleanup.*10' 3k/0.00m/"$f"s/worker-w8-k3-chain-n3*.log | grep -C 1 hard | grep -v hard | cut -d ' ' -f 1 | xargs -I{} date -d {} +%s%3N | awk -F' ' 'NR == 1 {old = $1; next}; {print ($1 - old) ; next} '; done | awk '{sum += $1} END {if (NR > 0) print sum / NR}' -





for f in 0 1 2 3 4 ; do grep 'oq.sync\|performing hard cleanup.*10' 3k/0.00m/"$f"s/worker-w8-k3-chain-n3*.log | awk '/performing/{recovering=1; next} /oq.sync/{if (recovering != 1) { tuple=$10 ; ts = $1 ; next;} else if (recovering == 1 && retransmitting != 1 && recovered == 0) { if ($10 > tuple) {tuple = $10 ; ts = $1; next} else {retransmitting = 1 ; next }} else if (recovering == 1 && retransmitting == 1 && recovered != 1) { if ($10 > tuple) { newtuple = $10 ; newts = $1 ; recovered = 1 ; next}} } END {print ts, "\n", newts}' | xargs -I{} date -d {} +%s%3N |  awk 'NR == 1 {old = $1; next}; {print ($1 - old) ; next} '; done | awk '{sum += $1} END {if (NR > 0) print sum / NR}' -
