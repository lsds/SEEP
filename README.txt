# Frontier: Resilient Edge Processing for the Internet of Things.

Frontier is an experimental edge processing platform for the Internet of Things (IoT)
that is being developed by the Distributed and Global Computing Centre (DGC) at 
Royal Holloway University of London and the Large-Scale Distributed Systems (LSDS)
group at Imperial College London. It is licensed under EPL (Eclipse Public License).

The Frontier system is under heavy development and should be considered an alpha
release. This is not considered a "stable" branch.

Further details on Frontier, including a paper that explains the underlying model 
can be found at the project website:
http://lsds.doc.ic.ac.uk/projects/ita-dsm

Frontier builds on previous research into data-parallel stream processing for 
datacenter environments as part of the SEEP project: 
http://lsds.doc.ic.ac.uk/projects/ita-dsm

The SEEP system consists of two modules, the runtime system (seep-system) and a
compiler (java2sdg). Frontier uses parts of the SEEP runtime system codebase,
albeit heavily modified to operate in an edge environment. Below is some
information regarding how to build the system and modules.

BUILDING:
#####################
The project follows the standard Maven directory structure, with two
differentiated modules, seep-system and seep-java2sdg.

From the top level directory:

./frontier-bld.sh

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

e.g. To run the master for the acita_demo_2015 example:
cd seep-system/examples/acita_demo_2015
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Master `pwd`/dist/acita_demo_2015.jar Base

Finally run as many worker nodes as your query requires:

java -jar <system.jar> Worker

Local mode:

To run the SEEP system in a single local machine, append a different port to
each Worker node:

java -jar <system.jar> Worker <port>

e.g. For the acita_demo_2015 example:
cd seep-system/examples/acita_demo_2015
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Worker 3501 
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Worker 3502 
java -jar lib/seep-system-0.0.1-SNAPSHOT.jar Worker 3503 

Note you will need to run the master and each worker in a different shell. Then follow
the instructions on the Master command prompt. 

Specifically, after giving the workers a few seconds to register with the Master, enter 1 at the Master command prompt.
This will deploy the operators in the query in src/Base.java to the Workers.
Once that has completed, simply press 2 to start the query, and enter to start the source.
You should see tuples being received in shell output for the Worker running the sink operator. 


RASPBERRY PI FACE RECOGNITION INSTRUCTIONS
------------------------------------------
To run one of the face recognition queries on raspberry pi:
cd seep-system/examples/acita_demo_2015
cp ~/.m2/repository/org/bytedeco/javacv/1.2/javacv-1.2.jar lib
cp ~/.m2/repository/org/bytedeco/javacpp/1.2/javacpp-1.2.jar lib
cp ~/.m2/repository/org/bytedeco/javacpp-presets/1.2/javacpp-1.2.jar lib
cp ~/.m2/repository/org/bytedeco/javacpp-presets/opencv/3.1.0-1.2/opencv-3.1.0-1.2.jar lib
cp ~/.m2/repository/org/bytedeco/javacpp-presets/opencv/3.1.0-1.2/opencv-3.1.0-1.2-linux-armhf.jar lib
ant clean
ant

Next, start the master and workers using the following command line FROM THE seep-system/examples/acita_demo_2015/tmp DIRECTORY, i.e.
cd tmp
java -classpath "../lib/*" uk.ac.imperial.lsds.seep.Main Master `pwd`/../dist/acita_demo_2015.jar Base

N.B. Note the command line is different to before since we are now specifying the classpath explicitly so that it picks up all the jars in lib.
To start the workers, N.B. again FROM THE seep-system/examples/acita_demo_2015/tmp DIRECTORY!
cd tmp
java -classpath "../lib/*" uk.ac.imperial.lsds.seep.Main  Worker 3501

cd tmp
java -classpath "../lib/*" uk.ac.imperial.lsds.seep.Main  Worker 3502

cd tmp
java -classpath "../lib/*" uk.ac.imperial.lsds.seep.Main  Worker 3503

Now if you follow the Master command prompt as before it should run a face recognition query.
N.B. You must allow sufficient time for step 1 to complete. It may take a couple of minutes for the workers to
train the prediction model on a raspberry pi. When the workers are ready, the master command prompt will reappear.
DO NOT PROCEED TO STAGE 2 AT THE MASTER BEFORE THE 2ND COMMAND PROMPT APPEARS!
