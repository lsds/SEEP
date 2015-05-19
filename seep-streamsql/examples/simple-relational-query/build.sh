#!/bin/sh

SEEP="../../.."

[ ! -d "lib" ] && mkdir lib/
cp $SEEP/seep-system/target/seep-system-0.0.1-SNAPSHOT.jar lib/
cp $SEEP/seep-streamsql/target/seep-streamsql-0.0.1-SNAPSHOT.jar lib/

JCP="."
JCP=${JCP}:lib/seep-streamsql-0.0.1-SNAPSHOT.jar
JCP=${JCP}:lib/seep-system-0.0.1-SNAPSHOT.jar

javac -cp $JCP -Xlint:unchecked ./src/*.java
javac -cp $JCP -Xlint:unchecked ./src/synth/*.java

exit 0
