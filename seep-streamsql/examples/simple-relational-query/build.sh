#!/bin/sh

SEEP="../../.."

[ ! -d "lib" ] && mkdir lib/
cp $SEEP/seep-system/target/seep-system-0.0.1-SNAPSHOT.jar lib/
cp $SEEP/seep-streamsql/target/seep-streamsql-0.0.1-SNAPSHOT.jar lib/

JCP="."
JCP=${JCP}:lib/seep-streamsql-0.0.1-SNAPSHOT.jar
JCP=${JCP}:lib/seep-system-0.0.1-SNAPSHOT.jar

javac -cp $JCP -Xlint:unchecked ./src/*.java

# Specific to GPU build
[ -f CongestedSegRel.cl ] && rm -f CongestedSegRel.cl
[ -f Operator.cl ] && rm -f Operator.cl
if [ "$1" == "gpu" ]; then
	cp $SEEP/seep-system/src/main/java/uk/ac/imperial/lsds/seep/gpu/CongestedSegRel.cl .
	cp $SEEP/seep-system/src/main/java/uk/ac/imperial/lsds/seep/gpu/Operator.cl .
fi

exit 0
