package uk.ac.imperial.lsds.seep.reliable;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import uk.ac.imperial.lsds.seep.utils.dynamiccodedeployer.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import java.util.ArrayList;
import junit.framework.*;

/**
 * The class <code>BackupHandlerTest</code> contains tests for the class <code>{@link BackupHandler}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:10
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class BackupHandlerTest extends TestCase {
	/**
	 * Run the BackupHandler(CoreRE,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testBackupHandler_1()
		throws Exception {
		CoreRE owner = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));
		int port = 1;

		BackupHandler result = new BackupHandler(owner, port);

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
	 * Run the void addBackupHandler(int,FileChannel,File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testAddBackupHandler_1()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;
		FileChannel fc = FileChannel.open((Path) null, new OpenOption[] {null});
		File f = new File("");

		fixture.addBackupHandler(opId, fc, f);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void closeSession(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testCloseSession_1()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.closeSession(opId, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void closeSession(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testCloseSession_2()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.closeSession(opId, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void closeSession(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testCloseSession_3()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.closeSession(opId, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void closeSession(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testCloseSession_4()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.closeSession(opId, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void closeSession(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testCloseSession_5()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.closeSession(opId, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void closeSession(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testCloseSession_6()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.closeSession(opId, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void closeSession(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testCloseSession_7()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.closeSession(opId, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the boolean getGoOn() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetGoOn_1()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);

		boolean result = fixture.getGoOn();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
		assertTrue(result);
	}

	/**
	 * Run the boolean getGoOn() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetGoOn_2()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(false);

		boolean result = fixture.getGoOn();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
		assertTrue(result);
	}

	/**
	 * Run the String getLastBackupSessionName(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetLastBackupSessionName_1()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;

		String result = fixture.getLastBackupSessionName(opId);

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
	 * Run the CoreRE getOwner() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetOwner_1()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);

		CoreRE result = fixture.getOwner();

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
	 * Run the ArrayList<File> getSessionFileHandlers(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetSessionFileHandlers_1()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;

		ArrayList<File> result = fixture.getSessionFileHandlers(opId);

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
	 * Run the void openSession(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testOpenSession_1()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.openSession(opId, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void openSession(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testOpenSession_2()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		int opId = 1;
		InetAddress remoteAddress = InetAddress.getLocalHost();

		fixture.openSession(opId, remoteAddress);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testRun_1()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(false);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testRun_2()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testRun_3()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testRun_4()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(false);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void setGoOn(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testSetGoOn_1()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		boolean goOn = true;

		fixture.setGoOn(goOn);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void setOwner(CoreRE) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testSetOwner_1()
		throws Exception {
		BackupHandler fixture = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		fixture.setGoOn(true);
		CoreRE owner = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));

		fixture.setOwner(owner);

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
			junit.textui.TestRunner.run(BackupHandlerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new BackupHandlerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}