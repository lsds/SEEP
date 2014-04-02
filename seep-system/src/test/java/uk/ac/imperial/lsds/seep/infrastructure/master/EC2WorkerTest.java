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
package uk.ac.imperial.lsds.seep.infrastructure.master;

import junit.framework.*;

/**
 * The class <code>EC2WorkerTest</code> contains tests for the class <code>{@link EC2Worker}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:08
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class EC2WorkerTest extends TestCase {
	/**
	 * Run the EC2Worker(Infrastructure) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testEC2Worker_1()
		throws Exception {
		Infrastructure inf = new Infrastructure(1);

		EC2Worker result = new EC2Worker(inf);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testRun_1()
		throws Exception {
		EC2Worker fixture = new EC2Worker(new Infrastructure(1));

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testRun_2()
		throws Exception {
		EC2Worker fixture = new EC2Worker(new Infrastructure(1));

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testRun_3()
		throws Exception {
		EC2Worker fixture = new EC2Worker(new Infrastructure(1));

		fixture.run();

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
	 * @generatedBy CodePro at 18/10/13 19:08
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
	 * @generatedBy CodePro at 18/10/13 19:08
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
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(EC2WorkerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new EC2WorkerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
