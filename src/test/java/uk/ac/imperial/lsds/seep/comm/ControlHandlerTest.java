package uk.ac.imperial.lsds.seep.comm;

import java.net.InetAddress;
import java.net.URL;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.utils.dynamiccodedeployer.RuntimeClassLoader;
import java.net.URLClassLoader;
import junit.framework.*;

/**
 * The class <code>ControlHandlerTest</code> contains tests for the class <code>{@link ControlHandler}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:08
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ControlHandlerTest extends TestCase {
	/**
	 * Run the ControlHandler(CoreRE,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testControlHandler_1()
		throws Exception {
		CoreRE owner = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		int connPort = 1;

		ControlHandler result = new ControlHandler(owner, connPort);

		// add additional test code here
		assertNotNull(result);
		assertEquals(true, result.getGoOn());
		assertEquals(1, result.getConnPort());
	}

	/**
	 * Run the int getConnPort() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetConnPort_1()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);

		int result = fixture.getConnPort();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the boolean getGoOn() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetGoOn_1()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);

		boolean result = fixture.getGoOn();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean getGoOn() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetGoOn_2()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(false);

		boolean result = fixture.getGoOn();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the CoreRE getOwner() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetOwner_1()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);

		CoreRE result = fixture.getOwner();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getDSA());
		assertEquals(-1, result.getBackupUpstreamIndex());
		assertEquals(true, result.killHandlers());
		assertEquals(null, result.getInitialStarTopology());
		assertEquals(null, result.getControlDispatcher());
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testRun_1()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(false);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.ControlHandler.run(ControlHandler.java:80)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testRun_2()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.ControlHandler.run(ControlHandler.java:80)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testRun_3()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.ControlHandler.run(ControlHandler.java:80)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testRun_4()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.ControlHandler.run(ControlHandler.java:80)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testRun_5()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(false);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.ControlHandler.run(ControlHandler.java:80)
	}

	/**
	 * Run the void setConnPort(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetConnPort_1()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int connPort = 1;

		fixture.setConnPort(connPort);

		// add additional test code here
	}

	/**
	 * Run the void setGoOn(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetGoOn_1()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		boolean goOn = true;

		fixture.setGoOn(goOn);

		// add additional test code here
	}

	/**
	 * Run the void setOwner(CoreRE) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetOwner_1()
		throws Exception {
		ControlHandler fixture = new ControlHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		CoreRE owner = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));

		fixture.setOwner(owner);

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
	 * @generatedBy CodePro at 18/10/13 19:08
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
	 * @generatedBy CodePro at 18/10/13 19:08
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
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(ControlHandlerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ControlHandlerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}