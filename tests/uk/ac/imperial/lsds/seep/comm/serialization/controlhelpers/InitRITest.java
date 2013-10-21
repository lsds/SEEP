package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.ArrayList;
import junit.framework.*;

/**
 * The class <code>InitRITest</code> contains tests for the class <code>{@link InitRI}</code>.
 *
 * @generatedBy CodePro at 18/10/13 18:58
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class InitRITest extends TestCase {
	/**
	 * Run the InitRI() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testInitRI_1()
		throws Exception {

		InitRI result = new InitRI();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getKey());
		assertEquals(null, result.getIndex());
		assertEquals(0, result.getNodeId());
	}

	/**
	 * Run the InitRI(int,ArrayList<Integer>,ArrayList<Integer>) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testInitRI_2()
		throws Exception {
		int nodeId = 1;
		ArrayList<Integer> index = new ArrayList();
		ArrayList<Integer> key = new ArrayList();

		InitRI result = new InitRI(nodeId, index, key);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getNodeId());
	}

	/**
	 * Run the ArrayList<Integer> getIndex() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testGetIndex_1()
		throws Exception {
		InitRI fixture = new InitRI(1, new ArrayList(), new ArrayList());

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
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testGetKey_1()
		throws Exception {
		InitRI fixture = new InitRI(1, new ArrayList(), new ArrayList());

		ArrayList<Integer> result = fixture.getKey();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the int getNodeId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testGetNodeId_1()
		throws Exception {
		InitRI fixture = new InitRI(1, new ArrayList(), new ArrayList());

		int result = fixture.getNodeId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the void setIndex(ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSetIndex_1()
		throws Exception {
		InitRI fixture = new InitRI(1, new ArrayList(), new ArrayList());
		ArrayList<Integer> index = new ArrayList();

		fixture.setIndex(index);

		// add additional test code here
	}

	/**
	 * Run the void setKey(ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSetKey_1()
		throws Exception {
		InitRI fixture = new InitRI(1, new ArrayList(), new ArrayList());
		ArrayList<Integer> key = new ArrayList();

		fixture.setKey(key);

		// add additional test code here
	}

	/**
	 * Run the void setNodeId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSetNodeId_1()
		throws Exception {
		InitRI fixture = new InitRI(1, new ArrayList(), new ArrayList());
		int nodeId = 1;

		fixture.setNodeId(nodeId);

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
	 * @generatedBy CodePro at 18/10/13 18:58
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
	 * @generatedBy CodePro at 18/10/13 18:58
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
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(InitRITest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new InitRITest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}