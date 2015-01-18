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
import java.nio.channels.Selector;
import uk.ac.imperial.lsds.seep.buffer.OutputBuffer;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.runtimeengine.DisposableCommunicationChannel;
import uk.ac.imperial.lsds.seep.buffer.Buffer;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import java.util.ArrayList;
import java.util.Vector;
import uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.operator.Operator;
import junit.framework.*;

/**
 * The class <code>PUContextTest</code> contains tests for the class <code>{@link PUContext}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:07
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class PUContextTest extends TestCase {
	/**
	 * Run the PUContext(WorkerNodeDescription,ArrayList<EndPoint>) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testPUContext_1()
		throws Exception {
		WorkerNodeDescription nodeDescr = new WorkerNodeDescription(InetAddress.getLocalHost(), 1);
		ArrayList<EndPoint> starTopology = new ArrayList();

		PUContext result = new PUContext(nodeDescr, starTopology);

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getDownstreamTypeConnection());
		assertEquals(null, result.getUpstreamTypeConnection());
		assertEquals(0, result.getStarTopologySize());
	}

	/**
	 * Run the PUContext(WorkerNodeDescription,ArrayList<EndPoint>) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testPUContext_2()
		throws Exception {
		WorkerNodeDescription nodeDescr = new WorkerNodeDescription(InetAddress.getLocalHost(), 1);
		ArrayList<EndPoint> starTopology = new ArrayList();

		PUContext result = new PUContext(nodeDescr, starTopology);

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getDownstreamTypeConnection());
		assertEquals(null, result.getUpstreamTypeConnection());
		assertEquals(0, result.getStarTopologySize());
	}

	/**
	 * Run the void configureNewDownstreamCommunication(int,OperatorStaticInformation) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testConfigureNewDownstreamCommunication_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opID = 1;
		OperatorStaticInformation loc = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		fixture.configureNewDownstreamCommunication(opID, loc);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.configureNewDownstreamCommunication(PUContext.java:173)
	}

	/**
	 * Run the void configureNewDownstreamCommunication(int,OperatorStaticInformation) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testConfigureNewDownstreamCommunication_2()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opID = 1;
		OperatorStaticInformation loc = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		fixture.configureNewDownstreamCommunication(opID, loc);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.configureNewDownstreamCommunication(PUContext.java:173)
	}

	/**
	 * Run the void configureNewDownstreamCommunication(int,OperatorStaticInformation) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testConfigureNewDownstreamCommunication_3()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opID = 1;
		OperatorStaticInformation loc = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		fixture.configureNewDownstreamCommunication(opID, loc);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.configureNewDownstreamCommunication(PUContext.java:173)
	}

	/**
	 * Run the void configureNewUpstreamCommunication(int,OperatorStaticInformation) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testConfigureNewUpstreamCommunication_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opID = 1;
		OperatorStaticInformation loc = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		fixture.configureNewUpstreamCommunication(opID, loc);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.configureNewUpstreamCommunication(PUContext.java:160)
	}

	/**
	 * Run the void configureNewUpstreamCommunication(int,OperatorStaticInformation) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testConfigureNewUpstreamCommunication_2()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opID = 1;
		OperatorStaticInformation loc = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		fixture.configureNewUpstreamCommunication(opID, loc);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.configureNewUpstreamCommunication(PUContext.java:160)
	}

	/**
	 * Run the void configureOperatorConnections(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testConfigureOperatorConnections_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		Operator op = null;

		fixture.configureOperatorConnections(op);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.configureDownstreamAndUpstreamConnections(PUContext.java:138)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.configureOperatorConnections(PUContext.java:150)
	}

	/**
	 * Run the void filterStarTopology(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testFilterStarTopology_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;

		fixture.filterStarTopology(opId);

		// add additional test code here
	}

	/**
	 * Run the void filterStarTopology(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testFilterStarTopology_2()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;

		fixture.filterStarTopology(opId);

		// add additional test code here
	}

	/**
	 * Run the void filterStarTopology(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testFilterStarTopology_3()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;

		fixture.filterStarTopology(opId);

		// add additional test code here
	}

	/**
	 * Run the Buffer getBuffer(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetBuffer_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;

		Buffer result = fixture.getBuffer(opId);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the SynchronousCommunicationChannel getCCIfromOpId(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetCCIfromOpId_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;
		String type = "d";

		SynchronousCommunicationChannel result = fixture.getCCIfromOpId(opId, type);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:287)
		assertNotNull(result);
	}

	/**
	 * Run the SynchronousCommunicationChannel getCCIfromOpId(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetCCIfromOpId_2()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;
		String type = "u";

		SynchronousCommunicationChannel result = fixture.getCCIfromOpId(opId, type);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:294)
		assertNotNull(result);
	}

	/**
	 * Run the SynchronousCommunicationChannel getCCIfromOpId(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetCCIfromOpId_3()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;
		String type = "u";

		SynchronousCommunicationChannel result = fixture.getCCIfromOpId(opId, type);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:294)
		assertNotNull(result);
	}

	/**
	 * Run the SynchronousCommunicationChannel getCCIfromOpId(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetCCIfromOpId_4()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;
		String type = "u";

		SynchronousCommunicationChannel result = fixture.getCCIfromOpId(opId, type);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:294)
		assertNotNull(result);
	}

	/**
	 * Run the SynchronousCommunicationChannel getCCIfromOpId(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetCCIfromOpId_5()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;
		String type = "";

		SynchronousCommunicationChannel result = fixture.getCCIfromOpId(opId, type);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the SynchronousCommunicationChannel getCCIfromOpId(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetCCIfromOpId_6()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;
		String type = "d";

		SynchronousCommunicationChannel result = fixture.getCCIfromOpId(opId, type);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:287)
		assertNotNull(result);
	}

	/**
	 * Run the SynchronousCommunicationChannel getCCIfromOpId(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetCCIfromOpId_7()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;
		String type = "d";

		SynchronousCommunicationChannel result = fixture.getCCIfromOpId(opId, type);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:287)
		assertNotNull(result);
	}

	/**
	 * Run the Selector getConfiguredSelector() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetConfiguredSelector_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		Selector result = fixture.getConfiguredSelector();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the DisposableCommunicationChannel getDCCfromOpIdInStarTopology(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetDCCfromOpIdInStarTopology_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;

		DisposableCommunicationChannel result = fixture.getDCCfromOpIdInStarTopology(opId);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the DisposableCommunicationChannel getDCCfromOpIdInStarTopology(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetDCCfromOpIdInStarTopology_2()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;

		DisposableCommunicationChannel result = fixture.getDCCfromOpIdInStarTopology(opId);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the DisposableCommunicationChannel getDCCfromOpIdInStarTopology(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetDCCfromOpIdInStarTopology_3()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;

		DisposableCommunicationChannel result = fixture.getDCCfromOpIdInStarTopology(opId);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the Vector<EndPoint> getDownstreamTypeConnection() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetDownstreamTypeConnection_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		Vector<EndPoint> result = fixture.getDownstreamTypeConnection();

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the ArrayList<OutputBuffer> getOutputBuffers() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetOutputBuffers_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		ArrayList<OutputBuffer> result = fixture.getOutputBuffers();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getOutputBuffers(PUContext.java:116)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<OutputBuffer> getOutputBuffers() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetOutputBuffers_2()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		ArrayList<OutputBuffer> result = fixture.getOutputBuffers();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getOutputBuffers(PUContext.java:116)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<OutputBuffer> getOutputBuffers() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetOutputBuffers_3()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		ArrayList<OutputBuffer> result = fixture.getOutputBuffers();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getOutputBuffers(PUContext.java:116)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<EndPoint> getStarTopology() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetStarTopology_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		ArrayList<EndPoint> result = fixture.getStarTopology();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the int getStarTopologySize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetStarTopologySize_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		int result = fixture.getStarTopologySize();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the Vector<EndPoint> getUpstreamTypeConnection() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetUpstreamTypeConnection_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		Vector<EndPoint> result = fixture.getUpstreamTypeConnection();

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the boolean isScalingOpDirectDownstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testIsScalingOpDirectDownstream_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;

		boolean result = fixture.isScalingOpDirectDownstream(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.isScalingOpDirectDownstream(PUContext.java:76)
		assertTrue(result);
	}

	/**
	 * Run the boolean isScalingOpDirectDownstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testIsScalingOpDirectDownstream_2()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;

		boolean result = fixture.isScalingOpDirectDownstream(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.isScalingOpDirectDownstream(PUContext.java:76)
		assertTrue(result);
	}

	/**
	 * Run the boolean isScalingOpDirectDownstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testIsScalingOpDirectDownstream_3()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opId = 1;

		boolean result = fixture.isScalingOpDirectDownstream(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.isScalingOpDirectDownstream(PUContext.java:76)
		assertTrue(result);
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_2()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_3()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_4()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_5()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_6()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_7()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_8()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_9()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_10()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_11()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_12()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_13()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_14()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_15()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateConnection(int,Operator,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateConnection_16()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		int opRecId = 1;
		Operator opToReconfigure = null;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.updateConnection(opRecId, opToReconfigure, newIp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.updateConnection(PUContext.java:313)
	}

	/**
	 * Run the void updateStarTopology(ArrayList<EndPoint>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUpdateStarTopology_1()
		throws Exception {
		PUContext fixture = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());
		ArrayList<EndPoint> starTopology = new ArrayList();

		fixture.updateStarTopology(starTopology);

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
	 * @generatedBy CodePro at 18/10/13 19:07
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
	 * @generatedBy CodePro at 18/10/13 19:07
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
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(PUContextTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new PUContextTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
