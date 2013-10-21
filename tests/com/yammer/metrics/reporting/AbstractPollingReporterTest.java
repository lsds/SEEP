package com.yammer.metrics.reporting;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import junit.framework.*;

/**
 * The class <code>AbstractPollingReporterTest</code> contains tests for the class <code>{@link AbstractPollingReporter}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:10
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class AbstractPollingReporterTest extends TestCase {
	/**
	 * Run the void shutdown() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testShutdown_1()
		throws Exception {
		AbstractPollingReporter fixture = new ConsoleReporter(new PrintStream(new ByteArrayOutputStream()));

		fixture.shutdown();

		// add additional test code here
	}

	/**
	 * Run the void shutdown() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testShutdown_2()
		throws Exception {
		AbstractPollingReporter fixture = new ConsoleReporter(new PrintStream(new ByteArrayOutputStream()));

		fixture.shutdown();

		// add additional test code here
	}

	/**
	 * Run the void start(long,TimeUnit) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testStart_1()
		throws Exception {
		AbstractPollingReporter fixture = new ConsoleReporter(new PrintStream(new ByteArrayOutputStream()));
		long period = 1L;
		TimeUnit unit = TimeUnit.DAYS;

		fixture.start(period, unit);

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
	 * @generatedBy CodePro at 18/10/13 19:10
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
	 * @generatedBy CodePro at 18/10/13 19:10
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
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(AbstractPollingReporterTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new AbstractPollingReporterTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}