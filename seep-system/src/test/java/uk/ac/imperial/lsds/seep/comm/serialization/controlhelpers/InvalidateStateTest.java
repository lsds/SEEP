package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import junit.framework.*;

/**
 * The class <code>InvalidateStateTest</code> contains tests for the class <code>{@link InvalidateState}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:01
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class InvalidateStateTest extends TestCase {
	/**
	 * Run the InvalidateState() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testInvalidateState_1()
		throws Exception {

		InvalidateState result = new InvalidateState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getOperatorId());
	}

	/**
	 * Run the InvalidateState(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testInvalidateState_2()
		throws Exception {
		int nodeId = 1;

		InvalidateState result = new InvalidateState(nodeId);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getOperatorId());
	}

	/**
	 * Run the int getOperatorId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetOperatorId_1()
		throws Exception {
		InvalidateState fixture = new InvalidateState(1);

		int result = fixture.getOperatorId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the void setOperatorId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetOperatorId_1()
		throws Exception {
		InvalidateState fixture = new InvalidateState(1);
		int nodeId = 1;

		fixture.setOperatorId(nodeId);

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
			junit.textui.TestRunner.run(InvalidateStateTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new InvalidateStateTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}