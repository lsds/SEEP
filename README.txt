SEEP is an experimental parallel data processing system that is being developed
by the Large-Scale Distributed Systems (LSDS) research group
(http://lsds.doc.ic.ac.uk) at Imperial College London. It is licsensed under
EPL (Eclipse Public License).

The SEEP system is under heavy development and should be considered an alpha
release. This is not considered a "stable" branch.

Further details on SEEP, including papers that explain the underlying model 
can be found at the project website:
http://lsds.doc.ic.ac.uk/projects/SEEP

The SEEP system consists of two modules, the runtime system (seep-system) and a
compiler (java2sdg). Below is some information regarding how to build the
system and modules.

BUILDING:
#####################
The project follows the standard Maven directory structure, with two
differentiated modules, seep-system and seep-java2sdg.

There are two options to build the SEEP system:

Option 1, single jar (recommended) -- run:

mvn clean compile assembly:single

This produces one jar with all dependencies included.

Option 2, without dependencies -- to compile it:

mvn -DskipTests package

In this case, ensure that the classpath includes the dependencies.

You can alternatively build only individual modules, by running the same
options above inside seep-system or seep-java2sdg, respectively.

************
seep-system
************

RUNNING:
#########################
The system requires one master node and N worker nodes (one worker node per
Operator).

First set the IP address of the master node in "mainAddr" inside
config.properties and build the SEEP system.

Next run the master in the designated node:

java -jar <system.jar> Master <query.jar> <Base-class>

where query.jar is the compiled query and the last parameter is the name of 
the base class, not a path.

Finally run as many worker nodes as your query requires:

java -jar <system.jar> Worker

Local mode:

To run the SEEP system in a single local machine, append a different port to
each Worker node:

java -jar <system.jar> Worker <port>

*************
seep-java2sdg
*************

RUNNING:
###########################
It is mandatory to indicate an input program, an output file name and a target
(dot/seepjar) and the classpath to the driver program and its dependencies.
Examples: 

java -jar <java2sdg.jar> -i Driver -t dot -o myOutput -cp examples/

The above code will process input program "Driver" using the dependencies in
"examples/" to generate an output file "myOutput.dot".
###########################
