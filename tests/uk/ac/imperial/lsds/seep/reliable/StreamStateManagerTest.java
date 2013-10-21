package uk.ac.imperial.lsds.seep.reliable;

import junit.framework.*;
import uk.ac.imperial.lsds.seep.infrastructure.api.datastructure.SeepMap;
import uk.ac.imperial.lsds.seep.operator.Streamable;

/**
 * The class <code>StreamStateManagerTest</code> contains tests for the class <code>{@link StreamStateManager}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:08
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class StreamStateManagerTest extends TestCase {
	/**
	 * Run the StreamStateManager(Streamable) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testStreamStateManager_1()
		throws Exception {
		Streamable state = new SeepMap();

		StreamStateManager result = new StreamStateManager(state);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.reliable.StreamStateManager.<init>(StreamStateManager.java:30)
		assertNotNull(result);
	}

	/**
	 * Run the StreamStateManager(Streamable) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testStreamStateManager_2()
		throws Exception {
		Streamable state = new SeepMap();

		StreamStateManager result = new StreamStateManager(state);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.reliable.StreamStateManager.<init>(StreamStateManager.java:30)
		assertNotNull(result);
	}

	/**
	 * Run the StreamStateManager(Streamable) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testStreamStateManager_3()
		throws Exception {
		try {
			Streamable state = new SeepMap();

			StreamStateManager result = new StreamStateManager(state);

			// add additional test code here
			fail("The exception java.lang.NumberFormatException should have been thrown.");
		} catch (java.lang.NumberFormatException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the MemoryChunk getChunk() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetChunk_1()
		throws Exception {
		StreamStateManager fixture = new StreamStateManager(new SeepMap());

		MemoryChunk result = fixture.getChunk();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.reliable.StreamStateManager.<init>(StreamStateManager.java:30)
		assertNotNull(result);
	}

	/**
	 * Run the MemoryChunk getChunk() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetChunk_2()
		throws Exception {
		StreamStateManager fixture = new StreamStateManager(new SeepMap());

		MemoryChunk result = fixture.getChunk();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.reliable.StreamStateManager.<init>(StreamStateManager.java:30)
		assertNotNull(result);
	}

	/**
	 * Run the MemoryChunk getChunk() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetChunk_3()
		throws Exception {
		StreamStateManager fixture = new StreamStateManager(new SeepMap());

		MemoryChunk result = fixture.getChunk();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.reliable.StreamStateManager.<init>(StreamStateManager.java:30)
		assertNotNull(result);
	}

	/**
	 * Run the int getTotalNumberChunks() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetTotalNumberChunks_1()
		throws Exception {
		StreamStateManager fixture = new StreamStateManager(new SeepMap());

		int result = fixture.getTotalNumberChunks();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.<init>(Integer.java:677)
		//       at uk.ac.imperial.lsds.seep.reliable.StreamStateManager.<init>(StreamStateManager.java:30)
		assertEquals(0, result);
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
			junit.textui.TestRunner.run(StreamStateManagerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new StreamStateManagerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}