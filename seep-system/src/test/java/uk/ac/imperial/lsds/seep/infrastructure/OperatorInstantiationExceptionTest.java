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
package uk.ac.imperial.lsds.seep.infrastructure;

import junit.framework.*;

/**
 * The class <code>OperatorInstantiationExceptionTest</code> contains tests for the class <code>{@link OperatorInstantiationException}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:00
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class OperatorInstantiationExceptionTest extends TestCase {
	/**
	 * Run the OperatorInstantiationException(String) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testOperatorInstantiationException_1()
		throws Exception {
		String msg = "";

		OperatorInstantiationException result = new OperatorInstantiationException(msg);

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getCause());
		assertEquals("uk.ac.imperial.lsds.seep.infrastructure.OperatorInstantiationException: ", result.toString());
		assertEquals("", result.getMessage());
		assertEquals("", result.getLocalizedMessage());
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
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
	 * @generatedBy CodePro at 18/10/13 19:00
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
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(OperatorInstantiationExceptionTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new OperatorInstantiationExceptionTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
