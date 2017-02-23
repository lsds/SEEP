pushd seep-system/examples/acita_demo_2015 ; ant clean ; popd 
pushd seep-system/examples/stateless-simple-query ; ant clean ; popd
mvn clean compile assembly:single 

mkdir -p seep-system/examples/acita_demo_2015/lib 
cp seep-system/target/seep-system-0.0.1-SNAPSHOT.jar seep-system/examples/acita_demo_2015/lib 

mkdir -p seep-system/examples/stateless-simple-query/lib 
cp seep-system/target/seep-system-0.0.1-SNAPSHOT.jar seep-system/examples/stateless-simple-query/lib 

pushd seep-system/examples/acita_demo_2015 ; ant ; popd 
pushd seep-system/examples/stateless-simple-query ; ant ; popd
