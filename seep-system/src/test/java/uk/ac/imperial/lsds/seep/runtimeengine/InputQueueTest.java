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

import java.util.ArrayList;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import junit.framework.*;

/**
 * The class <code>InputQueueTest</code> contains tests for the class <code>{@link InputQueue}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:05
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class InputQueueTest extends TestCase {
	/**
	 * Run the InputQueue() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testInputQueue_1()
		throws Exception {

		InputQueue result = new InputQueue();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.InputQueue.<init>(InputQueue.java:26)
		assertNotNull(result);
	}

	/**
	 * Run the InputQueue() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testInputQueue_2()
		throws Exception {
		try {

			InputQueue result = new InputQueue();

			// add additional test code here
			fail("The exception java.lang.NumberFormatException should have been thrown.");
		} catch (java.lang.NumberFormatException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the InputQueue(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testInputQueue_3()
		throws Exception {
		int size = 1;

		InputQueue result = new InputQueue(size);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void clean() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testClean_1()
		throws Exception {
		InputQueue fixture = new InputQueue();

		fixture.clean();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.InputQueue.<init>(InputQueue.java:26)
	}

	/**
	 * Run the void clean() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testClean_2()
		throws Exception {
		InputQueue fixture = new InputQueue();

		fixture.clean();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.InputQueue.<init>(InputQueue.java:26)
	}

	/**
	 * Run the DataTuple pull() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testPull_1()
		throws Exception {
		InputQueue fixture = new InputQueue();

		DataTuple result = fixture.pull();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.InputQueue.<init>(InputQueue.java:26)
		assertNotNull(result);
	}

	/**
	 * Run the DataTuple pull() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testPull_2()
		throws Exception {
		InputQueue fixture = new InputQueue();

		DataTuple result = fixture.pull();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.InputQueue.<init>(InputQueue.java:26)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<DataTuple> pull_from_barrier() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testPull_from_barrier_1()
		throws Exception {
		InputQueue fixture = new InputQueue();

		ArrayList<DataTuple> result = fixture.pull_from_barrier();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.InputQueue.<init>(InputQueue.java:26)
		assertNotNull(result);
	}

	/**
	 * Run the void push(DataTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testPush_1()
		throws Exception {
		InputQueue fixture = new InputQueue();
		DataTuple data = new DataTuple();

		fixture.push(data);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.InputQueue.<init>(InputQueue.java:26)
	}

	/**
	 * Run the void push(DataTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testPush_2()
		throws Exception {
		InputQueue fixture = new InputQueue();
		DataTuple data = new DataTuple();

		fixture.push(data);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.InputQueue.<init>(InputQueue.java:26)
	}

	/**
	 * Run the boolean pushOrShed(DataTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testPushOrShed_1()
		throws Exception {
		InputQueue fixture = new InputQueue();
		DataTuple data = new DataTuple();

		boolean result = fixture.pushOrShed(data);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.InputQueue.<init>(InputQueue.java:26)
		assertTrue(result);
	}

	/**
	 * Run the boolean pushOrShed(DataTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testPushOrShed_2()
		throws Exception {
		InputQueue fixture = new InputQueue();
		DataTuple data = new DataTuple();

		boolean result = fixture.pushOrShed(data);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.InputQueue.<init>(InputQueue.java:26)
		assertTrue(result);
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
			junit.textui.TestRunner.run(InputQueueTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new InputQueueTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
