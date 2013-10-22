package uk.ac.imperial.lsds.seep.infrastructure.api.datastructure;

import junit.framework.*;

/**
 * The class <code>ComponentTest</code> contains tests for the class <code>{@link Component}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:01
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ComponentTest extends TestCase {
	/**
	 * Run the Component() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testComponent_1()
		throws Exception {

		Component result = new Component();

		// add additional test code here
		assertNotNull(result);
		assertEquals(" column-> 0 value-> 0", result.toString());
	}

	/**
	 * Run the Component(int,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testComponent_2()
		throws Exception {
		int col = 1;
		int value = 1;

		Component result = new Component(col, value);

		// add additional test code here
		assertNotNull(result);
		assertEquals(" column-> 1 value-> 1", result.toString());
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testToString_1()
		throws Exception {
		Component fixture = new Component(1, 1);

		String result = fixture.toString();

		// add additional test code here
		assertEquals(" column-> 1 value-> 1", result);
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
			junit.textui.TestRunner.run(ComponentTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ComponentTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}