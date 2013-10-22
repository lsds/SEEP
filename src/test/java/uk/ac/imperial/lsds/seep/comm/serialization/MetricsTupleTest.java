package uk.ac.imperial.lsds.seep.comm.serialization;

import junit.framework.*;

/**
 * The class <code>MetricsTupleTest</code> contains tests for the class <code>{@link MetricsTuple}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:13
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class MetricsTupleTest extends TestCase {
	/**
	 * Run the MetricsTuple() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMetricsTuple_1()
		throws Exception {

		MetricsTuple result = new MetricsTuple();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0L, result.getInputQueueEvents());
		assertEquals(0L, result.getNumberIncomingDataHandlerWorkers());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the long getInputQueueEvents() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetInputQueueEvents_1()
		throws Exception {
		MetricsTuple fixture = new MetricsTuple();
		fixture.setInputQueueEvents(1L);
		fixture.setOpId(1);
		fixture.setNumberIncomingDataHandlerWorkers(1L);

		long result = fixture.getInputQueueEvents();

		// add additional test code here
		assertEquals(1L, result);
	}

	/**
	 * Run the long getNumberIncomingDataHandlerWorkers() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetNumberIncomingDataHandlerWorkers_1()
		throws Exception {
		MetricsTuple fixture = new MetricsTuple();
		fixture.setInputQueueEvents(1L);
		fixture.setOpId(1);
		fixture.setNumberIncomingDataHandlerWorkers(1L);

		long result = fixture.getNumberIncomingDataHandlerWorkers();

		// add additional test code here
		assertEquals(1L, result);
	}

	/**
	 * Run the int getOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetOpId_1()
		throws Exception {
		MetricsTuple fixture = new MetricsTuple();
		fixture.setInputQueueEvents(1L);
		fixture.setOpId(1);
		fixture.setNumberIncomingDataHandlerWorkers(1L);

		int result = fixture.getOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the void setInputQueueEvents(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetInputQueueEvents_1()
		throws Exception {
		MetricsTuple fixture = new MetricsTuple();
		fixture.setInputQueueEvents(1L);
		fixture.setOpId(1);
		fixture.setNumberIncomingDataHandlerWorkers(1L);
		long inputQueueEvents = 1L;

		fixture.setInputQueueEvents(inputQueueEvents);

		// add additional test code here
	}

	/**
	 * Run the void setNumberIncomingDataHandlerWorkers(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetNumberIncomingDataHandlerWorkers_1()
		throws Exception {
		MetricsTuple fixture = new MetricsTuple();
		fixture.setInputQueueEvents(1L);
		fixture.setOpId(1);
		fixture.setNumberIncomingDataHandlerWorkers(1L);
		long numberIncomingdataHandlerWorkers2 = 1L;

		fixture.setNumberIncomingDataHandlerWorkers(numberIncomingdataHandlerWorkers2);

		// add additional test code here
	}

	/**
	 * Run the void setOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetOpId_1()
		throws Exception {
		MetricsTuple fixture = new MetricsTuple();
		fixture.setInputQueueEvents(1L);
		fixture.setOpId(1);
		fixture.setNumberIncomingDataHandlerWorkers(1L);
		int opId = 1;

		fixture.setOpId(opId);

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
	 * @generatedBy CodePro at 18/10/13 19:13
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
	 * @generatedBy CodePro at 18/10/13 19:13
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
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(MetricsTupleTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new MetricsTupleTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}