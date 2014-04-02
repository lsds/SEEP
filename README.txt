Seep is an experimental parallel data processing system that is being developed by the Large-Scale Distributed Systems (LSDS) research group at Imperial College London. It is EPL (Eclipse Public License) licensed.

The system is currently under heavy development and should be considered an alpha release. This is not considered a "stable" branch.

The system consists of two modules, the runtime system (seep-system) and a cross-compiler (java2sdg). Following there is some information regarding how to build the system and modules.

BUILD:
#####################
The project follows the standard maven directory structure, with two differentiated modules, seep-system and seep-java2sdg

There are two options to build the system:

Option 1, single jar (recommended):
Run:
mvn clean compile assembly:single

To produce a one jar with all dependencies included

Option 2, without dependencies:
To compile it:
mvn -DskipTests package

then, make sure to include in your classpath the dependencies.


You can alternatively build only individual modules, by running the same options above inside seep-system or seep-java2sdg respectively.

************
seep-system
************

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

*************
seep-java2sdg
*************

RUN:
###########################
It is mandatory to indicate input program, output file name and target (dot/seepjar) and the classpath to the driver program and its dependencies.
Examples:

java -jar <java2sdg.jar> -i Driver -t dot -o myOutput -cp examples/

The above code will process input program "Driver" using the dependencies in "examples/" to generate an output file "myOutput.dot".
