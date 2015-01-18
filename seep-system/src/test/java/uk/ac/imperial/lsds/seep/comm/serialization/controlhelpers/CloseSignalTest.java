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
package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import junit.framework.*;

/**
 * The class <code>CloseSignalTest</code> contains tests for the class <code>{@link CloseSignal}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:08
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class CloseSignalTest extends TestCase {
	/**
	 * Run the CloseSignal() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testCloseSignal_1()
		throws Exception {

		CloseSignal result = new CloseSignal();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getTotalNumberOfChunks());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the CloseSignal(int,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testCloseSignal_2()
		throws Exception {
		int opId = 1;
		int totalNumberOfChunks = 1;

		CloseSignal result = new CloseSignal(opId, totalNumberOfChunks);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getTotalNumberOfChunks());
		assertEquals(1, result.getOpId());
	}

	/**
	 * Run the int getOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetOpId_1()
		throws Exception {
		CloseSignal fixture = new CloseSignal(1, 1);

		int result = fixture.getOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getTotalNumberOfChunks() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetTotalNumberOfChunks_1()
		throws Exception {
		CloseSignal fixture = new CloseSignal(1, 1);

		int result = fixture.getTotalNumberOfChunks();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the void setOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetOpId_1()
		throws Exception {
		CloseSignal fixture = new CloseSignal(1, 1);
		int opId = 1;

		fixture.setOpId(opId);

		// add additional test code here
	}

	/**
	 * Run the void setTotalNumberOfChunks(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetTotalNumberOfChunks_1()
		throws Exception {
		CloseSignal fixture = new CloseSignal(1, 1);
		int totalNumberOfChunks = 1;

		fixture.setTotalNumberOfChunks(totalNumberOfChunks);

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
			junit.textui.TestRunner.run(CloseSignalTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new CloseSignalTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
