package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import junit.framework.*;

/**
 * The class <code>ReplayStateInfoTest</code> contains tests for the class <code>{@link ReplayStateInfo}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:05
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ReplayStateInfoTest extends TestCase {
	/**
	 * Run the ReplayStateInfo() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testReplayStateInfo_1()
		throws Exception {

		ReplayStateInfo result = new ReplayStateInfo();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getNewOpId());
		assertEquals(false, result.isStreamToSingleNode());
		assertEquals(0, result.getOldOpId());
	}

	/**
	 * Run the ReplayStateInfo(int,int,boolean) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testReplayStateInfo_2()
		throws Exception {
		int oldOpId = 1;
		int newOpId = 1;
		boolean singleNode = true;

		ReplayStateInfo result = new ReplayStateInfo(oldOpId, newOpId, singleNode);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getNewOpId());
		assertEquals(true, result.isStreamToSingleNode());
		assertEquals(1, result.getOldOpId());
	}

	/**
	 * Run the int getNewOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetNewOpId_1()
		throws Exception {
		ReplayStateInfo fixture = new ReplayStateInfo(1, 1, true);

		int result = fixture.getNewOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getOldOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetOldOpId_1()
		throws Exception {
		ReplayStateInfo fixture = new ReplayStateInfo(1, 1, true);

		int result = fixture.getOldOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the boolean isStreamToSingleNode() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testIsStreamToSingleNode_1()
		throws Exception {
		ReplayStateInfo fixture = new ReplayStateInfo(1, 1, true);

		boolean result = fixture.isStreamToSingleNode();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isStreamToSingleNode() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testIsStreamToSingleNode_2()
		throws Exception {
		ReplayStateInfo fixture = new ReplayStateInfo(1, 1, false);

		boolean result = fixture.isStreamToSingleNode();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the void setNewOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSetNewOpId_1()
		throws Exception {
		ReplayStateInfo fixture = new ReplayStateInfo(1, 1, true);
		int newOpId = 1;

		fixture.setNewOpId(newOpId);

		// add additional test code here
	}

	/**
	 * Run the void setOldOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSetOldOpId_1()
		throws Exception {
		ReplayStateInfo fixture = new ReplayStateInfo(1, 1, true);
		int oldOpId = 1;

		fixture.setOldOpId(oldOpId);

		// add additional test code here
	}

	/**
	 * Run the void setSingleNode(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSetSingleNode_1()
		throws Exception {
		ReplayStateInfo fixture = new ReplayStateInfo(1, 1, true);
		boolean singleNode = true;

		fixture.setSingleNode(singleNode);

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
			junit.textui.TestRunner.run(ReplayStateInfoTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ReplayStateInfoTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}