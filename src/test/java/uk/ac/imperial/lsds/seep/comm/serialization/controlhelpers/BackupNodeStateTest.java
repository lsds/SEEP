package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import junit.framework.*;

/**
 * The class <code>BackupNodeStateTest</code> contains tests for the class <code>{@link BackupNodeState}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:01
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class BackupNodeStateTest extends TestCase {
	/**
	 * Run the BackupNodeState() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testBackupNodeState_1()
		throws Exception {

		BackupNodeState result = new BackupNodeState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getUpstreamOpId());
		assertEquals(null, result.getBackupOperatorState());
		assertEquals(0, result.getNodeId());
	}

	/**
	 * Run the BackupNodeState(int,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testBackupNodeState_2()
		throws Exception {
		int nodeId = 1;
		int upstreamOpId = 1;

		BackupNodeState result = new BackupNodeState(nodeId, upstreamOpId);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getUpstreamOpId());
		assertEquals(null, result.getBackupOperatorState());
		assertEquals(1, result.getNodeId());
	}

	/**
	 * Run the BackupNodeState(int,BackupOperatorState[]) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testBackupNodeState_3()
		throws Exception {
		int nodeId = 1;
		BackupOperatorState[] backupOperatorState = new BackupOperatorState[] {};

		BackupNodeState result = new BackupNodeState(nodeId, backupOperatorState);

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getUpstreamOpId());
		assertEquals(1, result.getNodeId());
	}

	/**
	 * Run the BackupOperatorState[] getBackupOperatorState() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetBackupOperatorState_1()
		throws Exception {
		BackupNodeState fixture = new BackupNodeState(1, 1);
		fixture.setBackupOperatorState(new BackupOperatorState[] {});

		BackupOperatorState[] result = fixture.getBackupOperatorState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.length);
	}

	/**
	 * Run the BackupOperatorState getBackupOperatorStateWithOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetBackupOperatorStateWithOpId_1()
		throws Exception {
		BackupNodeState fixture = new BackupNodeState(1, 1);
		fixture.setBackupOperatorState(new BackupOperatorState[] {});
		int opId = 1;

		BackupOperatorState result = fixture.getBackupOperatorStateWithOpId(opId);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the BackupOperatorState getBackupOperatorStateWithOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetBackupOperatorStateWithOpId_2()
		throws Exception {
		BackupNodeState fixture = new BackupNodeState(1, 1);
		fixture.setBackupOperatorState(new BackupOperatorState[] {});
		int opId = 1;

		BackupOperatorState result = fixture.getBackupOperatorStateWithOpId(opId);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the BackupOperatorState getBackupOperatorStateWithOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetBackupOperatorStateWithOpId_3()
		throws Exception {
		BackupNodeState fixture = new BackupNodeState(1, 1);
		fixture.setBackupOperatorState(new BackupOperatorState[] {});
		int opId = 1;

		BackupOperatorState result = fixture.getBackupOperatorStateWithOpId(opId);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the int getNodeId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetNodeId_1()
		throws Exception {
		BackupNodeState fixture = new BackupNodeState(1, 1);
		fixture.setBackupOperatorState(new BackupOperatorState[] {});

		int result = fixture.getNodeId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getUpstreamOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testGetUpstreamOpId_1()
		throws Exception {
		BackupNodeState fixture = new BackupNodeState(1, 1);
		fixture.setBackupOperatorState(new BackupOperatorState[] {});

		int result = fixture.getUpstreamOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the void replaceOpBackupWithId(int,BackupOperatorState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testReplaceOpBackupWithId_1()
		throws Exception {
		BackupNodeState fixture = new BackupNodeState(1, 1);
		fixture.setBackupOperatorState(new BackupOperatorState[] {});
		int opIdToReplace = 1;
		BackupOperatorState replace = new BackupOperatorState();

		fixture.replaceOpBackupWithId(opIdToReplace, replace);

		// add additional test code here
	}

	/**
	 * Run the void replaceOpBackupWithId(int,BackupOperatorState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testReplaceOpBackupWithId_2()
		throws Exception {
		BackupNodeState fixture = new BackupNodeState(1, 1);
		fixture.setBackupOperatorState(new BackupOperatorState[] {});
		int opIdToReplace = 1;
		BackupOperatorState replace = new BackupOperatorState();

		fixture.replaceOpBackupWithId(opIdToReplace, replace);

		// add additional test code here
	}

	/**
	 * Run the void replaceOpBackupWithId(int,BackupOperatorState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testReplaceOpBackupWithId_3()
		throws Exception {
		BackupNodeState fixture = new BackupNodeState(1, 1);
		fixture.setBackupOperatorState(new BackupOperatorState[] {});
		int opIdToReplace = 1;
		BackupOperatorState replace = new BackupOperatorState();

		fixture.replaceOpBackupWithId(opIdToReplace, replace);

		// add additional test code here
	}

	/**
	 * Run the void setBackupOperatorState(BackupOperatorState[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testSetBackupOperatorState_1()
		throws Exception {
		BackupNodeState fixture = new BackupNodeState(1, 1);
		fixture.setBackupOperatorState(new BackupOperatorState[] {});
		BackupOperatorState[] backupOperatorState = new BackupOperatorState[] {};

		fixture.setBackupOperatorState(backupOperatorState);

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
			junit.textui.TestRunner.run(BackupNodeStateTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new BackupNodeStateTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}