package com.yammer.metrics.stats;

import junit.framework.*;

/**
 * The class <code>ThreadLocalRandomTest</code> contains tests for the class <code>{@link ThreadLocalRandom}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:13
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ThreadLocalRandomTest extends TestCase {
	/**
	 * Run the ThreadLocalRandom() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testThreadLocalRandom_1()
		throws Exception {

		ThreadLocalRandom result = new ThreadLocalRandom();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the ThreadLocalRandom current() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testCurrent_1()
		throws Exception {

		ThreadLocalRandom result = ThreadLocalRandom.current();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the int next(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNext_1()
		throws Exception {
		ThreadLocalRandom fixture = ThreadLocalRandom.current();
		fixture.initialized = true;
		int bits = 1;

		int result = fixture.next(bits);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the double nextDouble(double) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextDouble_1()
		throws Exception {
		ThreadLocalRandom fixture = ThreadLocalRandom.current();
		fixture.initialized = true;
		double n = 1.0;

		double result = fixture.nextDouble(n);

		// add additional test code here
		assertEquals(0.8771877060470772, result, 0.1);
	}

	/**
	 * Run the double nextDouble(double) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextDouble_2()
		throws Exception {
		try {
			ThreadLocalRandom fixture = ThreadLocalRandom.current();
			fixture.initialized = true;
			double n = -4.9E-324;

			double result = fixture.nextDouble(n);

			// add additional test code here
			fail("The exception java.lang.IllegalArgumentException should have been thrown.");
		} catch (java.lang.IllegalArgumentException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the double nextDouble(double,double) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextDouble_3()
		throws Exception {
		ThreadLocalRandom fixture = ThreadLocalRandom.current();
		fixture.initialized = true;
		double least = 1.0;
		double bound = Double.POSITIVE_INFINITY;

		double result = fixture.nextDouble(least, bound);

		// add additional test code here
		assertEquals(Double.POSITIVE_INFINITY, result, 0.1);
	}

	/**
	 * Run the double nextDouble(double,double) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextDouble_4()
		throws Exception {
		try {
			ThreadLocalRandom fixture = ThreadLocalRandom.current();
			fixture.initialized = true;
			double least = Double.MAX_VALUE;
			double bound = 1.0;

			double result = fixture.nextDouble(least, bound);

			// add additional test code here
			fail("The exception java.lang.IllegalArgumentException should have been thrown.");
		} catch (java.lang.IllegalArgumentException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the int nextInt(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextInt_1()
		throws Exception {
		ThreadLocalRandom fixture = ThreadLocalRandom.current();
		fixture.initialized = true;
		int least = 1;
		int bound = 1;

		int result = fixture.nextInt(least, bound);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IllegalArgumentException
		//       at com.yammer.metrics.stats.ThreadLocalRandom.nextInt(ThreadLocalRandom.java:97)
		assertEquals(0, result);
	}

	/**
	 * Run the int nextInt(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextInt_2()
		throws Exception {
		try {
			ThreadLocalRandom fixture = ThreadLocalRandom.current();
			fixture.initialized = true;
			int least = 1;
			int bound = 1;

			int result = fixture.nextInt(least, bound);

			// add additional test code here
			fail("The exception java.lang.IllegalArgumentException should have been thrown.");
		} catch (java.lang.IllegalArgumentException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the long nextLong(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextLong_1()
		throws Exception {
		ThreadLocalRandom fixture = ThreadLocalRandom.current();
		fixture.initialized = true;
		long n = 2147483647L;

		long result = fixture.nextLong(n);

		// add additional test code here
		assertEquals(244376621L, result);
	}

	/**
	 * Run the long nextLong(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextLong_2()
		throws Exception {
		ThreadLocalRandom fixture = ThreadLocalRandom.current();
		fixture.initialized = true;
		long n = 2147483647L;

		long result = fixture.nextLong(n);

		// add additional test code here
		assertEquals(394722453L, result);
	}

	/**
	 * Run the long nextLong(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextLong_3()
		throws Exception {
		ThreadLocalRandom fixture = ThreadLocalRandom.current();
		fixture.initialized = true;
		long n = 1L;

		long result = fixture.nextLong(n);

		// add additional test code here
		assertEquals(0L, result);
	}

	/**
	 * Run the long nextLong(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextLong_4()
		throws Exception {
		try {
			ThreadLocalRandom fixture = ThreadLocalRandom.current();
			fixture.initialized = true;
			long n = 0L;

			long result = fixture.nextLong(n);

			// add additional test code here
			fail("The exception java.lang.IllegalArgumentException should have been thrown.");
		} catch (java.lang.IllegalArgumentException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the long nextLong(long,long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextLong_5()
		throws Exception {
		ThreadLocalRandom fixture = ThreadLocalRandom.current();
		fixture.initialized = true;
		long least = 1L;
		long bound = 1L;

		long result = fixture.nextLong(least, bound);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IllegalArgumentException
		//       at com.yammer.metrics.stats.ThreadLocalRandom.nextLong(ThreadLocalRandom.java:140)
		assertEquals(0L, result);
	}

	/**
	 * Run the long nextLong(long,long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testNextLong_6()
		throws Exception {
		try {
			ThreadLocalRandom fixture = ThreadLocalRandom.current();
			fixture.initialized = true;
			long least = 1L;
			long bound = 1L;

			long result = fixture.nextLong(least, bound);

			// add additional test code here
			fail("The exception java.lang.IllegalArgumentException should have been thrown.");
		} catch (java.lang.IllegalArgumentException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the void setSeed(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetSeed_1()
		throws Exception {
		ThreadLocalRandom fixture = ThreadLocalRandom.current();
		fixture.initialized = false;
		long seed = 1L;

		fixture.setSeed(seed);

		// add additional test code here
	}

	/**
	 * Run the void setSeed(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetSeed_2()
		throws Exception {
		try {
			ThreadLocalRandom fixture = ThreadLocalRandom.current();
			fixture.initialized = true;
			long seed = 1L;

			fixture.setSeed(seed);

			// add additional test code here
			fail("The exception java.lang.UnsupportedOperationException should have been thrown.");
		} catch (java.lang.UnsupportedOperationException exception) {
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
	 * @generatedBy CodePro at 18/10/13 19:13
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
	 * @generatedBy CodePro at 18/10/13 19:13
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
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(ThreadLocalRandomTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ThreadLocalRandomTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}