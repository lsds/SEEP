package uk.ac.imperial.lsds.seep.reliable;

import java.net.InetAddress;
import java.net.Socket;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import java.net.URL;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.RuntimeClassLoader;

import java.net.URLClassLoader;
import junit.framework.*;

/**
 * The class <code>BackupHandlerWorkerTest</code> contains tests for the class <code>{@link BackupHandlerWorker}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:00
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class BackupHandlerWorkerTest extends TestCase {
	/**
	 * Run the BackupHandlerWorker(int,Socket,BackupHandler,String,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testBackupHandlerWorker_1()
		throws Exception {
		int opId = 1;
		Socket incomingSocket = new Socket();
		BackupHandler owner = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);
		String sessionName = "";
		int transNumber = 1;

		BackupHandlerWorker result = new BackupHandlerWorker(opId, incomingSocket, owner, sessionName, transNumber);

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
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_1()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_2()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_3()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_4()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_5()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_6()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_7()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_8()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_9()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_10()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_11()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_12()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void memoryMappedFile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testMemoryMappedFile_13()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

		fixture.memoryMappedFile();

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
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRun_1()
		throws Exception {
		BackupHandlerWorker fixture = new BackupHandlerWorker(1, new Socket(), new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1), "", 1);

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
			junit.textui.TestRunner.run(BackupHandlerWorkerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new BackupHandlerWorkerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}