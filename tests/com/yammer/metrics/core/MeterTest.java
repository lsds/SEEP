package com.yammer.metrics.core;

import java.util.concurrent.TimeUnit;
import junit.framework.*;

/**
 * The class <code>MeterTest</code> contains tests for the class <code>{@link Meter}</code>.
 *
 * @generatedBy CodePro at 18/10/13 18:59
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class MeterTest extends TestCase {
	/**
	 * Run the Meter(String,TimeUnit,Clock) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testMeter_1()
		throws Exception {
		String eventType = "";
		TimeUnit rateUnit = TimeUnit.DAYS;
		Clock clock = new Clock.CpuTimeClock();

		Meter result = new Meter(eventType, rateUnit, clock);

		// add additional test code here
		assertNotNull(result);
		assertEquals(0.0, result.getMeanRate(), 1.0);
		assertEquals(0.0, result.getOneMinuteRate(), 1.0);
		assertEquals(0.0, result.getFifteenMinuteRate(), 1.0);
		assertEquals(0.0, result.getFiveMinuteRate(), 1.0);
		assertEquals(0L, result.getCount());
		assertEquals("", result.getEventType());
	}

	/**
	 * Run the long getCount() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testGetCount_1()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());

		long result = fixture.getCount();

		// add additional test code here
		assertEquals(0L, result);
	}

	/**
	 * Run the String getEventType() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testGetEventType_1()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());

		String result = fixture.getEventType();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the double getFifteenMinuteRate() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testGetFifteenMinuteRate_1()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());

		double result = fixture.getFifteenMinuteRate();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFiveMinuteRate() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testGetFiveMinuteRate_1()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());

		double result = fixture.getFiveMinuteRate();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getMeanRate() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testGetMeanRate_1()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());

		double result = fixture.getMeanRate();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getMeanRate() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testGetMeanRate_2()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());

		double result = fixture.getMeanRate();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getOneMinuteRate() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testGetOneMinuteRate_1()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());

		double result = fixture.getOneMinuteRate();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the TimeUnit getRateUnit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testGetRateUnit_1()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());

		TimeUnit result = fixture.getRateUnit();

		// add additional test code here
		assertNotNull(result);
		assertEquals("DAYS", result.name());
		assertEquals("DAYS", result.toString());
		assertEquals(6, result.ordinal());
	}

	/**
	 * Run the void mark() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testMark_1()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());

		fixture.mark();

		// add additional test code here
	}

	/**
	 * Run the void mark(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testMark_2()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());
		long n = 1L;

		fixture.mark(n);

		// add additional test code here
	}

	/**
	 * Run the void tick() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testTick_1()
		throws Exception {
		Meter fixture = new Meter("", TimeUnit.DAYS, new Clock.CpuTimeClock());

		fixture.tick();

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
			junit.textui.TestRunner.run(MeterTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new MeterTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}