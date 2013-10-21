package uk.ac.imperial.lsds.seep.infrastructure.api.datastructure;

import java.util.ArrayList;
import java.util.Iterator;
import junit.framework.*;

/**
 * The class <code>SeepMapTest</code> contains tests for the class <code>{@link SeepMap}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:13
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class SeepMapTest extends TestCase {
	/**
	 * Run the SeepMap() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSeepMap_1()
		throws Exception {

		SeepMap result = new SeepMap();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the SeepMap(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSeepMap_2()
		throws Exception {
		int initialSize = 1;

		SeepMap result = new SeepMap(initialSize);

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the void appendChunk(ArrayList<Object>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testAppendChunk_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		ArrayList<Object> chunk = null;

		fixture.appendChunk(chunk);

		// add additional test code here
	}

	/**
	 * Run the void appendChunk(ArrayList<Object>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testAppendChunk_2()
		throws Exception {
		SeepMap fixture = new SeepMap();
		ArrayList chunk = new ArrayList();
		chunk.add(new Object());

		fixture.appendChunk(chunk);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.ClassCastException: java.lang.Object cannot be cast to java.lang.String
		//       at uk.ac.imperial.lsds.seep.infrastructure.api.datastructure.SeepMap.appendChunk(SeepMap.java:172)
	}

	/**
	 * Run the void appendChunk(ArrayList<Object>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testAppendChunk_3()
		throws Exception {
		SeepMap fixture = new SeepMap();
		ArrayList<Object> chunk = new ArrayList();

		fixture.appendChunk(chunk);

		// add additional test code here
	}

	/**
	 * Run the Object get(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGet_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		Object result = fixture.get(key);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the Object get(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGet_2()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		Object result = fixture.get(key);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the Object get(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGet_3()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		Object result = fixture.get(key);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the Object getFromBackup(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetFromBackup_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		Object result = fixture.getFromBackup(key);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the Iterator getIterator() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetIterator_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		Iterator result = fixture.getIterator();

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.hasNext());
	}

	/**
	 * Run the int getSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetSize_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		int result = fixture.getSize();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getTotalNumberOfChunks(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetTotalNumberOfChunks_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		int chunkSize = 1;

		int result = fixture.getTotalNumberOfChunks(chunkSize);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the void lockStateAccess() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testLockStateAccess_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.lockStateAccess();

		// add additional test code here
	}

	/**
	 * Run the void lockStateAccess() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testLockStateAccess_2()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.lockStateAccess();

		// add additional test code here
	}

	/**
	 * Run the Object put(Object,Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testPut_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();
		Object value = new Object();

		Object result = fixture.put(key, value);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the Object put(Object,Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testPut_2()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();
		Object value = new Object();

		Object result = fixture.put(key, value);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testReconcile_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testReconcile_2()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testReconcile_3()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testReconcile_4()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void releaseStateAccess() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testReleaseStateAccess_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.releaseStateAccess();

		// add additional test code here
	}

	/**
	 * Run the void reset() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testReset_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.reset();

		// add additional test code here
	}

	/**
	 * Run the void setDirtyMode(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetDirtyMode_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		boolean newValue = true;

		fixture.setDirtyMode(newValue);

		// add additional test code here
	}

	/**
	 * Run the ArrayList<Object> streamSplitState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testStreamSplitState_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		int chunkSize = 1;

		ArrayList<Object> result = fixture.streamSplitState(chunkSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.infrastructure.api.datastructure.SeepMap.streamSplitState(SeepMap.java:146)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Object> streamSplitState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testStreamSplitState_2()
		throws Exception {
		SeepMap fixture = new SeepMap();
		int chunkSize = 2;

		ArrayList<Object> result = fixture.streamSplitState(chunkSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.infrastructure.api.datastructure.SeepMap.streamSplitState(SeepMap.java:146)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Object> streamSplitState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testStreamSplitState_3()
		throws Exception {
		SeepMap fixture = new SeepMap();
		int chunkSize = 1;

		ArrayList<Object> result = fixture.streamSplitState(chunkSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.infrastructure.api.datastructure.SeepMap.streamSplitState(SeepMap.java:146)
		assertNotNull(result);
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
			junit.textui.TestRunner.run(SeepMapTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new SeepMapTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}