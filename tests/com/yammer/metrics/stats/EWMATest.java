package com.yammer.metrics.stats;

import java.util.concurrent.TimeUnit;
import junit.framework.*;

/**
 * The class <code>EWMATest</code> contains tests for the class <code>{@link EWMA}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:09
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class EWMATest extends TestCase {
	/**
	 * Run the EWMA(double,long,TimeUnit) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testEWMA_1()
		throws Exception {
		double alpha = 1.0;
		long interval = 1L;
		TimeUnit intervalUnit = TimeUnit.DAYS;

		EWMA result = new EWMA(alpha, interval, intervalUnit);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the EWMA fifteenMinuteEWMA() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testFifteenMinuteEWMA_1()
		throws Exception {

		EWMA result = EWMA.fifteenMinuteEWMA();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the EWMA fiveMinuteEWMA() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testFiveMinuteEWMA_1()
		throws Exception {

		EWMA result = EWMA.fiveMinuteEWMA();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the double getRate(TimeUnit) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetRate_1()
		throws Exception {
		EWMA fixture = new EWMA(1.0, 1L, TimeUnit.DAYS);
		TimeUnit rateUnit = TimeUnit.DAYS;

		double result = fixture.getRate(rateUnit);

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the EWMA oneMinuteEWMA() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testOneMinuteEWMA_1()
		throws Exception {

		EWMA result = EWMA.oneMinuteEWMA();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void tick() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testTick_1()
		throws Exception {
		EWMA fixture = new EWMA(1.0, 1L, TimeUnit.DAYS);

		fixture.tick();

		// add additional test code here
	}

	/**
	 * Run the void tick() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testTick_2()
		throws Exception {
		EWMA fixture = new EWMA(1.0, 1L, TimeUnit.DAYS);

		fixture.tick();

		// add additional test code here
	}

	/**
	 * Run the void update(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testUpdate_1()
		throws Exception {
		EWMA fixture = new EWMA(1.0, 1L, TimeUnit.DAYS);
		long n = 1L;

		fixture.update(n);

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
	 * @generatedBy CodePro at 18/10/13 19:09
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
	 * @generatedBy CodePro at 18/10/13 19:09
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
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(EWMATest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new EWMATest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}