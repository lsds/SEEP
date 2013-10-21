package uk.ac.imperial.lsds.seep.comm.serialization;

import junit.framework.*;

/**
 * The class <code>SeePSerializationExceptionTest</code> contains tests for the class <code>{@link SeePSerializationException}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:08
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class SeePSerializationExceptionTest extends TestCase {
	/**
	 * Run the SeePSerializationException(String) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSeePSerializationException_1()
		throws Exception {
		String msg = "";

		SeePSerializationException result = new SeePSerializationException(msg);

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getCause());
		assertEquals("uk.ac.imperial.lsds.seep.comm.serialization.SeePSerializationException: ", result.toString());
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
	 * @generatedBy CodePro at 18/10/13 19:08
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
	 * @generatedBy CodePro at 18/10/13 19:08
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
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(SeePSerializationExceptionTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new SeePSerializationExceptionTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}