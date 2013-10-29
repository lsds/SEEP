package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import junit.framework.*;
import uk.ac.imperial.lsds.seep.state.State;
import uk.ac.imperial.lsds.seep.elastic.MockState;

/**
 * The class <code>InitOperatorStateTest</code> contains tests for the class <code>{@link InitOperatorState}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:00
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class InitOperatorStateTest extends TestCase {
	/**
	 * Run the InitOperatorState() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testInitOperatorState_1()
		throws Exception {

		InitOperatorState result = new InitOperatorState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getState());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the InitOperatorState(int,State) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testInitOperatorState_2()
		throws Exception {
		int opId = 1;
		State state = new MockState();

		InitOperatorState result = new InitOperatorState(opId, state);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getOpId());
	}

	/**
	 * Run the int getOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testGetOpId_1()
		throws Exception {
		InitOperatorState fixture = new InitOperatorState(1, new MockState());

		int result = fixture.getOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the State getState() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testGetState_1()
		throws Exception {
		InitOperatorState fixture = new InitOperatorState(1, new MockState());

		State result = fixture.getState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getData_ts());
		assertEquals(0, result.getCheckpointInterval());
		assertEquals(null, result.getStateTag());
		assertEquals(0, result.getOwnerId());
		assertEquals(null, result.getStateImpl());
	}

	/**
	 * Run the void setOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testSetOpId_1()
		throws Exception {
		InitOperatorState fixture = new InitOperatorState(1, new MockState());
		int opId = 1;

		fixture.setOpId(opId);

		// add additional test code here
	}

	/**
	 * Run the void setState(State) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testSetState_1()
		throws Exception {
		InitOperatorState fixture = new InitOperatorState(1, new MockState());
		State state = new MockState();

		fixture.setState(state);

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
			junit.textui.TestRunner.run(InitOperatorStateTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new InitOperatorStateTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}