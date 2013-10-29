package uk.ac.imperial.lsds.seep.processingunit;

import java.util.concurrent.Semaphore;
import uk.ac.imperial.lsds.seep.elastic.MockState;
import junit.framework.*;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.state.State;

/**
 * The class <code>StatefulProcessingWorkerTest</code> contains tests for the class <code>{@link StatefulProcessingWorker}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:02
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class StatefulProcessingWorkerTest extends TestCase {
	/**
	 * Run the StatefulProcessingWorker(DataStructureAdapter,Operator,State,Semaphore) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testStatefulProcessingWorker_1()
		throws Exception {
		DataStructureAdapter dsa = new DataStructureAdapter();
		Operator op = null;
		State s = new MockState();
		Semaphore executorMutex = new Semaphore(1);

		StatefulProcessingWorker result = new StatefulProcessingWorker(dsa, op, s, executorMutex);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testRun_1()
		throws Exception {
		StatefulProcessingWorker fixture = new StatefulProcessingWorker(new DataStructureAdapter(), (Operator) null, new MockState(), new Semaphore(1));

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingWorker.run(StatefulProcessingWorker.java:53)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testRun_2()
		throws Exception {
		StatefulProcessingWorker fixture = new StatefulProcessingWorker(new DataStructureAdapter(), (Operator) null, new MockState(), new Semaphore(1));

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingWorker.run(StatefulProcessingWorker.java:53)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public void testRun_3()
		throws Exception {
		StatefulProcessingWorker fixture = new StatefulProcessingWorker(new DataStructureAdapter(), (Operator) null, new MockState(), new Semaphore(1));

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingWorker.run(StatefulProcessingWorker.java:53)
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:02
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
	 * @generatedBy CodePro at 18/10/13 19:02
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
	 * @generatedBy CodePro at 18/10/13 19:02
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(StatefulProcessingWorkerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new StatefulProcessingWorkerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}