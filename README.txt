Seep is an experimental stream processing platform that is being developed by the Large-Scale Distributed Systems (LSDS) research group at Imperial College London. 

The system is currently under heavy development and should be considered an alpha release. In particular, this is a "feature" branch. A "stable" branch will be created once the first release comes.

The project follows the standard maven directory structure.

There are two options to run the system:

Option 1, without dependencies:
To compile it:
mvn -DskipTests package

then, make sure to include in your classpath the dependencies.

Option 2, single jar:
Run:
mvn clean compile assembly:single

To produce a one jar with all dependencies included
