package com.yammer.metrics.util;

import javax.management.ObjectName;
import junit.framework.*;

/**
 * The class <code>JmxGaugeTest</code> contains tests for the class <code>{@link JmxGauge}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:07
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class JmxGaugeTest extends TestCase {
	/**
	 * Run the JmxGauge(String,String) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testJmxGauge_1()
		throws Exception {
		String objectName = "";
		String attribute = "";

		JmxGauge result = new JmxGauge(objectName, attribute);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the JmxGauge(String,String) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testJmxGauge_2()
		throws Exception {
		String objectName = "";
		String attribute = "";

		JmxGauge result = new JmxGauge(objectName, attribute);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the JmxGauge(ObjectName,String) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testJmxGauge_3()
		throws Exception {
		ObjectName objectName = ObjectName.getInstance(new ObjectName(""));
		String attribute = "";

		JmxGauge result = new JmxGauge(objectName, attribute);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the Object getValue() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetValue_1()
		throws Exception {
		JmxGauge fixture = new JmxGauge(ObjectName.getInstance(new ObjectName("")), "");

		Object result = fixture.getValue();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.RuntimeException: javax.management.InstanceNotFoundException: *:*
		//       at com.yammer.metrics.util.JmxGauge.getValue(JmxGauge.java:46)
		assertNotNull(result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
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
	 * @generatedBy CodePro at 18/10/13 19:07
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
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(JmxGaugeTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new JmxGaugeTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}