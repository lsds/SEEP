package uk.ac.imperial.lsds.seep.reliable;

import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import java.util.ArrayList;
import junit.framework.*;

/**
 * The class <code>BackupSessionInfoTest</code> contains tests for the class <code>{@link BackupSessionInfo}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:10
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class BackupSessionInfoTest extends TestCase {
	/**
	 * Run the BackupSessionInfo(int,ArrayList<FileChannel>,BackupHandler,String,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testBackupSessionInfo_1()
		throws Exception {
		int opId = 1;
		ArrayList<FileChannel> lastBackupHandlers = new ArrayList();
		BackupHandler backupHandler = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		String sessionName = "";
		int transNumber = 1;

		BackupSessionInfo result = new BackupSessionInfo(opId, lastBackupHandlers, backupHandler, sessionName, transNumber);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
		assertNotNull(result);
	}

	/**
	 * Run the int getOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetOpId_1()
		throws Exception {
		BackupSessionInfo fixture = new BackupSessionInfo(1, new ArrayList(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		int result = fixture.getOpId();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
		assertEquals(0, result);
	}

	/**
	 * Run the String getSessionName() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetSessionName_1()
		throws Exception {
		BackupSessionInfo fixture = new BackupSessionInfo(1, new ArrayList(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		String result = fixture.getSessionName();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
		assertNotNull(result);
	}

	/**
	 * Run the int getTransNumber() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetTransNumber_1()
		throws Exception {
		BackupSessionInfo fixture = new BackupSessionInfo(1, new ArrayList(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		int result = fixture.getTransNumber();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
		assertEquals(0, result);
	}

	/**
	 * Run the void incrementTransNumber() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testIncrementTransNumber_1()
		throws Exception {
		BackupSessionInfo fixture = new BackupSessionInfo(1, new ArrayList(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.incrementTransNumber();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
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
			junit.textui.TestRunner.run(BackupSessionInfoTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new BackupSessionInfoTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}