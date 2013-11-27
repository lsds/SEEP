Seep is an experimental stream processing platform that is being developed by the Large-Scale Distributed Systems (LSDS) research group at Imperial College London. It is EPL (Eclipse Public License) licensed.

The system is currently under heavy development and should be considered an alpha release. This is not considered a "stable" branch.


BUILD:
#####################
The project follows the standard maven directory structure.

There are two options to build the system:

Option 1, without dependencies:
To compile it:
mvn -DskipTests package

then, make sure to include in your classpath the dependencies.

Option 2, single jar:
Run:
mvn clean compile assembly:single

To produce a one jar with all dependencies included

RUN:
#########################
The system requires one master node and N worker nodes (one worker node per Operator).

First set the IP of the master node in "mainAddr" inside config.properties and build the system.

Then run the master in the designated node:

java -jar <system.jar> Master <query.jar> <Base-class>
where query.jar is the compiled query

And finally run as many worker nodes as your query requires:
java -jar <system.jar> Worker

Local mode:
To run the system in a local machine, just append a different port to each Worker node:
java -jar <system.jar> Worker <port>
