#mvn clean compile assembly:single ; mkdir -p seep-system/examples/acita_demo_2015/lib ; cp seep-system/target/seep-system-0.0.1-SNAPSHOT.jar seep-system/examples/acita_demo_2015/lib ; pushd seep-system/examples/acita_demo_2015 ; ant ; popd
#./gradlew build -x test
mkdir -p seep-system/examples/acita_demo_2015/lib
mkdir -p seep-system/examples/acita_demo_2015/tmp
pushd seep-system/examples/acita_demo_2015
ant clean
popd
#./gradlew build -x test
mvn clean compile assembly:single
#mvn assembly:single
cp seep-system/target/seep-system-0.0.1-SNAPSHOT.jar seep-system/examples/acita_demo_2015/lib
pushd seep-system/examples/acita_demo_2015
ant
popd
