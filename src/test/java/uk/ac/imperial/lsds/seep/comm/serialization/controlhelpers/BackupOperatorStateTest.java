package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.state.State;
import uk.ac.imperial.lsds.seep.buffer.OutputBuffer;
import uk.ac.imperial.lsds.seep.elastic.MockState;
import junit.framework.*;

/**
 * The class <code>BackupOperatorStateTest</code> contains tests for the class <code>{@link BackupOperatorState}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:02
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class BackupOperatorStateTest extends TestCase {
	/**
	 * Run the BackupOperatorState() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testBackupOperatorState_1()
		throws Exception {

		BackupOperatorState result = new BackupOperatorState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getState());
		assertEquals(null, result.getStateClass());
		assertEquals(null, result.getOutputBuffers());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the int getOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testGetOpId_1()
		throws Exception {
		BackupOperatorState fixture = new BackupOperatorState();
		fixture.setOutputBuffers(new ArrayList());
		fixture.setStateClass("");
		fixture.setOpId(1);
		fixture.setState(new MockState());

		int result = fixture.getOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the ArrayList<OutputBuffer> getOutputBuffers() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testGetOutputBuffers_1()
		throws Exception {
		BackupOperatorState fixture = new BackupOperatorState();
		fixture.setOutputBuffers(new ArrayList());
		fixture.setStateClass("");
		fixture.setOpId(1);
		fixture.setState(new MockState());

		ArrayList<OutputBuffer> result = fixture.getOutputBuffers();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the State getState() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testGetState_1()
		throws Exception {
		BackupOperatorState fixture = new BackupOperatorState();
		fixture.setOutputBuffers(new ArrayList());
		fixture.setStateClass("");
		fixture.setOpId(1);
		fixture.setState(new MockState());

		State result = fixture.getState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getOwnerId());
		assertEquals(null, result.getStateTag());
		assertEquals(0, result.getCheckpointInterval());
		assertEquals(null, result.getStateImpl());
		assertEquals(null, result.getData_ts());
	}

	/**
	 * Run the String getStateClass() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testGetStateClass_1()
		throws Exception {
		BackupOperatorState fixture = new BackupOperatorState();
		fixture.setOutputBuffers(new ArrayList());
		fixture.setStateClass("");
		fixture.setOpId(1);
		fixture.setState(new MockState());

		String result = fixture.getStateClass();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the void setOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testSetOpId_1()
		throws Exception {
		BackupOperatorState fixture = new BackupOperatorState();
		fixture.setOutputBuffers(new ArrayList());
		fixture.setStateClass("");
		fixture.setOpId(1);
		fixture.setState(new MockState());
		int opId = 1;

		fixture.setOpId(opId);

		// add additional test code here
	}

	/**
	 * Run the void setOutputBuffers(ArrayList<OutputBuffer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testSetOutputBuffers_1()
		throws Exception {
		BackupOperatorState fixture = new BackupOperatorState();
		fixture.setOutputBuffers(new ArrayList());
		fixture.setStateClass("");
		fixture.setOpId(1);
		fixture.setState(new MockState());
		ArrayList<OutputBuffer> outputBuffers = new ArrayList();

		fixture.setOutputBuffers(outputBuffers);

		// add additional test code here
	}

	/**
	 * Run the void setState(State) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testSetState_1()
		throws Exception {
		BackupOperatorState fixture = new BackupOperatorState();
		fixture.setOutputBuffers(new ArrayList());
		fixture.setStateClass("");
		fixture.setOpId(1);
		fixture.setState(new MockState());
		State state = new MockState();

		fixture.setState(state);

		// add additional test code here
	}

	/**
	 * Run the void setStateClass(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testSetStateClass_1()
		throws Exception {
		BackupOperatorState fixture = new BackupOperatorState();
		fixture.setOutputBuffers(new ArrayList());
		fixture.setStateClass("");
		fixture.setOpId(1);
		fixture.setState(new MockState());
		String stateClass = "";

		fixture.setStateClass(stateClass);

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
	 * @generatedBy CodePro at 18/10/13 19:02
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
	 * @generatedBy CodePro at 18/10/13 19:02
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
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(BackupOperatorStateTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new BackupOperatorStateTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}