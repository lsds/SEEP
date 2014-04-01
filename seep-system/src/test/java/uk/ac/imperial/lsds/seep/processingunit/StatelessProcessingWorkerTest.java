package uk.ac.imperial.lsds.seep.processingunit;

import junit.framework.*;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;

/**
 * The class <code>StatelessProcessingWorkerTest</code> contains tests for the class <code>{@link StatelessProcessingWorker}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:01
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class StatelessProcessingWorkerTest extends TestCase {
	/**
	 * Run the StatelessProcessingWorker(DataStructureAdapter,Operator) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testStatelessProcessingWorker_1()
		throws Exception {
		DataStructureAdapter dsa = new DataStructureAdapter();
		Operator runningOp = null;

		StatelessProcessingWorker result = new StatelessProcessingWorker(dsa, runningOp);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testRun_1()
		throws Exception {
		StatelessProcessingWorker fixture = new StatelessProcessingWorker(new DataStructureAdapter(), (Operator) null);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingWorker.run(StatelessProcessingWorker.java:38)
	}

	/**
	 * Run the void run() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public void testRun_2()
		throws Exception {
		StatelessProcessingWorker fixture = new StatelessProcessingWorker(new DataStructureAdapter(), (Operator) null);

		fixture.run();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.processingunit.StatelessProcessingWorker.run(StatelessProcessingWorker.java:38)
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:01
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
	 * @generatedBy CodePro at 18/10/13 19:01
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
	 * @generatedBy CodePro at 18/10/13 19:01
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(StatelessProcessingWorkerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new StatelessProcessingWorkerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}