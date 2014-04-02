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

import java.util.Iterator;
import java.util.Map;
import junit.framework.*;

/**
 * The class <code>TimestampTrackerTest</code> contains tests for the class <code>{@link TimestampTracker}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:06
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class TimestampTrackerTest extends TestCase {
	/**
	 * Run the TimestampTracker() constructor test.
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testTimestampTracker_1()
		throws Exception {
		TimestampTracker result = new TimestampTracker();
		assertNotNull(result);
		// add additional test code here
	}

	/**
	 * Run the long get(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGet_1()
		throws Exception {
		TimestampTracker fixture = new TimestampTracker();
		int stream = 1;

		long result = fixture.get(stream);

		// add additional test code here
		assertEquals(0L, result);
	}

	/**
	 * Run the long get(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGet_2()
		throws Exception {
		TimestampTracker fixture = new TimestampTracker();
		int stream = 1;

		long result = fixture.get(stream);

		// add additional test code here
		assertEquals(0L, result);
	}

	/**
	 * Run the Iterator<java.util.Map.Entry<Integer, Long>> getTsStream() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetTsStream_1()
		throws Exception {
		TimestampTracker fixture = new TimestampTracker();

		Iterator<java.util.Map.Entry<Integer, Long>> result = fixture.getTsStream();

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.hasNext());
	}

	/**
	 * Run the boolean isSmallerOrEqual(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testIsSmallerOrEqual_1()
		throws Exception {
		TimestampTracker a = null;
		TimestampTracker b = new TimestampTracker();

		boolean result = TimestampTracker.isSmallerOrEqual(a, b);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean isSmallerOrEqual(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testIsSmallerOrEqual_2()
		throws Exception {
		TimestampTracker a = new TimestampTracker();
		TimestampTracker b = null;

		boolean result = TimestampTracker.isSmallerOrEqual(a, b);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean isSmallerOrEqual(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testIsSmallerOrEqual_3()
		throws Exception {
		TimestampTracker a = new TimestampTracker();
		TimestampTracker b = new TimestampTracker();

		boolean result = TimestampTracker.isSmallerOrEqual(a, b);

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isSmallerOrEqual(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testIsSmallerOrEqual_4()
		throws Exception {
		TimestampTracker a = new TimestampTracker();
		TimestampTracker b = new TimestampTracker();

		boolean result = TimestampTracker.isSmallerOrEqual(a, b);

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isSmallerOrEqual(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testIsSmallerOrEqual_5()
		throws Exception {
		TimestampTracker a = new TimestampTracker();
		TimestampTracker b = new TimestampTracker();

		boolean result = TimestampTracker.isSmallerOrEqual(a, b);

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isSmallerOrEqual(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testIsSmallerOrEqual_6()
		throws Exception {
		TimestampTracker a = new TimestampTracker();
		TimestampTracker b = new TimestampTracker();

		boolean result = TimestampTracker.isSmallerOrEqual(a, b);

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the TimestampTracker returnSmaller(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReturnSmaller_1()
		throws Exception {
		TimestampTracker a = null;
		TimestampTracker b = new TimestampTracker();

		TimestampTracker result = TimestampTracker.returnSmaller(a, b);

		// add additional test code here
		assertNotNull(result);
		assertEquals("", result.toString());
	}

	/**
	 * Run the TimestampTracker returnSmaller(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReturnSmaller_2()
		throws Exception {
		TimestampTracker a = new TimestampTracker();
		TimestampTracker b = null;

		TimestampTracker result = TimestampTracker.returnSmaller(a, b);

		// add additional test code here
		assertNotNull(result);
		assertEquals("", result.toString());
	}

	/**
	 * Run the TimestampTracker returnSmaller(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReturnSmaller_3()
		throws Exception {
		TimestampTracker a = new TimestampTracker();
		TimestampTracker b = new TimestampTracker();

		TimestampTracker result = TimestampTracker.returnSmaller(a, b);

		// add additional test code here
		assertNotNull(result);
		assertEquals("", result.toString());
	}

	/**
	 * Run the TimestampTracker returnSmaller(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReturnSmaller_4()
		throws Exception {
		TimestampTracker a = new TimestampTracker();
		TimestampTracker b = new TimestampTracker();

		TimestampTracker result = TimestampTracker.returnSmaller(a, b);

		// add additional test code here
		assertNotNull(result);
		assertEquals("", result.toString());
	}

	/**
	 * Run the TimestampTracker returnSmaller(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReturnSmaller_5()
		throws Exception {
		TimestampTracker a = new TimestampTracker();
		TimestampTracker b = new TimestampTracker();

		TimestampTracker result = TimestampTracker.returnSmaller(a, b);

		// add additional test code here
		assertNotNull(result);
		assertEquals("", result.toString());
	}

	/**
	 * Run the TimestampTracker returnSmaller(TimestampTracker,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testReturnSmaller_6()
		throws Exception {
		TimestampTracker a = new TimestampTracker();
		TimestampTracker b = new TimestampTracker();

		TimestampTracker result = TimestampTracker.returnSmaller(a, b);

		// add additional test code here
		assertNotNull(result);
		assertEquals("", result.toString());
	}

	/**
	 * Run the void set(int,long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSet_1()
		throws Exception {
		TimestampTracker fixture = new TimestampTracker();
		int stream = 1;
		long ts = 1L;

		fixture.set(stream, ts);

		// add additional test code here
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testToString_1()
		throws Exception {
		TimestampTracker fixture = new TimestampTracker();

		String result = fixture.toString();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testToString_2()
		throws Exception {
		TimestampTracker fixture = new TimestampTracker();

		String result = fixture.toString();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
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
	 * @generatedBy CodePro at 18/10/13 19:06
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
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(TimestampTrackerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new TimestampTrackerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
