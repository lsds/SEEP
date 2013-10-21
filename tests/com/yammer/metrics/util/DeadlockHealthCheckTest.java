package com.yammer.metrics.util;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.VirtualMachineMetrics;
import junit.framework.*;

/**
 * The class <code>DeadlockHealthCheckTest</code> contains tests for the class <code>{@link DeadlockHealthCheck}</code>.
 *
 * @generatedBy CodePro at 18/10/13 18:59
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class DeadlockHealthCheckTest extends TestCase {
	/**
	 * Run the DeadlockHealthCheck() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDeadlockHealthCheck_1()
		throws Exception {

		DeadlockHealthCheck result = new DeadlockHealthCheck();

		// add additional test code here
		assertNotNull(result);
		assertEquals("deadlocks", result.getName());
	}

	/**
	 * Run the DeadlockHealthCheck(VirtualMachineMetrics) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDeadlockHealthCheck_2()
		throws Exception {
		VirtualMachineMetrics vm = VirtualMachineMetrics.getInstance();

		DeadlockHealthCheck result = new DeadlockHealthCheck(vm);

		// add additional test code here
		assertNotNull(result);
		assertEquals("deadlocks", result.getName());
	}

	/**
	 * Run the com.yammer.metrics.core.HealthCheck.Result check() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testCheck_1()
		throws Exception {
		DeadlockHealthCheck fixture = new DeadlockHealthCheck(VirtualMachineMetrics.getInstance());

		com.yammer.metrics.core.HealthCheck.Result result = fixture.check();

		// add additional test code here
		assertNotNull(result);
		assertEquals("Result{isHealthy=true}", result.toString());
		assertEquals(null, result.getMessage());
		assertEquals(true, result.isHealthy());
		assertEquals(null, result.getError());
	}

	/**
	 * Run the com.yammer.metrics.core.HealthCheck.Result check() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testCheck_2()
		throws Exception {
		DeadlockHealthCheck fixture = new DeadlockHealthCheck(VirtualMachineMetrics.getInstance());

		com.yammer.metrics.core.HealthCheck.Result result = fixture.check();

		// add additional test code here
		assertNotNull(result);
		assertEquals("Result{isHealthy=true}", result.toString());
		assertEquals(null, result.getMessage());
		assertEquals(true, result.isHealthy());
		assertEquals(null, result.getError());
	}

	/**
	 * Run the com.yammer.metrics.core.HealthCheck.Result check() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testCheck_3()
		throws Exception {
		DeadlockHealthCheck fixture = new DeadlockHealthCheck(VirtualMachineMetrics.getInstance());

		com.yammer.metrics.core.HealthCheck.Result result = fixture.check();

		// add additional test code here
		assertNotNull(result);
		assertEquals("Result{isHealthy=true}", result.toString());
		assertEquals(null, result.getMessage());
		assertEquals(true, result.isHealthy());
		assertEquals(null, result.getError());
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
			junit.textui.TestRunner.run(DeadlockHealthCheckTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new DeadlockHealthCheckTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}