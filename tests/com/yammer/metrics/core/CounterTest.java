package com.yammer.metrics.core;

import junit.framework.*;

/**
 * The class <code>CounterTest</code> contains tests for the class <code>{@link Counter}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:02
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class CounterTest extends TestCase {
	/**
	 * Run the Counter() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testCounter_1()
		throws Exception {

		Counter result = new Counter();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0L, result.getCount());
	}

	/**
	 * Run the void clear() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testClear_1()
		throws Exception {
		Counter fixture = new Counter();

		fixture.clear();

		// add additional test code here
	}

	/**
	 * Run the void dec() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testDec_1()
		throws Exception {
		Counter fixture = new Counter();

		fixture.dec();

		// add additional test code here
	}

	/**
	 * Run the void dec(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testDec_2()
		throws Exception {
		Counter fixture = new Counter();
		long n = 1L;

		fixture.dec(n);

		// add additional test code here
	}

	/**
	 * Run the long getCount() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testGetCount_1()
		throws Exception {
		Counter fixture = new Counter();

		long result = fixture.getCount();

		// add additional test code here
		assertEquals(0L, result);
	}

	/**
	 * Run the void inc() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testInc_1()
		throws Exception {
		Counter fixture = new Counter();

		fixture.inc();

		// add additional test code here
	}

	/**
	 * Run the void inc(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testInc_2()
		throws Exception {
		Counter fixture = new Counter();
		long n = 1L;

		fixture.inc(n);

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
	 * @generatedBy CodePro at 18/10/13 19:02
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
	 * @generatedBy CodePro at 18/10/13 19:02
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
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(CounterTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new CounterTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}