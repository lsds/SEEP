route | grep '^n' | tr -s ' ' | cut -d' ' -f1,5 | grep ' 1$' | sed "s/^/`hostname` /"
