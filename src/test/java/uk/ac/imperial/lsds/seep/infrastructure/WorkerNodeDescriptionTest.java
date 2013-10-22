package uk.ac.imperial.lsds.seep.infrastructure;

import java.net.InetAddress;
import junit.framework.*;

/**
 * The class <code>WorkerNodeDescriptionTest</code> contains tests for the class <code>{@link WorkerNodeDescription}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:00
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class WorkerNodeDescriptionTest extends TestCase {
	/**
	 * Run the WorkerNodeDescription(InetAddress,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testWorkerNodeDescription_1()
		throws Exception {
		InetAddress ip = InetAddress.getLocalHost();
		int ownPort = 1;

		WorkerNodeDescription result = new WorkerNodeDescription(ip, ownPort);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getOwnPort());
		assertEquals(1074740524, result.getNodeId());
	}

	/**
	 * Run the InetAddress getIp() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testGetIp_1()
		throws Exception {
		WorkerNodeDescription fixture = new WorkerNodeDescription(InetAddress.getLocalHost(), 1);

		InetAddress result = fixture.getIp();

		// add additional test code here
		assertNotNull(result);
		assertEquals("hedera.doc.ic.ac.uk/146.169.5.130", result.toString());
		assertEquals(false, result.isLinkLocalAddress());
		assertEquals(false, result.isMCGlobal());
		assertEquals(false, result.isLoopbackAddress());
		assertEquals(false, result.isMCLinkLocal());
		assertEquals(false, result.isMCOrgLocal());
		assertEquals("hedera.doc.ic.ac.uk", result.getCanonicalHostName());
		assertEquals(false, result.isMCNodeLocal());
		assertEquals(false, result.isMCSiteLocal());
		assertEquals(false, result.isMulticastAddress());
		assertEquals(false, result.isSiteLocalAddress());
		assertEquals(false, result.isAnyLocalAddress());
		assertEquals("146.169.5.130", result.getHostAddress());
		assertEquals("hedera.doc.ic.ac.uk", result.getHostName());
	}

	/**
	 * Run the int getNodeId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testGetNodeId_1()
		throws Exception {
		WorkerNodeDescription fixture = new WorkerNodeDescription(InetAddress.getLocalHost(), 1);

		int result = fixture.getNodeId();

		// add additional test code here
		assertEquals(98356805, result);
	}

	/**
	 * Run the int getOwnPort() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testGetOwnPort_1()
		throws Exception {
		WorkerNodeDescription fixture = new WorkerNodeDescription(InetAddress.getLocalHost(), 1);

		int result = fixture.getOwnPort();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the void setIp(InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testSetIp_1()
		throws Exception {
		WorkerNodeDescription fixture = new WorkerNodeDescription(InetAddress.getLocalHost(), 1);
		InetAddress ip = InetAddress.getLocalHost();

		fixture.setIp(ip);

		// add additional test code here
	}

	/**
	 * Run the void setOwnPort(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testSetOwnPort_1()
		throws Exception {
		WorkerNodeDescription fixture = new WorkerNodeDescription(InetAddress.getLocalHost(), 1);
		int ownPort = 1;

		fixture.setOwnPort(ownPort);

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
			junit.textui.TestRunner.run(WorkerNodeDescriptionTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new WorkerNodeDescriptionTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}