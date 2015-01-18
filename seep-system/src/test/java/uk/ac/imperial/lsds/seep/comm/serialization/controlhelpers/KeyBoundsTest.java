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
 * The class <code>KeyBoundsTest</code> contains tests for the class <code>{@link KeyBounds}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:08
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class KeyBoundsTest extends TestCase {
	/**
	 * Run the KeyBounds() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testKeyBounds_1()
		throws Exception {

		KeyBounds result = new KeyBounds();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getMaxBound());
		assertEquals(0, result.getMinBound());
	}

	/**
	 * Run the KeyBounds(int,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testKeyBounds_2()
		throws Exception {
		int minBound = 1;
		int maxBound = 1;

		KeyBounds result = new KeyBounds(minBound, maxBound);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getMaxBound());
		assertEquals(1, result.getMinBound());
	}

	/**
	 * Run the int getMaxBound() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetMaxBound_1()
		throws Exception {
		KeyBounds fixture = new KeyBounds(1, 1);

		int result = fixture.getMaxBound();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getMinBound() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetMinBound_1()
		throws Exception {
		KeyBounds fixture = new KeyBounds(1, 1);

		int result = fixture.getMinBound();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the void setMaxBound(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetMaxBound_1()
		throws Exception {
		KeyBounds fixture = new KeyBounds(1, 1);
		int maxBound = 1;

		fixture.setMaxBound(maxBound);

		// add additional test code here
	}

	/**
	 * Run the void setMinBound(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetMinBound_1()
		throws Exception {
		KeyBounds fixture = new KeyBounds(1, 1);
		int minBound = 1;

		fixture.setMinBound(minBound);

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
			junit.textui.TestRunner.run(KeyBoundsTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new KeyBoundsTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
