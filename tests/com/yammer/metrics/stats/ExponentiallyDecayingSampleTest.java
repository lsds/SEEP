package com.yammer.metrics.stats;

import com.yammer.metrics.core.Clock;
import junit.framework.*;

/**
 * The class <code>ExponentiallyDecayingSampleTest</code> contains tests for the class <code>{@link ExponentiallyDecayingSample}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:01
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ExponentiallyDecayingSampleTest extends TestCase {
	/**
	 * Run the ExponentiallyDecayingSample(int,double) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testExponentiallyDecayingSample_1()
		throws Exception {
		int reservoirSize = 1;
		double alpha = 1.0;

		ExponentiallyDecayingSample result = new ExponentiallyDecayingSample(reservoirSize, alpha);

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the ExponentiallyDecayingSample(int,double,Clock) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testExponentiallyDecayingSample_2()
		throws Exception {
		int reservoirSize = 1;
		double alpha = 1.0;
		Clock clock = new com.yammer.metrics.core.Clock.CpuTimeClock();

		ExponentiallyDecayingSample result = new ExponentiallyDecayingSample(reservoirSize, alpha, clock);

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the void clear() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testClear_1()
		throws Exception {
		ExponentiallyDecayingSample fixture = new ExponentiallyDecayingSample(1, 1.0, new com.yammer.metrics.core.Clock.CpuTimeClock());

		fixture.clear();

		// add additional test code here
	}

	/**
	 * Run the Snapshot getSnapshot() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetSnapshot_1()
		throws Exception {
		ExponentiallyDecayingSample fixture = new ExponentiallyDecayingSample(1, 1.0, new com.yammer.metrics.core.Clock.CpuTimeClock());

		Snapshot result = fixture.getSnapshot();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
		assertEquals(0.0, result.get999thPercentile(), 1.0);
		assertEquals(0.0, result.get95thPercentile(), 1.0);
		assertEquals(0.0, result.get99thPercentile(), 1.0);
		assertEquals(0.0, result.get75thPercentile(), 1.0);
		assertEquals(0.0, result.get98thPercentile(), 1.0);
		assertEquals(0.0, result.getMedian(), 1.0);
	}

	/**
	 * Run the int size() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSize_1()
		throws Exception {
		ExponentiallyDecayingSample fixture = new ExponentiallyDecayingSample(1, 1.0, new com.yammer.metrics.core.Clock.CpuTimeClock());

		int result = fixture.size();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the void update(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testUpdate_1()
		throws Exception {
		ExponentiallyDecayingSample fixture = new ExponentiallyDecayingSample(1, 1.0, new com.yammer.metrics.core.Clock.CpuTimeClock());
		long value = 1L;

		fixture.update(value);

		// add additional test code here
	}

	/**
	 * Run the void update(long,long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testUpdate_2()
		throws Exception {
		ExponentiallyDecayingSample fixture = new ExponentiallyDecayingSample(1, 1.0, new com.yammer.metrics.core.Clock.CpuTimeClock());
		long value = 1L;
		long timestamp = 1L;

		fixture.update(value, timestamp);

		// add additional test code here
	}

	/**
	 * Run the void update(long,long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testUpdate_3()
		throws Exception {
		ExponentiallyDecayingSample fixture = new ExponentiallyDecayingSample(1, 1.0, new com.yammer.metrics.core.Clock.CpuTimeClock());
		long value = 1L;
		long timestamp = 1L;

		fixture.update(value, timestamp);

		// add additional test code here
	}

	/**
	 * Run the void update(long,long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testUpdate_4()
		throws Exception {
		ExponentiallyDecayingSample fixture = new ExponentiallyDecayingSample(1, 1.0, new com.yammer.metrics.core.Clock.CpuTimeClock());
		long value = 1L;
		long timestamp = 1L;

		fixture.update(value, timestamp);

		// add additional test code here
	}

	/**
	 * Run the void update(long,long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testUpdate_5()
		throws Exception {
		ExponentiallyDecayingSample fixture = new ExponentiallyDecayingSample(1, 1.0, new com.yammer.metrics.core.Clock.CpuTimeClock());
		long value = 1L;
		long timestamp = 1L;

		fixture.update(value, timestamp);

		// add additional test code here
	}

	/**
	 * Run the void update(long,long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testUpdate_6()
		throws Exception {
		ExponentiallyDecayingSample fixture = new ExponentiallyDecayingSample(1, 1.0, new com.yammer.metrics.core.Clock.CpuTimeClock());
		long value = 1L;
		long timestamp = 1L;

		fixture.update(value, timestamp);

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
	 * @generatedBy CodePro at 18/10/13 19:01
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
	 * @generatedBy CodePro at 18/10/13 19:01
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
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(ExponentiallyDecayingSampleTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ExponentiallyDecayingSampleTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}