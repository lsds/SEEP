package uk.ac.imperial.lsds.seep.runtimeengine;

import java.net.InetAddress;
import java.net.URL;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;
import java.net.URLClassLoader;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.elastic.MockState;
import uk.ac.imperial.lsds.seep.reliable.BackupHandler;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitRI;
import java.util.ArrayList;
import junit.framework.*;
import uk.ac.imperial.lsds.seep.processingunit.IProcessingUnit;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.utils.dynamiccodedeployer.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.processingunit.PUContext;
import uk.ac.imperial.lsds.seep.operator.State;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;

/**
 * The class <code>CoreProcessingLogicTest</code> contains tests for the class <code>{@link CoreProcessingLogic}</code>.
 *
 * @generatedBy CodePro at 18/10/13 18:59
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class CoreProcessingLogicTest extends TestCase {
	/**
	 * Run the CoreProcessingLogic() constructor test.
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testCoreProcessingLogic_1()
		throws Exception {
		CoreProcessingLogic result = new CoreProcessingLogic();
		assertNotNull(result);
		// add additional test code here
	}

	/**
	 * Run the void backupRoutingInformation(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testBackupRoutingInformation_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int oldOpId = 1;

		fixture.backupRoutingInformation(oldOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit.getRouterIndexesInformation(StatefulProcessingUnit.java:452)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.backupRoutingInformation(CoreProcessingLogic.java:344)
	}

	/**
	 * Run the void backupRoutingInformation(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testBackupRoutingInformation_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int oldOpId = 1;

		fixture.backupRoutingInformation(oldOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit.getRouterIndexesInformation(StatefulProcessingUnit.java:452)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.backupRoutingInformation(CoreProcessingLogic.java:344)
	}

	/**
	 * Run the int configureNewDownstreamStatefulOperatorPartition(int,int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testConfigureNewDownstreamStatefulOperatorPartition_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int oldOpId = 1;
		int newOpId = 1;
		int oldOpIndex = 1;
		int newOpIndex = 1;

		int result = fixture.configureNewDownstreamStatefulOperatorPartition(oldOpId, newOpId, oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.configureNewDownstreamStatefulOperatorPartition(CoreProcessingLogic.java:325)
		assertEquals(0, result);
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_3()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_4()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_5()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_6()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_7()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_8()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_9()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_10()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_11()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_12()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void directReplayStateFailure(int,BackupHandler) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testDirectReplayStateFailure_13()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		BackupHandler bh = new BackupHandler(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), 1);

		fixture.directReplayStateFailure(opId, bh);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.File.mkdir(File.java:1237)
		//       at java.io.File.mkdirs(File.java:1266)
		//       at uk.ac.imperial.lsds.seep.reliable.BackupHandler.<init>(BackupHandler.java:83)
	}

	/**
	 * Run the void handleNewChunk(StateChunk) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testHandleNewChunk_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		StateChunk stateChunk = new StateChunk(1, 1, 1, 1, (MemoryChunk) null, 1);

		fixture.handleNewChunk(stateChunk);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit.resetState(StatefulProcessingUnit.java:791)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.handleNewChunk(CoreProcessingLogic.java:663)
	}

	/**
	 * Run the void handleNewChunk(StateChunk) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testHandleNewChunk_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		StateChunk stateChunk = new StateChunk(1, 1, 1, 1, (MemoryChunk) null, 1);

		fixture.handleNewChunk(stateChunk);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit.resetState(StatefulProcessingUnit.java:791)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.handleNewChunk(CoreProcessingLogic.java:663)
	}

	/**
	 * Run the void handleNewChunk(StateChunk) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testHandleNewChunk_3()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		StateChunk stateChunk = new StateChunk(1, 1, 1, 1, (MemoryChunk) null, 1);

		fixture.handleNewChunk(stateChunk);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit.resetState(StatefulProcessingUnit.java:791)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.handleNewChunk(CoreProcessingLogic.java:663)
	}

	/**
	 * Run the void handleNewChunk(StateChunk) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testHandleNewChunk_4()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		StateChunk stateChunk = new StateChunk(1, 1, 1, 1, (MemoryChunk) null, 1);

		fixture.handleNewChunk(stateChunk);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit.resetState(StatefulProcessingUnit.java:791)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.handleNewChunk(CoreProcessingLogic.java:663)
	}

	/**
	 * Run the void initializeSerialization() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testInitializeSerialization_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;

		fixture.initializeSerialization();

		// add additional test code here
	}

	/**
	 * Run the void installRI(InitRI) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testInstallRI_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		InitRI initRI = new InitRI(1, new ArrayList(), new ArrayList());

		fixture.installRI(initRI);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.installRI(CoreProcessingLogic.java:140)
	}

	/**
	 * Run the void installRI(InitRI) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testInstallRI_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		InitRI initRI = new InitRI(1, new ArrayList(), new ArrayList());

		fixture.installRI(initRI);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.installRI(CoreProcessingLogic.java:140)
	}

	/**
	 * Run the int[] manageDownstreamDistributedScaleOut(int,int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testManageDownstreamDistributedScaleOut_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int oldOpId = 1;
		int newOpId = 1;
		int oldOpIndex = 1;
		int newOpIndex = 1;

		int[] result = fixture.manageDownstreamDistributedScaleOut(oldOpId, newOpId, oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit.stopConnection(StatefulProcessingUnit.java:431)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.manageDownstreamDistributedScaleOut(CoreProcessingLogic.java:273)
		assertNotNull(result);
	}

	/**
	 * Run the void processAck(Ack) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testProcessAck_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		Ack ct = new Ack(1, 1L);

		fixture.processAck(ct);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.processAck(CoreProcessingLogic.java:189)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.processAck(CoreProcessingLogic.java:183)
	}

	/**
	 * Run the void processBackupState(BackupOperatorState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testProcessBackupState_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		BackupOperatorState ct = new BackupOperatorState();
		ct.setOpId(1);
		ct.setState(new MockState());

		fixture.processBackupState(ct);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:287)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.processBackupState(CoreProcessingLogic.java:167)
	}

	/**
	 * Run the void processBackupState(BackupOperatorState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testProcessBackupState_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		BackupOperatorState ct = new BackupOperatorState();
		ct.setOpId(1);
		ct.setState(new MockState());

		fixture.processBackupState(ct);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:287)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.processBackupState(CoreProcessingLogic.java:167)
	}

	/**
	 * Run the void processInitState(InitOperatorState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testProcessInitState_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		InitOperatorState ct = new InitOperatorState(1, new MockState());

		fixture.processInitState(ct);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.manageBackupUpstreamIndex(CoreRE.java:674)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.processInitState(CoreProcessingLogic.java:377)
	}

	/**
	 * Run the void propagateNewKeys(int[],int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testPropagateNewKeys_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int[] bounds = new int[] {1, 1};
		int oldOpIndex = 1;
		int newOpIndex = 1;

		fixture.propagateNewKeys(bounds, oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.propagateNewKeys(CoreProcessingLogic.java:697)
	}

	/**
	 * Run the void replayState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testReplayState_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;

		fixture.replayState(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:287)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.replayState(CoreProcessingLogic.java:357)
	}

	/**
	 * Run the void replayState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testReplayState_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;

		fixture.replayState(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.PUContext.getCCIfromOpId(PUContext.java:287)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.replayState(CoreProcessingLogic.java:357)
	}

	/**
	 * Run the void scaleOut(ScaleOutInfo,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testScaleOut_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		ScaleOutInfo scaleOutInfo = new ScaleOutInfo(1, 1, false);
		int newOpIndex = 1;
		int oldOpIndex = 1;

		fixture.scaleOut(scaleOutInfo, newOpIndex, oldOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.scaleOut(CoreProcessingLogic.java:292)
	}

	/**
	 * Run the void scaleOut(ScaleOutInfo,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testScaleOut_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		ScaleOutInfo scaleOutInfo = new ScaleOutInfo(1, 1, true);
		int newOpIndex = 1;
		int oldOpIndex = 1;

		fixture.scaleOut(scaleOutInfo, newOpIndex, oldOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.configureNewDownstreamStatefulOperatorPartition(CoreProcessingLogic.java:325)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.scaleOutStatefulOperator(CoreProcessingLogic.java:298)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.scaleOut(CoreProcessingLogic.java:287)
	}

	/**
	 * Run the void scaleOutStatefulOperator(int,int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testScaleOutStatefulOperator_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int oldOpId = 1;
		int newOpId = 1;
		int oldOpIndex = 1;
		int newOpIndex = 1;

		fixture.scaleOutStatefulOperator(oldOpId, newOpId, oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.configureNewDownstreamStatefulOperatorPartition(CoreProcessingLogic.java:325)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.scaleOutStatefulOperator(CoreProcessingLogic.java:298)
	}

	/**
	 * Run the void scaleOutStatefulOperator(int,int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testScaleOutStatefulOperator_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int oldOpId = 1;
		int newOpId = 1;
		int oldOpIndex = 1;
		int newOpIndex = 1;

		fixture.scaleOutStatefulOperator(oldOpId, newOpId, oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.configureNewDownstreamStatefulOperatorPartition(CoreProcessingLogic.java:325)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.scaleOutStatefulOperator(CoreProcessingLogic.java:298)
	}

	/**
	 * Run the void sendInitialStateBackup() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSendInitialStateBackup_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;

		fixture.sendInitialStateBackup();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.sendInitialStateBackup(CoreProcessingLogic.java:400)
	}

	/**
	 * Run the void sendInitialStateBackup() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSendInitialStateBackup_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;

		fixture.sendInitialStateBackup();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.sendInitialStateBackup(CoreProcessingLogic.java:400)
	}

	/**
	 * Run the void sendInitialStateBackup() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSendInitialStateBackup_3()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;

		fixture.sendInitialStateBackup();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.sendInitialStateBackup(CoreProcessingLogic.java:400)
	}

	/**
	 * Run the void sendInitialStateBackup() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSendInitialStateBackup_4()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;

		fixture.sendInitialStateBackup();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.sendInitialStateBackup(CoreProcessingLogic.java:400)
	}

	/**
	 * Run the void sendRoutingInformation(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSendRoutingInformation_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		String operatorType = "";

		fixture.sendRoutingInformation(opId, operatorType);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.sendRoutingInformation(CoreProcessingLogic.java:146)
	}

	/**
	 * Run the void sendRoutingInformation(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSendRoutingInformation_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		String operatorType = "";

		fixture.sendRoutingInformation(opId, operatorType);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.sendRoutingInformation(CoreProcessingLogic.java:146)
	}

	/**
	 * Run the void sendRoutingInformation(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSendRoutingInformation_3()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		String operatorType = "";

		fixture.sendRoutingInformation(opId, operatorType);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.sendRoutingInformation(CoreProcessingLogic.java:146)
	}

	/**
	 * Run the void sendRoutingInformation(int,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSendRoutingInformation_4()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int opId = 1;
		String operatorType = "";

		fixture.sendRoutingInformation(opId, operatorType);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.sendRoutingInformation(CoreProcessingLogic.java:146)
	}

	/**
	 * Run the void setOpContext(PUContext) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSetOpContext_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		PUContext puCtx = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		fixture.setOpContext(puCtx);

		// add additional test code here
	}

	/**
	 * Run the void setOwner(CoreRE) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSetOwner_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		CoreRE owner = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));

		fixture.setOwner(owner);

		// add additional test code here
	}

	/**
	 * Run the void setProcessingUnit(IProcessingUnit) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSetProcessingUnit_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		IProcessingUnit processingUnit = new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true);

		fixture.setProcessingUnit(processingUnit);

		// add additional test code here
	}

	/**
	 * Run the void splitState(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSplitState_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int oldOpId = 1;
		int newOpId = 1;
		int key = 1;

		fixture.splitState(oldOpId, newOpId, key);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.splitState(CoreProcessingLogic.java:217)
	}

	/**
	 * Run the void splitState(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSplitState_2()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		int oldOpId = 1;
		int newOpId = 1;
		int key = 1;

		fixture.splitState(oldOpId, newOpId, key);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.runtimeengine.CoreProcessingLogic.splitState(CoreProcessingLogic.java:217)
	}

	/**
	 * Run the void storeBackupRI(BackupRI) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testStoreBackupRI_1()
		throws Exception {
		CoreProcessingLogic fixture = new CoreProcessingLogic();
		fixture.setOpContext(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		fixture.setProcessingUnit(new StatefulProcessingUnit(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))), true));
		fixture.setOwner(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		fixture.mergeTotal = 1L;
		BackupRI backupRI = new BackupRI(1, new ArrayList(), new ArrayList(), "");

		fixture.storeBackupRI(backupRI);

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
	 * @generatedBy CodePro at 18/10/13 18:59
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
	 * @generatedBy CodePro at 18/10/13 18:59
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
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(CoreProcessingLogicTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new CoreProcessingLogicTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}