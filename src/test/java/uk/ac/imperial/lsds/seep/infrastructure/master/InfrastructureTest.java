package uk.ac.imperial.lsds.seep.infrastructure.master;

import java.net.InetAddress;

import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.comm.RuntimeCommunicationTools;
import uk.ac.imperial.lsds.seep.elastic.MockState;
import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.QuerySpecificationI;
import uk.ac.imperial.lsds.seep.comm.NodeManagerCommunication;
import uk.ac.imperial.lsds.seep.elastic.ElasticInfrastructureUtils;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.MonitorManager;
import java.util.Map;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.state.State;
import junit.framework.*;

/**
 * The class <code>InfrastructureTest</code> contains tests for the class <code>{@link Infrastructure}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:06
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class InfrastructureTest extends TestCase {
	/**
	 * Run the Infrastructure(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testInfrastructure_1()
		throws Exception {
		int listeningPort = 1;

		Infrastructure result = new Infrastructure(listeningPort);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the void addNode(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testAddNode_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);

		fixture.addNode(n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void addNodeToStarTopology(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testAddNodeToStarTopology_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;
		InetAddress ip = InetAddress.getLocalHost();

		fixture.addNodeToStarTopology(opId, ip);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void addOperator(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testAddOperator_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;

		fixture.addOperator(o);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void addSource(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testAddSource_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator op = null;

		fixture.addSource(op);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void broadcastStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testBroadcastStarTopology_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.broadcastStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void broadcastStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testBroadcastStarTopology_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.broadcastStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void broadcastStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testBroadcastStarTopology_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.broadcastStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void broadcastStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testBroadcastStarTopology_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.broadcastStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void broadcastState(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testBroadcastState_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator op = null;

		fixture.broadcastState(op);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void broadcastState(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testBroadcastState_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator op = null;

		fixture.broadcastState(op);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void broadcastState(State) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testBroadcastState_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		State s = new MockState();

		fixture.broadcastState(s);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void broadcastState(State) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testBroadcastState_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		State s = new MockState();

		fixture.broadcastState(s);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureRouterStatically() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testConfigureRouterStatically_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.configureRouterStatically();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureRouterStatically() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testConfigureRouterStatically_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.configureRouterStatically();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureRouterStatically() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testConfigureRouterStatically_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.configureRouterStatically();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureSourceRate(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testConfigureSourceRate_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int numberEvents = 1;
		int time = 1;

		fixture.configureSourceRate(numberEvents, time);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureSourceRate(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testConfigureSourceRate_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int numberEvents = 1;
		int time = 1;

		fixture.configureSourceRate(numberEvents, time);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void createInitialStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testCreateInitialStarTopology_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.createInitialStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void createInitialStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testCreateInitialStarTopology_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.createInitialStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void createInitialStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testCreateInitialStarTopology_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.createInitialStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void createInitialStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testCreateInitialStarTopology_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.createInitialStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void createInitialStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testCreateInitialStarTopology_5()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.createInitialStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void createInitialStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testCreateInitialStarTopology_6()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.createInitialStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void createInitialStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testCreateInitialStarTopology_7()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.createInitialStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void createInitialStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testCreateInitialStarTopology_8()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.createInitialStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_5()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_6()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_7()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_8()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_9()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_10()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_11()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_12()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_13()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_14()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_15()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_16()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deploy();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deploy(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeploy_17()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator op = null;

		fixture.deploy(op);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deployConnection(String,QuerySpecificationI,QuerySpecificationI,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeployConnection_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		String command = "";
		QuerySpecificationI opToContact = null;
		QuerySpecificationI opToAdd = null;
		String operatorType = "";

		fixture.deployConnection(command, opToContact, opToAdd, operatorType);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deployConnection(String,QuerySpecificationI,QuerySpecificationI,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeployConnection_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		String command = "";
		QuerySpecificationI opToContact = null;
		QuerySpecificationI opToAdd = null;
		String operatorType = "";

		fixture.deployConnection(command, opToContact, opToAdd, operatorType);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deployQueryToNodes() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeployQueryToNodes_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deployQueryToNodes();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deployQueryToNodes() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeployQueryToNodes_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deployQueryToNodes();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void deployQueryToNodes() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDeployQueryToNodes_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.deployQueryToNodes();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void failure(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testFailure_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		fixture.failure(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void failure(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testFailure_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		fixture.failure(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void failure(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testFailure_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		fixture.failure(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void failure(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testFailure_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		fixture.failure(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void failure(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testFailure_5()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		fixture.failure(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the NodeManagerCommunication getBCU() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetBCU_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		NodeManagerCommunication result = fixture.getBCU();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the int getBaseId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetBaseId_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		int result = fixture.getBaseId();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the byte[] getDataFromFile(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetDataFromFile_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		String pathToQueryDefinition = "";

		byte[] result = fixture.getDataFromFile(pathToQueryDefinition);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the byte[] getDataFromFile(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetDataFromFile_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		String pathToQueryDefinition = "";

		byte[] result = fixture.getDataFromFile(pathToQueryDefinition);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the byte[] getDataFromFile(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetDataFromFile_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		String pathToQueryDefinition = "";

		byte[] result = fixture.getDataFromFile(pathToQueryDefinition);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the byte[] getDataFromFile(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetDataFromFile_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		String pathToQueryDefinition = "";

		byte[] result = fixture.getDataFromFile(pathToQueryDefinition);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the ElasticInfrastructureUtils getEiu() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetEiu_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		ElasticInfrastructureUtils result = fixture.getEiu();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Map<Integer, QuerySpecificationI> getElements() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetElements_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		Map<Integer, QuerySpecificationI> result = fixture.getElements();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the MonitorManager getMonitorManager() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetMonitorManager_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		MonitorManager result = fixture.getMonitorManager();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Node getNodeFromPool() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNodeFromPool_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		Node result = fixture.getNodeFromPool();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Node getNodeFromPool() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNodeFromPool_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		Node result = fixture.getNodeFromPool();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Node getNodeFromPool() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNodeFromPool_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		Node result = fixture.getNodeFromPool();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Node getNodeFromPool() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNodeFromPool_4()
		throws Exception {
		try {
			Infrastructure fixture = new Infrastructure(1);
			fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
			fixture.setPathToQueryDefinition("");
			fixture.elements = new HashMap();
			fixture.value = 1;
			fixture.incrementBaseId();
			fixture.getNodeFromPool();
			fixture.addOperator((Operator) null);
			fixture.addSource((Operator) null);

			Node result = fixture.getNodeFromPool();

			// add additional test code here
			fail("The exception java.lang.NumberFormatException should have been thrown.");
		} catch (java.lang.NumberFormatException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the int getNodePoolSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNodePoolSize_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		int result = fixture.getNodePoolSize();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the int getNumDownstreams(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNumDownstreams_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		int result = fixture.getNumDownstreams(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the int getNumDownstreams(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNumDownstreams_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		int result = fixture.getNumDownstreams(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the int getNumDownstreams(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNumDownstreams_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		int result = fixture.getNumDownstreams(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the int getNumUpstreams(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNumUpstreams_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		int result = fixture.getNumUpstreams(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the int getNumUpstreams(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNumUpstreams_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		int result = fixture.getNumUpstreams(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the int getNumUpstreams(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNumUpstreams_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		int result = fixture.getNumUpstreams(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the int getNumberRunningMachines() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetNumberRunningMachines_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		int result = fixture.getNumberRunningMachines();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the int getOpIdFromIp(InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOpIdFromIp_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress ip = InetAddress.getLocalHost();

		int result = fixture.getOpIdFromIp(ip);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the int getOpIdFromIp(InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOpIdFromIp_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress ip = InetAddress.getLocalHost();

		int result = fixture.getOpIdFromIp(ip);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the int getOpIdFromIp(InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOpIdFromIp_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress ip = InetAddress.getLocalHost();

		int result = fixture.getOpIdFromIp(ip);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertEquals(0, result);
	}

	/**
	 * Run the String getOpType(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOpType_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		String result = fixture.getOpType(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the String getOpType(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOpType_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		String result = fixture.getOpType(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the String getOpType(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOpType_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		String result = fixture.getOpType(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator getOperatorById(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOperatorById_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opIdToParallelize = 1;

		Operator result = fixture.getOperatorById(opIdToParallelize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator getOperatorById(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOperatorById_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opIdToParallelize = 1;

		Operator result = fixture.getOperatorById(opIdToParallelize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator getOperatorById(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOperatorById_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opIdToParallelize = 1;

		Operator result = fixture.getOperatorById(opIdToParallelize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Operator> getOps() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOps_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		ArrayList<Operator> result = fixture.getOps();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the String getPathToQueryDefinition() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetPathToQueryDefinition_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		String result = fixture.getPathToQueryDefinition();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the RuntimeCommunicationTools getRCT() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetRCT_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		RuntimeCommunicationTools result = fixture.getRCT();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<EndPoint> getStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetStarTopology_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		ArrayList<EndPoint> result = fixture.getStarTopology();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the void incrementBaseId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testIncrementBaseId_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.incrementBaseId();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void init(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testInit_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator op = null;

		fixture.init(op);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void initRuntime(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testInitRuntime_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator op = null;

		fixture.initRuntime(op);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the boolean isSystemRunning() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testIsSystemRunning_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		boolean result = fixture.isSystemRunning();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertTrue(result);
	}

	/**
	 * Run the boolean isSystemRunning() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testIsSystemRunning_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		boolean result = fixture.isSystemRunning();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertTrue(result);
	}

	/**
	 * Run the void loadQuery(QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testLoadQuery_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		QueryPlan qp = new QueryPlan();
		qp.elements = new HashMap();

		fixture.loadQuery(qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.ExceptionInInitializerError
	}

	/**
	 * Run the void loadQuery(QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testLoadQuery_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		QueryPlan qp = new QueryPlan();
		qp.elements = new HashMap();

		fixture.loadQuery(qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NoClassDefFoundError: Could not initialize class uk.ac.imperial.lsds.seep.infrastructure.api.QueryPlan
	}

	/**
	 * Run the void loadQuery(QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testLoadQuery_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		QueryPlan qp = new QueryPlan();
		qp.elements = new HashMap();

		fixture.loadQuery(qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NoClassDefFoundError: Could not initialize class uk.ac.imperial.lsds.seep.infrastructure.api.QueryPlan
	}

	/**
	 * Run the void loadQuery(QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testLoadQuery_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		QueryPlan qp = new QueryPlan();
		qp.elements = new HashMap();

		fixture.loadQuery(qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NoClassDefFoundError: Could not initialize class uk.ac.imperial.lsds.seep.infrastructure.api.QueryPlan
	}

	/**
	 * Run the void loadQuery(QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testLoadQuery_5()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		QueryPlan qp = new QueryPlan();
		qp.elements = new HashMap();

		fixture.loadQuery(qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NoClassDefFoundError: Could not initialize class uk.ac.imperial.lsds.seep.infrastructure.api.QueryPlan
	}

	/**
	 * Run the void loadQuery(QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testLoadQuery_6()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		QueryPlan qp = new QueryPlan();
		qp.elements = new HashMap();

		fixture.loadQuery(qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NoClassDefFoundError: Could not initialize class uk.ac.imperial.lsds.seep.infrastructure.api.QueryPlan
	}

	/**
	 * Run the void parallelRecovery(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParallelRecovery_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		String oldIp_txt = "";

		fixture.parallelRecovery(oldIp_txt);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parallelRecovery(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParallelRecovery_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		String oldIp_txt = "";

		fixture.parallelRecovery(oldIp_txt);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_5()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_6()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_7()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_8()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_9()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_10()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_11()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_12()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_13()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_14()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_15()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void parseFileForNetflix() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testParseFileForNetflix_16()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.parseFileForNetflix();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNew(Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNew_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNew(o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNew(Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNew_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNew(o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNew(Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNew_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNew(o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNew(Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNew_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNew(o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNew(Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNew_5()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNew(o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNew(Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNew_6()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNew(o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNew(Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNew_7()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNew(o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNew(Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNew_8()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNew(o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNewParallelReplica(Operator,Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNewParallelReplica_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator originalOp = null;
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNewParallelReplica(originalOp, o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNewParallelReplica(Operator,Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNewParallelReplica_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator originalOp = null;
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNewParallelReplica(originalOp, o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNewParallelReplica(Operator,Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNewParallelReplica_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator originalOp = null;
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNewParallelReplica(originalOp, o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNewParallelReplica(Operator,Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNewParallelReplica_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator originalOp = null;
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNewParallelReplica(originalOp, o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNewParallelReplica(Operator,Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNewParallelReplica_5()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator originalOp = null;
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNewParallelReplica(originalOp, o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNewParallelReplica(Operator,Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNewParallelReplica_6()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator originalOp = null;
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNewParallelReplica(originalOp, o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNewParallelReplica(Operator,Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNewParallelReplica_7()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator originalOp = null;
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNewParallelReplica(originalOp, o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void placeNewParallelReplica(Operator,Operator,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPlaceNewParallelReplica_8()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator originalOp = null;
		Operator o = null;
		Node n = new Node(1);

		fixture.placeNewParallelReplica(originalOp, o, n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void printCurrentInfrastructure() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPrintCurrentInfrastructure_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.printCurrentInfrastructure();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void printCurrentInfrastructure() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPrintCurrentInfrastructure_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.printCurrentInfrastructure();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void printCurrentInfrastructure() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPrintCurrentInfrastructure_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.printCurrentInfrastructure();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void printCurrentInfrastructure() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testPrintCurrentInfrastructure_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.printCurrentInfrastructure();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reDeploy(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReDeploy_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);

		fixture.reDeploy(n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reDeploy(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReDeploy_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);

		fixture.reDeploy(n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reDeploy(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReDeploy_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);

		fixture.reDeploy(n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reDeploy(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReDeploy_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);

		fixture.reDeploy(n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reDeploy(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReDeploy_5()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);

		fixture.reDeploy(n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reDeploy(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReDeploy_6()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);

		fixture.reDeploy(n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reDeploy(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReDeploy_7()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);

		fixture.reDeploy(n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reDeploy(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReDeploy_8()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);

		fixture.reDeploy(n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reDeploy(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReDeploy_9()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);

		fixture.reDeploy(n);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reMap(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReMap_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.reMap(oldIp, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reMap(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReMap_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.reMap(oldIp, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void reMap(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReMap_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.reMap(oldIp, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void removeNodeFromStarTopology(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testRemoveNodeFromStarTopology_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		fixture.removeNodeFromStarTopology(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void removeNodeFromStarTopology(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testRemoveNodeFromStarTopology_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		fixture.removeNodeFromStarTopology(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void removeNodeFromStarTopology(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testRemoveNodeFromStarTopology_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		int opId = 1;

		fixture.removeNodeFromStarTopology(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void saveResults() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSaveResults_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.saveResults();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void saveResultsSWC() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSaveResultsSWC_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.saveResultsSWC();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void saveResultsSWC() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSaveResultsSWC_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.saveResultsSWC();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void saveResultsSWC() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSaveResultsSWC_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.saveResultsSWC();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void sendCode(Node,byte[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSendCode_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Node n = new Node(1);
		byte[] data = new byte[] {};

		fixture.sendCode(n, data);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void sendCode(Operator,byte[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSendCode_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator op = null;
		byte[] data = new byte[] {};

		fixture.sendCode(op, data);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void setEiu(ElasticInfrastructureUtils) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSetEiu_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		ElasticInfrastructureUtils eiu = new ElasticInfrastructureUtils(new Infrastructure(1));

		fixture.setEiu(eiu);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void setPathToQueryDefinition(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSetPathToQueryDefinition_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		String pathToQueryDefinition = "";

		fixture.setPathToQueryDefinition(pathToQueryDefinition);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void setUp() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSetUp_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.setUp();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void setUp() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSetUp_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.setUp();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void setUp(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSetUp_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator op = null;

		fixture.setUp(op);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void start() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testStart_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.start();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void start() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testStart_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.start();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void startInfrastructure() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testStartInfrastructure_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.startInfrastructure();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void stopWorkers() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testStopWorkers_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.stopWorkers();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void switchMechanisms() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSwitchMechanisms_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.switchMechanisms();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void switchMechanisms() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSwitchMechanisms_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.switchMechanisms();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void switchMechanisms() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSwitchMechanisms_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.switchMechanisms();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void switchMechanisms() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSwitchMechanisms_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);

		fixture.switchMechanisms();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateContextLocations(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateContextLocations_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;

		fixture.updateContextLocations(o);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateContextLocations(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateContextLocations_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;

		fixture.updateContextLocations(o);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateContextLocations(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateContextLocations_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		Operator o = null;

		fixture.updateContextLocations(o);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_1()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_2()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_3()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_4()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_5()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_6()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_7()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_8()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_9()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_10()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_11()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_12()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_13()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_14()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_15()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void updateU_D(InetAddress,InetAddress,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testUpdateU_D_16()
		throws Exception {
		Infrastructure fixture = new Infrastructure(1);
		fixture.setEiu(new ElasticInfrastructureUtils(new Infrastructure(1)));
		fixture.setPathToQueryDefinition("");
		fixture.elements = new HashMap();
		fixture.value = 1;
		fixture.incrementBaseId();
		fixture.getNodeFromPool();
		fixture.addOperator((Operator) null);
		fixture.addSource((Operator) null);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();
		boolean parallelRecovery = true;

		fixture.updateU_D(oldIp, newIp, parallelRecovery);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	protected void setUp()
		throws Exception {
		super.setUp();
		// add additional set up code here
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @see TestCase#tearDown()
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	protected void tearDown()
		throws Exception {
		super.tearDown();
		// Add additional tear down code here
	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(InfrastructureTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new InfrastructureTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}