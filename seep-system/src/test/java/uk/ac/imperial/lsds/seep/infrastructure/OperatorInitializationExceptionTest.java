package uk.ac.imperial.lsds.seep.infrastructure;

import junit.framework.*;

/**
 * The class <code>OperatorInitializationExceptionTest</code> contains tests for the class <code>{@link OperatorInitializationException}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:09
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class OperatorInitializationExceptionTest extends TestCase {
	/**
	 * Run the OperatorInitializationException() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testOperatorInitializationException_1()
		throws Exception {

		OperatorInitializationException result = new OperatorInitializationException();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getCause());
		assertEquals("uk.ac.imperial.lsds.seep.infrastructure.OperatorInitializationException", result.toString());
		assertEquals(null, result.getMessage());
		assertEquals(null, result.getLocalizedMessage());
	}

	/**
	 * Run the OperatorInitializationException(String) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testOperatorInitializationException_2()
		throws Exception {
		String message = "";

		OperatorInitializationException result = new OperatorInitializationException(message);

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getCause());
		assertEquals("uk.ac.imperial.lsds.seep.infrastructure.OperatorInitializationException: ", result.toString());
		assertEquals("", result.getMessage());
		assertEquals("", result.getLocalizedMessage());
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
			junit.textui.TestRunner.run(OperatorInitializationExceptionTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new OperatorInitializationExceptionTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}