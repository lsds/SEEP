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
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import java.net.InetAddress;
import java.net.URL;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InvalidateState;
import java.net.URLClassLoader;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.KeyBounds;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateAck;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import java.util.ArrayList;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DistributedScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.operator.Operator;
import java.util.HashMap;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.CloseSignal;
import java.util.Map;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.OpenSignal;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import junit.framework.*;

/**
 * The class <code>CoreRETest</code> contains tests for the class <code>{@link CoreRE}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:01
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class CoreRETest extends TestCase {
	/**
	 * Run the CoreRE(WorkerNodeDescription,RuntimeClassLoader) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testCoreRE_1()
		throws Exception {
		WorkerNodeDescription nodeDescr = new WorkerNodeDescription(InetAddress.getLocalHost(), 1);
		RuntimeClassLoader rcl = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));

		CoreRE result = new CoreRE(nodeDescr, rcl);

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getDSA());
		assertEquals(true, result.killHandlers());
		assertEquals(-1, result.getBackupUpstreamIndex());
		assertEquals(null, result.getControlDispatcher());
		assertEquals(null, result.getInitialStarTopology());
	}

	/**
	 * Run the void ack(TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testAck_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		TimestampTracker tsVector = new TimestampTracker();

		fixture.ack(tsVector);

		// add additional test code here
	}

	/**
	 * Run the void ack(TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testAck_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		TimestampTracker tsVector = new TimestampTracker();

		fixture.ack(tsVector);

		// add additional test code here
	}

	/**
	 * Run the boolean checkSystemStatus() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testCheckSystemStatus_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		boolean result = fixture.checkSystemStatus();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.checkSystemStatus(CoreRE.java:327)
		assertTrue(result);
	}

	/**
	 * Run the boolean checkSystemStatus() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testCheckSystemStatus_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		boolean result = fixture.checkSystemStatus();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.checkSystemStatus(CoreRE.java:327)
		assertTrue(result);
	}

	/**
	 * Run the void configureUpstreamIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testConfigureUpstreamIndex_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int upstreamSize = 0;

		fixture.configureUpstreamIndex(upstreamSize);

		// add additional test code here
	}

	/**
	 * Run the void configureUpstreamIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testConfigureUpstreamIndex_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int upstreamSize = 1;

		fixture.configureUpstreamIndex(upstreamSize);

		// add additional test code here
	}

	/**
	 * Run the void configureUpstreamIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testConfigureUpstreamIndex_3()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int upstreamSize = 1;

		fixture.configureUpstreamIndex(upstreamSize);

		// add additional test code here
	}

	/**
	 * Run the void forwardData(ArrayList<DataTuple>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testForwardData_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ArrayList<DataTuple> data = new ArrayList();

		fixture.forwardData(data);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.forwardData(CoreRE.java:297)
	}

	/**
	 * Run the void forwardData(DataTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testForwardData_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		DataTuple data = new DataTuple();

		fixture.forwardData(data);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.forwardData(CoreRE.java:293)
	}

	/**
	 * Run the int getBackupUpstreamIndex() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetBackupUpstreamIndex_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		int result = fixture.getBackupUpstreamIndex();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the ControlDispatcher getControlDispatcher() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetControlDispatcher_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		ControlDispatcher result = fixture.getControlDispatcher();

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the DataStructureAdapter getDSA() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetDSA_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		DataStructureAdapter result = fixture.getDSA();

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the TimestampTracker getIncomingTT() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetIncomingTT_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		TimestampTracker result = fixture.getIncomingTT();

		// add additional test code here
		assertNotNull(result);
		assertEquals("", result.toString());
	}

	/**
	 * Run the ArrayList<EndPoint> getInitialStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetInitialStarTopology_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		ArrayList<EndPoint> result = fixture.getInitialStarTopology();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the WorkerNodeDescription getNodeDescr() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetNodeDescr_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		WorkerNodeDescription result = fixture.getNodeDescr();

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getOwnPort());
		assertEquals(-273077994, result.getNodeId());
	}

	/**
	 * Run the int getOpIdFromInetAddress(InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetOpIdFromInetAddress_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		InetAddress ip = InetAddress.getLocalHost();

		int result = fixture.getOpIdFromInetAddress(ip);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.getOpIdFromInetAddress(CoreRE.java:314)
		assertEquals(0, result);
	}

	/**
	 * Run the int getOriginalUpstreamFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetOriginalUpstreamFromOpId_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int opId = 1;

		int result = fixture.getOriginalUpstreamFromOpId(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.getOriginalUpstreamFromOpId(CoreRE.java:310)
		assertEquals(0, result);
	}

	/**
	 * Run the RuntimeClassLoader getRuntimeClassLoader() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetRuntimeClassLoader_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		RuntimeClassLoader result = fixture.getRuntimeClassLoader();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the long getTsData(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetTsData_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int stream = 1;

		long result = fixture.getTsData(stream);

		// add additional test code here
		assertEquals(0L, result);
	}

	/**
	 * Run the void initializeCommunications(Map<String,Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testInitializeCommunications_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		Map<String, Integer> tupleIdxMapper = new HashMap();

		fixture.initializeCommunications(tupleIdxMapper);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.initializeCommunications(CoreRE.java:152)
	}

	/**
	 * Run the void initializeCommunications(Map<String,Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testInitializeCommunications_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		Map<String, Integer> tupleIdxMapper = new HashMap();

		fixture.initializeCommunications(tupleIdxMapper);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.initializeCommunications(CoreRE.java:152)
	}

	/**
	 * Run the void initializeCommunications(Map<String,Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testInitializeCommunications_3()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		Map<String, Integer> tupleIdxMapper = new HashMap();

		fixture.initializeCommunications(tupleIdxMapper);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.initializeCommunications(CoreRE.java:152)
	}

	/**
	 * Run the boolean killHandlers() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testKillHandlers_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		boolean result = fixture.killHandlers();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the void manageBackupUpstreamIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testManageBackupUpstreamIndex_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int opId = 1;

		fixture.manageBackupUpstreamIndex(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.manageBackupUpstreamIndex(CoreRE.java:674)
	}

	/**
	 * Run the void manageBackupUpstreamIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testManageBackupUpstreamIndex_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(-1);
		fixture.setTotalNumberOfStateChunks(1);
		int opId = 1;

		fixture.manageBackupUpstreamIndex(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.manageBackupUpstreamIndex(CoreRE.java:674)
	}

	/**
	 * Run the void manageBackupUpstreamIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testManageBackupUpstreamIndex_3()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int opId = 1;

		fixture.manageBackupUpstreamIndex(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.manageBackupUpstreamIndex(CoreRE.java:674)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "replay", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:624)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "system_ready", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:602)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_3()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, 1, "add_upstream", "", 1, 1, 1, true, "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:584)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_4()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, 1, "", "", 1, 1, 1, true, "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.RuntimeException: Operator: ERROR in processCommand
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:633)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_5()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, 1, "add_downstream", "", 1, 1, 1, true, "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:580)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_6()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, 1, "", "", 1, 1, 1, true, "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.RuntimeException: Operator: ERROR in processCommand
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:633)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_7()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "reconfigure_D", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:551)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_8()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "just_reconfigure_D", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:551)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_9()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "just_reconfigure_D", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:551)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_10()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.RuntimeException: Operator: ERROR in processCommand
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:633)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_11()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.RuntimeException: Operator: ERROR in processCommand
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:633)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_12()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.RuntimeException: Operator: ERROR in processCommand
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:633)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_13()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.RuntimeException: Operator: ERROR in processCommand
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:633)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_14()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.RuntimeException: Operator: ERROR in processCommand
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:633)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_15()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ReconfigureConnection rc = new ReconfigureConnection(1, "", "");
		OutputStream os = new ByteArrayOutputStream();

		fixture.processCommand(rc, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.RuntimeException: Operator: ERROR in processCommand
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processCommand(CoreRE.java:633)
	}

	/**
	 * Run the void processCommand(ReconfigureConnection,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessCommand_16()
		throws Exception {
		try {
			CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
			fixture.pushStarTopology(new ArrayList());
			fixture.setBackupUpstreamIndex(1);
			fixture.setTotalNumberOfStateChunks(1);
			ReconfigureConnection rc = new ReconfigureConnection(1, "", "");
			OutputStream os = new ByteArrayOutputStream();

			fixture.processCommand(rc, os);

			// add additional test code here
			fail("The exception java.lang.RuntimeException should have been thrown.");
		} catch (java.lang.RuntimeException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setResume(new Resume());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_3()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setResume(new Resume());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_4()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_5()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_6()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setScaleOutInfo(new ScaleOutInfo());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_7()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setBackupRI(new BackupRI());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_8()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setStateAck(new StateAck());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_9()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setBackupState(new BackupOperatorState());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_10()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setCloseSignal(new CloseSignal());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_11()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setOpenSignal(new OpenSignal());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_12()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setKeyBounds(new KeyBounds());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_13()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setInitOperatorState(new InitOperatorState());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_14()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setInvalidateState(new InvalidateState());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_15()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setAck(new Ack());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void processControlTuple(ControlTuple,OutputStream,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testProcessControlTuple_16()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ct = new ControlTuple(CoreRE.ControlTupleType.ACK, 1, 1L);
		ct.setAck(new Ack());
		OutputStream os = new ByteArrayOutputStream();
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.processControlTuple(ct, os, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.processControlTuple(CoreRE.java:351)
	}

	/**
	 * Run the void pushOperator(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testPushOperator_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		Operator o = null;

		fixture.pushOperator(o);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.pushOperator(CoreRE.java:109)
	}

	/**
	 * Run the void pushOperator(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testPushOperator_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		Operator o = null;

		fixture.pushOperator(o);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.pushOperator(CoreRE.java:109)
	}

	/**
	 * Run the void pushOperator(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testPushOperator_3()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		Operator o = null;

		fixture.pushOperator(o);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.pushOperator(CoreRE.java:109)
	}

	/**
	 * Run the void pushOperator(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testPushOperator_4()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		Operator o = null;

		fixture.pushOperator(o);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.pushOperator(CoreRE.java:109)
	}

	/**
	 * Run the void pushStarTopology(ArrayList<EndPoint>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testPushStarTopology_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ArrayList<EndPoint> starTopology = new ArrayList();

		fixture.pushStarTopology(starTopology);

		// add additional test code here
	}

	/**
	 * Run the void pushStarTopology(ArrayList<EndPoint>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testPushStarTopology_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ArrayList<EndPoint> starTopology = new ArrayList();

		fixture.pushStarTopology(starTopology);

		// add additional test code here
	}

	/**
	 * Run the void reconfigureUpstreamBackupIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testReconfigureUpstreamBackupIndex_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int upstreamSize = 1;

		fixture.reconfigureUpstreamBackupIndex(upstreamSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.reconfigureUpstreamBackupIndex(CoreRE.java:738)
	}

	/**
	 * Run the void reconfigureUpstreamBackupIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testReconfigureUpstreamBackupIndex_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int upstreamSize = 1;

		fixture.reconfigureUpstreamBackupIndex(upstreamSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.reconfigureUpstreamBackupIndex(CoreRE.java:738)
	}

	/**
	 * Run the void reconfigureUpstreamBackupIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testReconfigureUpstreamBackupIndex_3()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int upstreamSize = 1;

		fixture.reconfigureUpstreamBackupIndex(upstreamSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.reconfigureUpstreamBackupIndex(CoreRE.java:738)
	}

	/**
	 * Run the void reconfigureUpstreamBackupIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testReconfigureUpstreamBackupIndex_4()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int upstreamSize = 1;

		fixture.reconfigureUpstreamBackupIndex(upstreamSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.reconfigureUpstreamBackupIndex(CoreRE.java:738)
	}

	/**
	 * Run the void reconfigureUpstreamBackupIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testReconfigureUpstreamBackupIndex_5()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int upstreamSize = 1;

		fixture.reconfigureUpstreamBackupIndex(upstreamSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.reconfigureUpstreamBackupIndex(CoreRE.java:738)
	}

	/**
	 * Run the void reconfigureUpstreamBackupIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testReconfigureUpstreamBackupIndex_6()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int upstreamSize = 1;

		fixture.reconfigureUpstreamBackupIndex(upstreamSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.reconfigureUpstreamBackupIndex(CoreRE.java:738)
	}

	/**
	 * Run the void sendBackupState(ControlTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSendBackupState_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ctB = new ControlTuple();
		ctB.setBackupState(new BackupOperatorState());

		fixture.sendBackupState(ctB);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.sendBackupState(CoreRE.java:689)
	}

	/**
	 * Run the void sendBlindData(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSendBlindData_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		ControlTuple ctB = new ControlTuple();
		int index = 1;

		fixture.sendBlindData(ctB, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.sendBlindData(CoreRE.java:693)
	}

	/**
	 * Run the void setBackupUpstreamIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetBackupUpstreamIndex_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int backupUpstreamIndex = 1;

		fixture.setBackupUpstreamIndex(backupUpstreamIndex);

		// add additional test code here
	}

	/**
	 * Run the void setOpReady(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetOpReady_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int opId = 1;

		fixture.setOpReady(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setOpReady(CoreRE.java:136)
	}

	/**
	 * Run the void setOpReady(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetOpReady_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int opId = 1;

		fixture.setOpReady(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setOpReady(CoreRE.java:136)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_3()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_4()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_5()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_6()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_7()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_8()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_9()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_10()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_11()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_12()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_13()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_14()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_15()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setRuntime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetRuntime_16()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.setRuntime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.setRuntime(CoreRE.java:187)
	}

	/**
	 * Run the void setTotalNumberOfStateChunks(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetTotalNumberOfStateChunks_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int number = 1;

		fixture.setTotalNumberOfStateChunks(number);

		// add additional test code here
	}

	/**
	 * Run the void setTsData(int,long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetTsData_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int stream = 1;
		long ts_data = 1L;

		fixture.setTsData(stream, ts_data);

		// add additional test code here
	}

	/**
	 * Run the void signalCloseBackupSession(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSignalCloseBackupSession_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int starTopologySize = 1;

		fixture.signalCloseBackupSession(starTopologySize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.signalCloseBackupSession(CoreRE.java:663)
	}

	/**
	 * Run the void signalCloseBackupSession(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSignalCloseBackupSession_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int starTopologySize = 0;

		fixture.signalCloseBackupSession(starTopologySize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.signalCloseBackupSession(CoreRE.java:663)
	}

	/**
	 * Run the void signalOpenBackupSession(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSignalOpenBackupSession_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int totalSizeST = 1;

		fixture.signalOpenBackupSession(totalSizeST);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.signalOpenBackupSession(CoreRE.java:651)
	}

	/**
	 * Run the void signalOpenBackupSession(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSignalOpenBackupSession_2()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);
		int totalSizeST = 0;

		fixture.signalOpenBackupSession(totalSizeST);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.signalOpenBackupSession(CoreRE.java:651)
	}

	/**
	 * Run the void startDataProcessing() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testStartDataProcessing_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.startDataProcessing();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.startDataProcessing(CoreRE.java:263)
	}

	/**
	 * Run the void stopDataProcessing() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testStopDataProcessing_1()
		throws Exception {
		CoreRE fixture = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		fixture.pushStarTopology(new ArrayList());
		fixture.setBackupUpstreamIndex(1);
		fixture.setTotalNumberOfStateChunks(1);

		fixture.stopDataProcessing();

		// add additional test code here
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
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
	 * @generatedBy CodePro at 18/10/13 19:01
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
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(CoreRETest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new CoreRETest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
