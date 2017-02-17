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

From the top level directory:

./meander-bld.sh

Will build seep-system and the example applications stateless-simple-query and
acita_demo_2015.

************
seep-system
************

RUNNING:
#########################
The system requires one master node and N worker nodes (one worker node per
Operator).

First set the IP address of the master node in "mainAddr" inside
config.properties and rebuild the SEEP system. By default it is 127.0.0.1
so you don't need to change anything if running in local mode (see below).

Next run the master in the designated node:

java -jar <system.jar> Master <query.jar> <Base-class>

where query.jar is the compiled query and the last parameter is the name of 
the base class, not a path.

e.g. To run the master for the stateless-simple-query example:
cd seep-system/examples/stateless-simple-query
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Master `pwd`/query.jar Base

Finally run as many worker nodes as your query requires:

java -jar <system.jar> Worker

Local mode:

To run the SEEP system in a single local machine, append a different port to
each Worker node:

java -jar <system.jar> Worker <port>

e.g. For the stateless-simple-query example:
cd seep-system/examples/stateless-simple-query
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Worker 3501 
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Worker 3502 
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Worker 3503 

Note you will need to run the master and each worker in a different shell. Then follow
the instructions on the Master command prompt. 

Specifically, after giving the workers a few seconds to register with the Master, enter 1 at the Master command prompt.
This will deploy the operators in the query in src/Base.java to the Workers.
Once that has completed, simply press 2 to start the query, and enter to start the source.
You should see tuples being received in shell output for the Worker running the sink operator. 
