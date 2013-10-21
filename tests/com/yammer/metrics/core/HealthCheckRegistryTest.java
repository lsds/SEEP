package com.yammer.metrics.core;

import java.util.SortedMap;
import com.yammer.metrics.util.DeadlockHealthCheck;
import junit.framework.*;

/**
 * The class <code>HealthCheckRegistryTest</code> contains tests for the class <code>{@link HealthCheckRegistry}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:00
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class HealthCheckRegistryTest extends TestCase {
	/**
	 * Run the HealthCheckRegistry() constructor test.
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testHealthCheckRegistry_1()
		throws Exception {
		HealthCheckRegistry result = new HealthCheckRegistry();
		assertNotNull(result);
		// add additional test code here
	}

	/**
	 * Run the void register(HealthCheck) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRegister_1()
		throws Exception {
		HealthCheckRegistry fixture = new HealthCheckRegistry();
		HealthCheck healthCheck = new DeadlockHealthCheck();

		fixture.register(healthCheck);

		// add additional test code here
	}

	/**
	 * Run the SortedMap<String, HealthCheck.Result> runHealthChecks() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRunHealthChecks_1()
		throws Exception {
		HealthCheckRegistry fixture = new HealthCheckRegistry();

		SortedMap<String, HealthCheck.Result> result = fixture.runHealthChecks();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the SortedMap<String, HealthCheck.Result> runHealthChecks() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRunHealthChecks_2()
		throws Exception {
		HealthCheckRegistry fixture = new HealthCheckRegistry();

		SortedMap<String, HealthCheck.Result> result = fixture.runHealthChecks();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the void unregister(HealthCheck) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testUnregister_1()
		throws Exception {
		HealthCheckRegistry fixture = new HealthCheckRegistry();
		HealthCheck healthCheck = new DeadlockHealthCheck();

		fixture.unregister(healthCheck);

		// add additional test code here
	}

	/**
	 * Run the void unregister(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testUnregister_2()
		throws Exception {
		HealthCheckRegistry fixture = new HealthCheckRegistry();
		String name = "";

		fixture.unregister(name);

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
	 * @generatedBy CodePro at 18/10/13 19:00
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
	 * @generatedBy CodePro at 18/10/13 19:00
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
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(HealthCheckRegistryTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new HealthCheckRegistryTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}