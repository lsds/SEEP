mvn install:install-file -DgroupId=soot -DartifactId=soot-framework -Dversion=2.5.0 -Dpackaging=jar -Dfile=libs/soot/soot-framework/2.5.0/soot-2.5.0.jar
mvn install:install-file -DgroupId=aparapi -DartifactId=aparapi-framework -Dversion=0.0.1 -Dpackaging=jar -Dfile=libs/aparapi/aparapi-framework/0.0.1/aparapi-0.0.1.jar

mvn clean compile assembly:single
