package uk.ac.imperial.lsds.seep.comm;

import java.net.InetAddress;
import java.net.Socket;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.utils.dynamiccodedeployer.RuntimeClassLoader;
import java.net.URL;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import java.net.URLClassLoader;
import junit.framework.*;

/**
 * The class <code>ControlHandlerWorkerTest</code> contains tests for the class <code>{@link ControlHandlerWorker}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:00
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ControlHandlerWorkerTest extends TestCase {
	/**
	 * Run the ControlHandlerWorker(Socket,CoreRE) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testControlHandlerWorker_1()
		throws Exception {
		Socket incomingSocket = new Socket();
		CoreRE owner = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));

		ControlHandlerWorker result = new ControlHandlerWorker(incomingSocket, owner);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRun_1()
		throws Exception {
		ControlHandlerWorker fixture = new ControlHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRun_2()
		throws Exception {
		ControlHandlerWorker fixture = new ControlHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRun_3()
		throws Exception {
		ControlHandlerWorker fixture = new ControlHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRun_4()
		throws Exception {
		ControlHandlerWorker fixture = new ControlHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRun_5()
		throws Exception {
		ControlHandlerWorker fixture = new ControlHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRun_6()
		throws Exception {
		ControlHandlerWorker fixture = new ControlHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRun_7()
		throws Exception {
		ControlHandlerWorker fixture = new ControlHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.run();

		// add additional test code here
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRun_8()
		throws Exception {
		ControlHandlerWorker fixture = new ControlHandlerWorker(new Socket(), new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.run();

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
			junit.textui.TestRunner.run(ControlHandlerWorkerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ControlHandlerWorkerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}