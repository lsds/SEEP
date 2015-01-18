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
package uk.ac.imperial.lsds.seep.reliable;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import com.esotericsoftware.kryo.KryoException;
import junit.framework.*;

/**
 * The class <code>StreamerWorkerTest</code> contains tests for the class <code>{@link StreamerWorker}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:04
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class StreamerWorkerTest extends TestCase {
	/**
	 * Run the StreamerWorker(Socket,ArrayBlockingQueue<Object>,int,int,int,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testStreamerWorker_1()
		throws Exception {
		Socket s = new Socket();
		ArrayBlockingQueue<Object> jobQueue = new ArrayBlockingQueue(1);
		int opId = 1;
		int keeperOpId = 1;
		int currentNumberBatch = 1;
		int totalNumberChunks = 1;

		StreamerWorker result = new StreamerWorker(s, jobQueue, opId, keeperOpId, currentNumberBatch, totalNumberChunks);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the StreamerWorker(Socket,ArrayBlockingQueue<Object>,int,int,int,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testStreamerWorker_2()
		throws Exception {
		Socket s = new Socket();
		ArrayBlockingQueue<Object> jobQueue = new ArrayBlockingQueue(1);
		int opId = 1;
		int keeperOpId = 1;
		int currentNumberBatch = 1;
		int totalNumberChunks = 1;

		StreamerWorker result = new StreamerWorker(s, jobQueue, opId, keeperOpId, currentNumberBatch, totalNumberChunks);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void initializeSerialization() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testInitializeSerialization_1()
		throws Exception {
		StreamerWorker fixture = new StreamerWorker(new Socket(), new ArrayBlockingQueue(1), 1, 1, 1, 1);

		fixture.initializeSerialization();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testRun_1()
		throws Exception {
		StreamerWorker fixture = new StreamerWorker(new Socket(), new ArrayBlockingQueue(1), 1, 1, 1, 1);

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testRun_2()
		throws Exception {
		StreamerWorker fixture = new StreamerWorker(new Socket(), new ArrayBlockingQueue(1), 1, 1, 1, 1);

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testRun_3()
		throws Exception {
		StreamerWorker fixture = new StreamerWorker(new Socket(), new ArrayBlockingQueue(1), 1, 1, 1, 1);

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testRun_4()
		throws Exception {
		StreamerWorker fixture = new StreamerWorker(new Socket(), new ArrayBlockingQueue(1), 1, 1, 1, 1);

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testRun_5()
		throws Exception {
		StreamerWorker fixture = new StreamerWorker(new Socket(), new ArrayBlockingQueue(1), 1, 1, 1, 1);

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testRun_6()
		throws Exception {
		try {
			StreamerWorker fixture = new StreamerWorker(new Socket(), new ArrayBlockingQueue(1), 1, 1, 1, 1);

			fixture.run();

			// add additional test code here
			fail("The exception com.esotericsoftware.kryo.KryoException should have been thrown.");
		} catch (com.esotericsoftware.kryo.KryoException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
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
	 * @generatedBy CodePro at 18/10/13 19:04
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
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(StreamerWorkerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new StreamerWorkerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
