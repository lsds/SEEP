/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.api.largestateimpls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.state.EmptyStateException;
import uk.ac.imperial.lsds.seep.state.MalformedStateChunk;
import uk.ac.imperial.lsds.seep.state.NullChunkWhileMerging;
import junit.framework.*;

/**
 * The class <code>SeepMapTest</code> contains tests for the class <code>{@link SeepMap}</code>.
 *
 * @generatedBy CodePro at 01/11/13 18:10
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class SeepMapTest extends TestCase {
	/**
	 * Run the SeepMap() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testAppendChunk_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		ArrayList chunk = new ArrayList();
		chunk.add(new Object());

		boolean thrown = false;
		try{
			fixture.appendChunk(chunk);
			fail("MalformedStateChunk exception should have been thrown");
		}
		catch(MalformedStateChunk e){
			thrown = true;
		}
		
		assertTrue(thrown);
	}

	/**
	 * Run the void appendChunk(ArrayList<Object>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testAppendChunk_2()
		throws Exception {
		SeepMap fixture = new SeepMap();
		ArrayList<Object> chunk = new ArrayList();

		boolean thrown = false;
		try{
			fixture.appendChunk(chunk);
			fail("MalformedStateChunk exception should have been thrown");
		}
		catch(MalformedStateChunk e){
			thrown = true;
		}
		
		assertTrue(thrown);
	}

	/**
	 * Run the void appendChunk(ArrayList<Object>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testAppendChunk_3()
		throws Exception {
		try {
			SeepMap fixture = new SeepMap();
			ArrayList<Object> chunk = null;

			fixture.appendChunk(chunk);

			// add additional test code here
			fail("The exception uk.ac.imperial.lsds.seep.state.NullChunkWhileMerging should have been thrown.");
		} catch (uk.ac.imperial.lsds.seep.state.NullChunkWhileMerging exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the void clear() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testClear_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.clear();
		System.out.println(fixture.size());

		for(int i = 0; i<10; i++){
			fixture.put(i, new Object());
		}
		System.out.println(fixture.size());
		assertEquals(fixture.size(), 10);
		fixture.clear();
		System.out.println(fixture.size());
		assertEquals(fixture.size(), 0);
		
	}

	/**
	 * Run the void clear() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testClear_2()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.clear();

		// add additional test code here
	}

	/**
	 * Run the boolean containsKey(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testContainsKey_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		boolean result = fixture.containsKey(key);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean containsKey(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testContainsKey_2()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		boolean result = fixture.containsKey(key);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean containsKey(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testContainsKey_3()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		boolean result = fixture.containsKey(key);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean containsKey(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testContainsKey_4()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		boolean result = fixture.containsKey(key);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean containsKey(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testContainsKey_5()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		boolean result = fixture.containsKey(key);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean containsValue(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testContainsValue_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object value = new Object();

		boolean result = fixture.containsValue(value);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean containsValue(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testContainsValue_2()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object value = new Object();

		boolean result = fixture.containsValue(value);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean containsValue(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testContainsValue_3()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object value = new Object();

		boolean result = fixture.containsValue(value);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean containsValue(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testContainsValue_4()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object value = new Object();

		boolean result = fixture.containsValue(value);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean containsValue(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testContainsValue_5()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object value = new Object();

		boolean result = fixture.containsValue(value);

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the Set<java.util.Map.Entry<Object, Object>> entrySet() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testEntrySet_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		Set<java.util.Map.Entry<Object, Object>> result = fixture.entrySet();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the Set<java.util.Map.Entry<Object, Object>> entrySet() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testEntrySet_2()
		throws Exception {
		SeepMap fixture = new SeepMap();

		Set<java.util.Map.Entry<Object, Object>> result = fixture.entrySet();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the Object get(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * Run the Object get(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testGet_4()
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
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testGet_5()
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
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * Run the Iterator<Object> getIterator() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testGetIterator_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		Iterator<Object> result = fixture.getIterator();

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.hasNext());
	}

	/**
	 * Run the int getSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testGetTotalNumberOfChunks_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		int chunkSize = 1;
		
		boolean thrown = false;
		try{
			int result = fixture.getTotalNumberOfChunks(chunkSize);
		}
		catch(EmptyStateException e){
			thrown = true;
		}

		// add additional test code here
		assertTrue(thrown);
	}

	/**
	 * Run the boolean isEmpty() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testIsEmpty_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		boolean result = fixture.isEmpty();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isEmpty() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testIsEmpty_2()
		throws Exception {
		SeepMap fixture = new SeepMap();

		boolean result = fixture.isEmpty();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isEmpty() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testIsEmpty_3()
		throws Exception {
		SeepMap fixture = new SeepMap();

		boolean result = fixture.isEmpty();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isEmpty() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testIsEmpty_4()
		throws Exception {
		SeepMap fixture = new SeepMap();

		boolean result = fixture.isEmpty();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isEmpty() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testIsEmpty_5()
		throws Exception {
		SeepMap fixture = new SeepMap();

		boolean result = fixture.isEmpty();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the Set<Object> keySet() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testKeySet_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		Set<Object> result = fixture.keySet();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the Set<Object> keySet() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testKeySet_2()
		throws Exception {
		SeepMap fixture = new SeepMap();

		Set<Object> result = fixture.keySet();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the void lock() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testLock_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.lock();

		// add additional test code here
	}

	/**
	 * Run the void lock() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testLock_2()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.lock();

		// add additional test code here
	}

	/**
	 * Run the Object put(Object,Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testReconcile_4()
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
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testReconcile_5()
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
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testReconcile_6()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void release() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testRelease_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.release();

		// add additional test code here
	}

	/**
	 * Run the Object remove(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testRemove_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		Object result = fixture.remove(key);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the Object remove(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testRemove_2()
		throws Exception {
		SeepMap fixture = new SeepMap();
		Object key = new Object();

		Object result = fixture.remove(key);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the void reset() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testReset_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		fixture.reset();

		// add additional test code here
	}

	/**
	 * Run the void setSnapshotMode(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testSetSnapshotMode_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		boolean newValue = true;

		fixture.setSnapshotMode(newValue);

		// add additional test code here
	}

	/**
	 * Run the int size() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testSize_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		int result = fixture.size();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the ArrayList<Object> streamSplitState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testStreamSplitState_1()
		throws Exception {
		SeepMap fixture = new SeepMap();
		int chunkSize = 1;

		ArrayList<Object> result = fixture.streamSplitState(chunkSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.api.largestateimpls.SeepMap.streamSplitState(SeepMap.java:273)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Object> streamSplitState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testStreamSplitState_2()
		throws Exception {
		SeepMap fixture = new SeepMap();
		int chunkSize = 2;

		ArrayList<Object> result = fixture.streamSplitState(chunkSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.api.largestateimpls.SeepMap.streamSplitState(SeepMap.java:273)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Object> streamSplitState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testStreamSplitState_3()
		throws Exception {
		SeepMap fixture = new SeepMap();
		int chunkSize = 1;

		ArrayList<Object> result = fixture.streamSplitState(chunkSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.api.largestateimpls.SeepMap.streamSplitState(SeepMap.java:273)
		assertNotNull(result);
	}

	/**
	 * Run the Collection<Object> values() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testValues_1()
		throws Exception {
		SeepMap fixture = new SeepMap();

		Collection<Object> result = fixture.values();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the Collection<Object> values() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public void testValues_2()
		throws Exception {
		SeepMap fixture = new SeepMap();

		Collection<Object> result = fixture.values();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
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
	 * @generatedBy CodePro at 01/11/13 18:10
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
