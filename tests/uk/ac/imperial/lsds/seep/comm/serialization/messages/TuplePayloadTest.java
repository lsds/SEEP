package uk.ac.imperial.lsds.seep.comm.serialization.messages;

import junit.framework.*;

/**
 * The class <code>TuplePayloadTest</code> contains tests for the class <code>{@link TuplePayload}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:13
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class TuplePayloadTest extends TestCase {
	/**
	 * Run the TuplePayload() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testTuplePayload_1()
		throws Exception {

		TuplePayload result = new TuplePayload();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testToString_1()
		throws Exception {
		TuplePayload fixture = new TuplePayload();
		fixture.instrumentation_ts = 1L;
		fixture.attrValues = new Payload();
		fixture.timestamp = 1L;
		fixture.schemaId = 1;

		String result = fixture.toString();

		// add additional test code here
		assertEquals("VAL ", result);
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
			junit.textui.TestRunner.run(TuplePayloadTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new TuplePayloadTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}