package com.yammer.metrics.core;

import java.util.concurrent.TimeUnit;
import junit.framework.*;

/**
 * The class <code>TimerContextTest</code> contains tests for the class <code>{@link TimerContext}</code>.
 *
 * @generatedBy CodePro at 18/10/13 18:59
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class TimerContextTest extends TestCase {
	/**
	 * Run the TimerContext(Timer,Clock) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testTimerContext_1()
		throws Exception {
		Timer timer = new Timer(TimeUnit.DAYS, TimeUnit.DAYS, new Clock.CpuTimeClock());
		Clock clock = new Clock.CpuTimeClock();

		TimerContext result = new TimerContext(timer, clock);

		// add additional test code here
		assertNotNull(result);
		assertEquals(46993L, result.stop());
	}

	/**
	 * Run the long stop() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testStop_1()
		throws Exception {
		TimerContext fixture = new TimerContext(new Timer(TimeUnit.DAYS, TimeUnit.DAYS, new Clock.CpuTimeClock()), new Clock.CpuTimeClock());

		long result = fixture.stop();

		// add additional test code here
		assertEquals(31132L, result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
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
	 * @generatedBy CodePro at 18/10/13 18:59
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
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(TimerContextTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new TimerContextTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}