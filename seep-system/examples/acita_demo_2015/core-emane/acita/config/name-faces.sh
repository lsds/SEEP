#!/bin/bash
for i in {10..30} ; do rename "s/00000/Person$i -/" "00$i"/*.pgm ; done
