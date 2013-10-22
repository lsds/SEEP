package uk.ac.imperial.lsds.seep.infrastructure.monitor;

import junit.framework.*;

/**
 * The class <code>LocalReporterMonitorTest</code> contains tests for the class <code>{@link LocalReporterMonitor}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:13
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class LocalReporterMonitorTest extends TestCase {
	/**
	 * Run the LocalReporterMonitor() constructor test.
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testLocalReporterMonitor_1()
		throws Exception {
		LocalReporterMonitor result = new LocalReporterMonitor();
		assertNotNull(result);
		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testRun_1()
		throws Exception {
		LocalReporterMonitor fixture = new LocalReporterMonitor();

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testRun_2()
		throws Exception {
		LocalReporterMonitor fixture = new LocalReporterMonitor();

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testRun_3()
		throws Exception {
		LocalReporterMonitor fixture = new LocalReporterMonitor();

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testRun_4()
		throws Exception {
		LocalReporterMonitor fixture = new LocalReporterMonitor();

		fixture.run();

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
			junit.textui.TestRunner.run(LocalReporterMonitorTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new LocalReporterMonitorTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}