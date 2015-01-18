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
 * The class <code>BarrierTest</code> contains tests for the class <code>{@link Barrier}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:03
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class BarrierTest extends TestCase {
	/**
	 * Run the Barrier(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testBarrier_1()
		throws Exception {
		int initialNumberOfThreads = 1;

		Barrier result = new Barrier(initialNumberOfThreads);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the DataTuple pull() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testPull_1()
		throws Exception {
		Barrier fixture = new Barrier(1);
		fixture.push(new DataTuple());

		DataTuple result = fixture.pull();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<DataTuple> pull_from_barrier() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testPull_from_barrier_1()
		throws Exception {
		Barrier fixture = new Barrier(1);
		fixture.push(new DataTuple());

		ArrayList<DataTuple> result = fixture.pull_from_barrier();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void push(DataTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testPush_1()
		throws Exception {
		Barrier fixture = new Barrier(1);
		fixture.push(new DataTuple());
		DataTuple dt = new DataTuple();

		fixture.push(dt);

		// add additional test code here
	}

	/**
	 * Run the void reconfigureBarrier(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testReconfigureBarrier_1()
		throws Exception {
		Barrier fixture = new Barrier(1);
		fixture.push(new DataTuple());
		int numThreads = 1;

		fixture.reconfigureBarrier(numThreads);

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
	 * @generatedBy CodePro at 18/10/13 19:03
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
	 * @generatedBy CodePro at 18/10/13 19:03
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
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(BarrierTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new BarrierTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
