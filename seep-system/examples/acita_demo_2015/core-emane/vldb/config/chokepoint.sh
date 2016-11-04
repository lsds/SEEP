for f in `find chokepoint -name '*.pgm'` ; do echo $f; done` > chokepoint.txt

# The @ below changes the delimiter from / to @.
sed 's@chokepoint/P2E_S1_C3/\([[:digit:]]\+\)/\(.*\)@chokepoint/P2E_S1_C3/\1/\2,\1@' chokepoint.txt
