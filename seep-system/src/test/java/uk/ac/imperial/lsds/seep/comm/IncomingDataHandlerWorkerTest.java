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
package uk.ac.imperial.lsds.seep.comm;

import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import java.util.Map;
import junit.framework.*;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;

/**
 * The class <code>IncomingDataHandlerWorkerTest</code> contains tests for the class <code>{@link IncomingDataHandlerWorker}</code>.
 *
 * @generatedBy CodePro at 18/10/13 18:59
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class IncomingDataHandlerWorkerTest extends TestCase {
	/**
	 * Run the IncomingDataHandlerWorker(Socket,CoreRE,Map<String,Integer>,DataStructureAdapter) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testIncomingDataHandlerWorker_1()
		throws Exception {
		Socket upstreamSocket = new Socket();
		CoreRE owner = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		Map<String, Integer> idxMapper = new HashMap();
		DataStructureAdapter dsa = new DataStructureAdapter();

		IncomingDataHandlerWorker result = new IncomingDataHandlerWorker(upstreamSocket, owner, idxMapper, dsa);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testRun_1()
		throws Exception {
		IncomingDataHandlerWorker fixture = new IncomingDataHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), new HashMap(), new DataStructureAdapter());

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.IncomingDataHandlerWorker.run(IncomingDataHandlerWorker.java:92)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testRun_2()
		throws Exception {
		IncomingDataHandlerWorker fixture = new IncomingDataHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), new HashMap(), new DataStructureAdapter());

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.IncomingDataHandlerWorker.run(IncomingDataHandlerWorker.java:92)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testRun_3()
		throws Exception {
		IncomingDataHandlerWorker fixture = new IncomingDataHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), new HashMap(), new DataStructureAdapter());

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.IncomingDataHandlerWorker.run(IncomingDataHandlerWorker.java:92)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testRun_4()
		throws Exception {
		IncomingDataHandlerWorker fixture = new IncomingDataHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), new HashMap(), new DataStructureAdapter());

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.IncomingDataHandlerWorker.run(IncomingDataHandlerWorker.java:92)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testRun_5()
		throws Exception {
		IncomingDataHandlerWorker fixture = new IncomingDataHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), new HashMap(), new DataStructureAdapter());

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.IncomingDataHandlerWorker.run(IncomingDataHandlerWorker.java:92)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testRun_6()
		throws Exception {
		IncomingDataHandlerWorker fixture = new IncomingDataHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), new HashMap(), new DataStructureAdapter());

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.IncomingDataHandlerWorker.run(IncomingDataHandlerWorker.java:92)
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
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
	 * @generatedBy CodePro at 18/10/13 18:59
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
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(IncomingDataHandlerWorkerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new IncomingDataHandlerWorkerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
