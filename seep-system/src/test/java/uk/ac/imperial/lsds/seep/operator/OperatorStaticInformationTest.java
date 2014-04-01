package uk.ac.imperial.lsds.seep.operator;

import junit.framework.*;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;

/**
 * The class <code>OperatorStaticInformationTest</code> contains tests for the class <code>{@link OperatorStaticInformation}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:09
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class OperatorStaticInformationTest extends TestCase {
	/**
	 * Run the OperatorStaticInformation(int,int,Node,int,int,boolean) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testOperatorStaticInformation_1()
		throws Exception {
		int opId = 1;
		int originalOpId = 1;
		Node myNode = new Node(1);
		int inC = 1;
		int inD = 1;
		boolean isStatefull = true;

		OperatorStaticInformation result = new OperatorStaticInformation(opId, originalOpId, myNode, inC, inD, isStatefull);

		// add additional test code here
		assertNotNull(result);
		assertEquals("node: Node [ip=null, port=0]inC: 1inD: 1", result.toString());
		assertEquals(1, result.getInC());
		assertEquals(true, result.isStatefull());
		assertEquals(1, result.getInD());
		assertEquals(1, result.getOriginalOpId());
		assertEquals(1, result.getOpId());
	}

	/**
	 * Run the int getInC() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetInC_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		int result = fixture.getInC();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getInD() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetInD_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		int result = fixture.getInD();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the Node getMyNode() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetMyNode_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		Node result = fixture.getMyNode();

		// add additional test code here
		assertNotNull(result);
		assertEquals("Node [ip=null, port=0]", result.toString());
		assertEquals(null, result.getIp());
		assertEquals(0, result.getPort());
		assertEquals(1, result.getNodeId());
	}

	/**
	 * Run the int getOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetOpId_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		int result = fixture.getOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getOriginalOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetOriginalOpId_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		int result = fixture.getOriginalOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the boolean isStatefull() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsStatefull_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		boolean result = fixture.isStatefull();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isStatefull() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsStatefull_2()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, false);

		boolean result = fixture.isStatefull();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the void setInC(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetInC_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);
		int inC = 1;

		fixture.setInC(inC);

		// add additional test code here
	}

	/**
	 * Run the void setInD(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetInD_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);
		int inD = 1;

		fixture.setInD(inD);

		// add additional test code here
	}

	/**
	 * Run the void setMyNode(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetMyNode_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);
		Node myNode = new Node(1);

		fixture.setMyNode(myNode);

		// add additional test code here
	}

	/**
	 * Run the OperatorStaticInformation setNode(Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetNode_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);
		Node newNode = new Node(1);

		OperatorStaticInformation result = fixture.setNode(newNode);

		// add additional test code here
		assertNotNull(result);
		assertEquals("node: Node [ip=null, port=0]inC: 1inD: 1", result.toString());
		assertEquals(1, result.getInC());
		assertEquals(true, result.isStatefull());
		assertEquals(1, result.getInD());
		assertEquals(1, result.getOriginalOpId());
		assertEquals(1, result.getOpId());
	}

	/**
	 * Run the void setStatefull(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetStatefull_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);
		boolean isStatefull = true;

		fixture.setStatefull(isStatefull);

		// add additional test code here
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testToString_1()
		throws Exception {
		OperatorStaticInformation fixture = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		String result = fixture.toString();

		// add additional test code here
		assertEquals("node: Node [ip=null, port=0]inC: 1inD: 1", result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
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
	 * @generatedBy CodePro at 18/10/13 19:09
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
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(OperatorStaticInformationTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new OperatorStaticInformationTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}