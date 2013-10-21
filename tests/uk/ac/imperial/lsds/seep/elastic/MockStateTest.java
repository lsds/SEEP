package uk.ac.imperial.lsds.seep.elastic;

import junit.framework.*;
import uk.ac.imperial.lsds.seep.operator.State;

/**
 * The class <code>MockStateTest</code> contains tests for the class <code>{@link MockState}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:05
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class MockStateTest extends TestCase {
	/**
	 * Run the MockState() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testMockState_1()
		throws Exception {

		MockState result = new MockState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getKeyAttribute());
		assertEquals(0, result.getCheckpointInterval());
		assertEquals(null, result.getStateTag());
		assertEquals(null, result.getStateImpl());
		assertEquals(null, result.getData_ts());
		assertEquals(0, result.getOwnerId());
	}

	/**
	 * Run the String getKeyAttribute() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetKeyAttribute_1()
		throws Exception {
		MockState fixture = new MockState();

		String result = fixture.getKeyAttribute();

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the void resetState() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testResetState_1()
		throws Exception {
		MockState fixture = new MockState();

		fixture.resetState();

		// add additional test code here
	}

	/**
	 * Run the void setKeyAttribute(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSetKeyAttribute_1()
		throws Exception {
		MockState fixture = new MockState();
		String s = "";

		fixture.setKeyAttribute(s);

		// add additional test code here
	}

	/**
	 * Run the State[] splitState(State,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSplitState_1()
		throws Exception {
		MockState fixture = new MockState();
		State toSplit = new MockState();
		int key = 1;

		State[] result = fixture.splitState(toSplit, key);

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
	 * @generatedBy CodePro at 18/10/13 19:05
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
	 * @generatedBy CodePro at 18/10/13 19:05
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
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(MockStateTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new MockStateTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}