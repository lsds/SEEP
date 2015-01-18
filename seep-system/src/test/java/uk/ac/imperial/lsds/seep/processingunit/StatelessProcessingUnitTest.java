/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.processingunit;

import java.net.InetAddress;
import java.net.URL;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.runtimeengine.OutputQueue;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import java.net.URLClassLoader;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import java.util.ArrayList;
import java.util.Map;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import junit.framework.*;
import uk.ac.imperial.lsds.seep.operator.Operator;

/**
 * The class <code>StatelessProcessingUnitTest</code> contains tests for the class <code>{@link StatelessProcessingUnit}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:05
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class StatelessProcessingUnitTest extends TestCase {
	/**
	 * Run the StatelessProcessingUnit(CoreRE,boolean) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testStatelessProcessingUnit_1()
		throws Exception {
		CoreRE owner = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		owner.pushStarTopology(new ArrayList());
		boolean multiCoreEnabled = true;

		StatelessProcessingUnit result = new StatelessProcessingUnit(owner, multiCoreEnabled);

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getOperator());
		assertEquals(false, result.isNodeStateful());
		assertEquals(true, result.isMultiCoreEnabled());
	}

	/**
	 * Run the void addDownstream(int,OperatorStaticInformation) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testAddDownstream_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;
		OperatorStaticInformation location = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		fixture.addDownstream(opId, location);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.addDownstream(StatelessProcessingUnit.java:62)
	}

	/**
	 * Run the void addUpstream(int,OperatorStaticInformation) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testAddUpstream_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;
		OperatorStaticInformation location = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		fixture.addUpstream(opId, location);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.addUpstream(StatelessProcessingUnit.java:71)
	}

	/**
	 * Run the void createAndRunAckWorker() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testCreateAndRunAckWorker_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		fixture.createAndRunAckWorker();

		// add additional test code here
	}

	/**
	 * Run the Map<String, Integer> createTupleAttributeMapper() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testCreateTupleAttributeMapper_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		Map<String, Integer> result = fixture.createTupleAttributeMapper();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.createTupleAttributeMapper(StatelessProcessingUnit.java:80)
		assertNotNull(result);
	}

	/**
	 * Run the Map<String, Integer> createTupleAttributeMapper() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testCreateTupleAttributeMapper_2()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		Map<String, Integer> result = fixture.createTupleAttributeMapper();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.createTupleAttributeMapper(StatelessProcessingUnit.java:80)
		assertNotNull(result);
	}

	/**
	 * Run the Map<String, Integer> createTupleAttributeMapper() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testCreateTupleAttributeMapper_3()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		Map<String, Integer> result = fixture.createTupleAttributeMapper();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.createTupleAttributeMapper(StatelessProcessingUnit.java:80)
		assertNotNull(result);
	}

	/**
	 * Run the void disableMultiCoreSupport() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testDisableMultiCoreSupport_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		fixture.disableMultiCoreSupport();

		// add additional test code here
	}

	/**
	 * Run the void emitACK(TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testEmitACK_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		TimestampTracker currentTs = new TimestampTracker();

		fixture.emitACK(currentTs);

		// add additional test code here
	}

	/**
	 * Run the TimestampTracker getLastACK() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetLastACK_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		TimestampTracker result = fixture.getLastACK();

		// add additional test code here
		assertNotNull(result);
		assertEquals("", result.toString());
	}

	/**
	 * Run the int getOpIdFromUpstreamIp(InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetOpIdFromUpstreamIp_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		InetAddress ip = InetAddress.getLocalHost();

		int result = fixture.getOpIdFromUpstreamIp(ip);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.getOpIdFromUpstreamIp(StatelessProcessingUnit.java:316)
		assertEquals(0, result);
	}

	/**
	 * Run the Operator getOperator() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetOperator_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		Operator result = fixture.getOperator();

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the int getOriginalUpstreamFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetOriginalUpstreamFromOpId_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		int result = fixture.getOriginalUpstreamFromOpId(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.getOriginalUpstreamFromOpId(StatelessProcessingUnit.java:146)
		assertEquals(0, result);
	}

	/**
	 * Run the CoreRE getOwner() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetOwner_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		CoreRE result = fixture.getOwner();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getDSA());
		assertEquals(-1, result.getBackupUpstreamIndex());
		assertEquals(true, result.killHandlers());
		assertEquals(null, result.getControlDispatcher());
	}

	/**
	 * Run the ArrayList<Integer> getRouterIndexesInformation(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetRouterIndexesInformation_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		ArrayList<Integer> result = fixture.getRouterIndexesInformation(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.getRouterIndexesInformation(StatelessProcessingUnit.java:307)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Integer> getRouterKeysInformation(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetRouterKeysInformation_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		ArrayList<Integer> result = fixture.getRouterKeysInformation(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.getRouterKeysInformation(StatelessProcessingUnit.java:311)
		assertNotNull(result);
	}

	/**
	 * Run the IProcessingUnit.SystemStatus getSystemStatus() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetSystemStatus_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		IProcessingUnit.SystemStatus result = fixture.getSystemStatus();

		// add additional test code here
		assertNotNull(result);
		assertEquals("INITIALISING_STATE", result.name());
		assertEquals("INITIALISING_STATE", result.toString());
		assertEquals(2, result.ordinal());
	}

	/**
	 * Run the void initOperator() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testInitOperator_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		fixture.initOperator();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.initOperator(StatelessProcessingUnit.java:113)
	}

	/**
	 * Run the void invalidateState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testInvalidateState_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		fixture.invalidateState(opId);

		// add additional test code here
	}

	/**
	 * Run the void invalidateState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testInvalidateState_2()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		fixture.invalidateState(opId);

		// add additional test code here
	}

	/**
	 * Run the void invalidateState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testInvalidateState_3()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		fixture.invalidateState(opId);

		// add additional test code here
	}

	/**
	 * Run the void invalidateState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testInvalidateState_4()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		fixture.invalidateState(opId);

		// add additional test code here
	}

	/**
	 * Run the boolean isManagingStateOf(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testIsManagingStateOf_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		boolean result = fixture.isManagingStateOf(opId);

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isManagingStateOf(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testIsManagingStateOf_2()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		boolean result = fixture.isManagingStateOf(opId);

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isMultiCoreEnabled() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testIsMultiCoreEnabled_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		boolean result = fixture.isMultiCoreEnabled();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isMultiCoreEnabled() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testIsMultiCoreEnabled_2()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, false);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		boolean result = fixture.isMultiCoreEnabled();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean isNodeStateful() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testIsNodeStateful_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		boolean result = fixture.isNodeStateful();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean isOperatorReady() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testIsOperatorReady_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		boolean result = fixture.isOperatorReady();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.isOperatorReady(StatelessProcessingUnit.java:151)
		assertTrue(result);
	}

	/**
	 * Run the boolean isOperatorReady() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testIsOperatorReady_2()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		boolean result = fixture.isOperatorReady();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.isOperatorReady(StatelessProcessingUnit.java:151)
		assertTrue(result);
	}

	/**
	 * Run the void launchMultiCoreMechanism(CoreRE,DataStructureAdapter) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testLaunchMultiCoreMechanism_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		CoreRE core = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		DataStructureAdapter dsa = new DataStructureAdapter();

		fixture.launchMultiCoreMechanism(core, dsa);

		// add additional test code here
	}

	/**
	 * Run the void launchMultiCoreMechanism(CoreRE,DataStructureAdapter) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testLaunchMultiCoreMechanism_2()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		CoreRE core = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		DataStructureAdapter dsa = new DataStructureAdapter();

		fixture.launchMultiCoreMechanism(core, dsa);

		// add additional test code here
	}

	/**
	 * Run the void launchMultiCoreMechanism(CoreRE,DataStructureAdapter) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testLaunchMultiCoreMechanism_3()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		CoreRE core = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		DataStructureAdapter dsa = new DataStructureAdapter();

		fixture.launchMultiCoreMechanism(core, dsa);

		// add additional test code here
	}

	/**
	 * Run the void launchMultiCoreMechanism(CoreRE,DataStructureAdapter) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testLaunchMultiCoreMechanism_4()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		CoreRE core = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		DataStructureAdapter dsa = new DataStructureAdapter();

		fixture.launchMultiCoreMechanism(core, dsa);

		// add additional test code here
	}

	/**
	 * Run the void newOperatorInstantiation(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testNewOperatorInstantiation_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		Operator o = null;

		fixture.newOperatorInstantiation(o);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.newOperatorInstantiation(StatelessProcessingUnit.java:164)
	}

	/**
	 * Run the void newOperatorInstantiation(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testNewOperatorInstantiation_2()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		Operator o = null;

		fixture.newOperatorInstantiation(o);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.newOperatorInstantiation(StatelessProcessingUnit.java:164)
	}

	/**
	 * Run the void processData(ArrayList<DataTuple>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testProcessData_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		ArrayList<DataTuple> data = new ArrayList();

		fixture.processData(data);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.processData(StatelessProcessingUnit.java:180)
	}

	/**
	 * Run the void processData(DataTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testProcessData_2()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		DataTuple data = new DataTuple();

		fixture.processData(data);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.processData(StatelessProcessingUnit.java:173)
	}

	/**
	 * Run the void reconfigureOperatorConnection(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testReconfigureOperatorConnection_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;
		InetAddress ip = InetAddress.getLocalHost();

		fixture.reconfigureOperatorConnection(opId, ip);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.reconfigureOperatorConnection(StatelessProcessingUnit.java:185)
	}

	/**
	 * Run the void reconfigureOperatorLocation(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testReconfigureOperatorLocation_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;
		InetAddress ip = InetAddress.getLocalHost();

		fixture.reconfigureOperatorLocation(opId, ip);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.reconfigureOperatorLocation(StatelessProcessingUnit.java:190)
	}

	/**
	 * Run the void registerManagedState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testRegisterManagedState_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		fixture.registerManagedState(opId);

		// add additional test code here
	}

	/**
	 * Run the void registerManagedState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testRegisterManagedState_2()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		fixture.registerManagedState(opId);

		// add additional test code here
	}

	/**
	 * Run the void sendData(DataTuple,ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSendData_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		DataTuple dt = new DataTuple();
		ArrayList targets = new ArrayList();
		targets.add(new Integer(-1));

		fixture.sendData(dt, targets);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.sendData(StatelessProcessingUnit.java:208)
	}

	/**
	 * Run the void sendData(DataTuple,ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSendData_2()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		DataTuple dt = new DataTuple();
		ArrayList targets = new ArrayList();
		targets.add(new Integer(-1));

		fixture.sendData(dt, targets);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.sendData(StatelessProcessingUnit.java:208)
	}

	/**
	 * Run the void sendData(DataTuple,ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSendData_3()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		DataTuple dt = new DataTuple();
		ArrayList targets = new ArrayList();
		targets.add(new Integer(-1));

		fixture.sendData(dt, targets);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.sendData(StatelessProcessingUnit.java:208)
	}

	/**
	 * Run the void sendData(DataTuple,ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSendData_4()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		DataTuple dt = new DataTuple();
		ArrayList targets = new ArrayList();
		targets.add(new Integer(-1));

		fixture.sendData(dt, targets);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.sendData(StatelessProcessingUnit.java:208)
	}

	/**
	 * Run the void sendData(DataTuple,ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSendData_5()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		DataTuple dt = new DataTuple();
		ArrayList<Integer> targets = new ArrayList();

		fixture.sendData(dt, targets);

		// add additional test code here
	}

	/**
	 * Run the void setOpReady(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSetOpReady_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		fixture.setOpReady(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.setOpReady(StatelessProcessingUnit.java:235)
	}

	/**
	 * Run the void setOutputQueue(OutputQueue) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSetOutputQueue_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		OutputQueue outputQueue = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.setOutputQueue(outputQueue);

		// add additional test code here
	}

	/**
	 * Run the void setSystemStatus(SystemStatus) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSetSystemStatus_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		IProcessingUnit.SystemStatus systemStatus = IProcessingUnit.SystemStatus.INITIALISING_STATE;

		fixture.setSystemStatus(systemStatus);

		// add additional test code here
	}

	/**
	 * Run the PUContext setUpRemoteConnections() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSetUpRemoteConnections_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		PUContext result = fixture.setUpRemoteConnections();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.configureDownstreamAndUpstreamConnections(PUContext.java:138)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.configureOperatorConnections(PUContext.java:150)
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.setUpRemoteConnections(StatelessProcessingUnit.java:250)
		assertNotNull(result);
	}

	/**
	 * Run the void startDataProcessing() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testStartDataProcessing_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);

		fixture.startDataProcessing();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.startDataProcessing(StatelessProcessingUnit.java:259)
	}

	/**
	 * Run the void stopConnection(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testStopConnection_1()
		throws Exception {
		CoreRE coreRE = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		coreRE.pushStarTopology(new ArrayList());
		StatelessProcessingUnit fixture = new StatelessProcessingUnit(coreRE, true);
		fixture.setSystemStatus(IProcessingUnit.SystemStatus.INITIALISING_STATE);
		fixture.setOutputQueue(new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})))));
		fixture.registerManagedState(1);
		int opId = 1;

		fixture.stopConnection(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:287)
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingUnit.stopConnection(StatelessProcessingUnit.java:268)
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
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
	 * @generatedBy CodePro at 18/10/13 19:05
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
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(StatelessProcessingUnitTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new StatelessProcessingUnitTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
