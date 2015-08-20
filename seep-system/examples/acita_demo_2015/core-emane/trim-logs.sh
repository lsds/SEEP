for f in `find log/$1 -type f -name '*worker*.log' | xargs grep -H -c 'FINISHED' | grep 0$ | cut -d':' -f1`; do sudo rm $f ; done
