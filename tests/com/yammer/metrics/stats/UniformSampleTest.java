package com.yammer.metrics.stats;

import junit.framework.*;

/**
 * The class <code>UniformSampleTest</code> contains tests for the class <code>{@link UniformSample}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:13
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class UniformSampleTest extends TestCase {
	/**
	 * Run the UniformSample(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testUniformSample_1()
		throws Exception {
		int reservoirSize = 1;

		UniformSample result = new UniformSample(reservoirSize);

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the void clear() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testClear_1()
		throws Exception {
		UniformSample fixture = new UniformSample(1);

		fixture.clear();

		// add additional test code here
	}

	/**
	 * Run the void clear() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testClear_2()
		throws Exception {
		UniformSample fixture = new UniformSample(1);

		fixture.clear();

		// add additional test code here
	}

	/**
	 * Run the Snapshot getSnapshot() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetSnapshot_1()
		throws Exception {
		UniformSample fixture = new UniformSample(1);

		Snapshot result = fixture.getSnapshot();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
		assertEquals(0.0, result.get75thPercentile(), 1.0);
		assertEquals(0.0, result.get999thPercentile(), 1.0);
		assertEquals(0.0, result.getMedian(), 1.0);
		assertEquals(0.0, result.get99thPercentile(), 1.0);
		assertEquals(0.0, result.get95thPercentile(), 1.0);
		assertEquals(0.0, result.get98thPercentile(), 1.0);
	}

	/**
	 * Run the Snapshot getSnapshot() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetSnapshot_2()
		throws Exception {
		UniformSample fixture = new UniformSample(1);

		Snapshot result = fixture.getSnapshot();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
		assertEquals(0.0, result.get75thPercentile(), 1.0);
		assertEquals(0.0, result.get999thPercentile(), 1.0);
		assertEquals(0.0, result.getMedian(), 1.0);
		assertEquals(0.0, result.get99thPercentile(), 1.0);
		assertEquals(0.0, result.get95thPercentile(), 1.0);
		assertEquals(0.0, result.get98thPercentile(), 1.0);
	}

	/**
	 * Run the int size() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSize_1()
		throws Exception {
		UniformSample fixture = new UniformSample(1);

		int result = fixture.size();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int size() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSize_2()
		throws Exception {
		UniformSample fixture = new UniformSample(1);

		int result = fixture.size();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the void update(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testUpdate_1()
		throws Exception {
		UniformSample fixture = new UniformSample(1);
		long value = 1L;

		fixture.update(value);

		// add additional test code here
	}

	/**
	 * Run the void update(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testUpdate_2()
		throws Exception {
		UniformSample fixture = new UniformSample(1);
		long value = 1L;

		fixture.update(value);

		// add additional test code here
	}

	/**
	 * Run the void update(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testUpdate_3()
		throws Exception {
		UniformSample fixture = new UniformSample(1);
		long value = 1L;

		fixture.update(value);

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
			junit.textui.TestRunner.run(UniformSampleTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new UniformSampleTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}