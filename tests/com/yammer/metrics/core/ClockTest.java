package com.yammer.metrics.core;

import junit.framework.*;

/**
 * The class <code>ClockTest</code> contains tests for the class <code>{@link Clock}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:06
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ClockTest extends TestCase {
	/**
	 * Run the Clock defaultClock() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testDefaultClock_1()
		throws Exception {

		Clock result = Clock.defaultClock();

		// add additional test code here
		assertNotNull(result);
		assertEquals(1382119611569L, result.getTime());
		assertEquals(1397286350154871L, result.getTick());
	}

	/**
	 * Run the long getTime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetTime_1()
		throws Exception {
		Clock fixture = new Clock.CpuTimeClock();

		long result = fixture.getTime();

		// add additional test code here
		assertEquals(1382119610960L, result);
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
			junit.textui.TestRunner.run(ClockTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ClockTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}