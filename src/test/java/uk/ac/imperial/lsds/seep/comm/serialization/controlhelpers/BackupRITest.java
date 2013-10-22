package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.ArrayList;
import junit.framework.*;

/**
 * The class <code>BackupRITest</code> contains tests for the class <code>{@link BackupRI}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:06
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class BackupRITest extends TestCase {
	/**
	 * Run the BackupRI() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testBackupRI_1()
		throws Exception {

		BackupRI result = new BackupRI();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getKey());
		assertEquals(null, result.getIndex());
		assertEquals(null, result.getOperatorType());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the BackupRI(int,ArrayList<Integer>,ArrayList<Integer>,String) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testBackupRI_2()
		throws Exception {
		int opId = 1;
		ArrayList<Integer> index = new ArrayList();
		ArrayList<Integer> key = new ArrayList();
		String operatorType = "";

		BackupRI result = new BackupRI(opId, index, key, operatorType);

		// add additional test code here
		assertNotNull(result);
		assertEquals("", result.getOperatorType());
		assertEquals(1, result.getOpId());
	}

	/**
	 * Run the ArrayList<Integer> getIndex() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetIndex_1()
		throws Exception {
		BackupRI fixture = new BackupRI(1, new ArrayList(), new ArrayList(), "");

		ArrayList<Integer> result = fixture.getIndex();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the ArrayList<Integer> getKey() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetKey_1()
		throws Exception {
		BackupRI fixture = new BackupRI(1, new ArrayList(), new ArrayList(), "");

		ArrayList<Integer> result = fixture.getKey();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the int getOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOpId_1()
		throws Exception {
		BackupRI fixture = new BackupRI(1, new ArrayList(), new ArrayList(), "");

		int result = fixture.getOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the String getOperatorType() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testGetOperatorType_1()
		throws Exception {
		BackupRI fixture = new BackupRI(1, new ArrayList(), new ArrayList(), "");

		String result = fixture.getOperatorType();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the void setIndex(ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSetIndex_1()
		throws Exception {
		BackupRI fixture = new BackupRI(1, new ArrayList(), new ArrayList(), "");
		ArrayList<Integer> index = new ArrayList();

		fixture.setIndex(index);

		// add additional test code here
	}

	/**
	 * Run the void setKey(ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSetKey_1()
		throws Exception {
		BackupRI fixture = new BackupRI(1, new ArrayList(), new ArrayList(), "");
		ArrayList<Integer> key = new ArrayList();

		fixture.setKey(key);

		// add additional test code here
	}

	/**
	 * Run the void setOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSetOpId_1()
		throws Exception {
		BackupRI fixture = new BackupRI(1, new ArrayList(), new ArrayList(), "");
		int opId = 1;

		fixture.setOpId(opId);

		// add additional test code here
	}

	/**
	 * Run the void setOperatorType(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public void testSetOperatorType_1()
		throws Exception {
		BackupRI fixture = new BackupRI(1, new ArrayList(), new ArrayList(), "");
		String operatorType = "";

		fixture.setOperatorType(operatorType);

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
	 * @generatedBy CodePro at 18/10/13 19:06
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
	 * @generatedBy CodePro at 18/10/13 19:06
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
	 * @generatedBy CodePro at 18/10/13 19:06
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(BackupRITest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new BackupRITest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}