package uk.ac.imperial.lsds.seep;

import junit.framework.*;

/**
 * The class <code>PTest</code> contains tests for the class <code>{@link P}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:04
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class PTest extends TestCase {
	/**
	 * Run the P() constructor test.
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testP_1()
		throws Exception {
		P result = new P();
		assertNotNull(result);
		// add additional test code here
	}

	/**
	 * Run the boolean loadProperties() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testLoadProperties_1()
		throws Exception {
		P fixture = new P();

		boolean result = fixture.loadProperties();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean loadProperties() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testLoadProperties_2()
		throws Exception {
		P fixture = new P();

		boolean result = fixture.loadProperties();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the String valueFor(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testValueFor_1()
		throws Exception {
		String key = "";

		String result = P.valueFor(key);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
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
	 * @generatedBy CodePro at 18/10/13 19:04
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
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(PTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new PTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}