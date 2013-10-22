package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import junit.framework.*;

/**
 * The class <code>DistributedScaleOutInfoTest</code> contains tests for the class <code>{@link DistributedScaleOutInfo}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:10
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class DistributedScaleOutInfoTest extends TestCase {
	/**
	 * Run the DistributedScaleOutInfo() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testDistributedScaleOutInfo_1()
		throws Exception {

		DistributedScaleOutInfo result = new DistributedScaleOutInfo();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getNewOpId());
		assertEquals(0, result.getOldOpId());
	}

	/**
	 * Run the DistributedScaleOutInfo(int,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testDistributedScaleOutInfo_2()
		throws Exception {
		int oldOpId = 1;
		int newOpId = 1;

		DistributedScaleOutInfo result = new DistributedScaleOutInfo(oldOpId, newOpId);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getNewOpId());
		assertEquals(1, result.getOldOpId());
	}

	/**
	 * Run the int getNewOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetNewOpId_1()
		throws Exception {
		DistributedScaleOutInfo fixture = new DistributedScaleOutInfo(1, 1);

		int result = fixture.getNewOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getOldOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetOldOpId_1()
		throws Exception {
		DistributedScaleOutInfo fixture = new DistributedScaleOutInfo(1, 1);

		int result = fixture.getOldOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the void setNewOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testSetNewOpId_1()
		throws Exception {
		DistributedScaleOutInfo fixture = new DistributedScaleOutInfo(1, 1);
		int newOpId = 1;

		fixture.setNewOpId(newOpId);

		// add additional test code here
	}

	/**
	 * Run the void setOldOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testSetOldOpId_1()
		throws Exception {
		DistributedScaleOutInfo fixture = new DistributedScaleOutInfo(1, 1);
		int oldOpId = 1;

		fixture.setOldOpId(oldOpId);

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
	 * @generatedBy CodePro at 18/10/13 19:10
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
	 * @generatedBy CodePro at 18/10/13 19:10
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
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(DistributedScaleOutInfoTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new DistributedScaleOutInfoTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}