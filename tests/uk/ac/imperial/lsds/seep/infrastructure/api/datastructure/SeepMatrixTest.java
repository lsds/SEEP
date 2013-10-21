package uk.ac.imperial.lsds.seep.infrastructure.api.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import junit.framework.*;

/**
 * The class <code>SeepMatrixTest</code> contains tests for the class <code>{@link SeepMatrix}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:10
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class SeepMatrixTest extends TestCase {
	/**
	 * Run the SeepMatrix() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testSeepMatrix_1()
		throws Exception {

		SeepMatrix result = new SeepMatrix();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getSize());
		assertEquals(0, result.size());
	}

	/**
	 * Run the void appendChunk(ArrayList<Object>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testAppendChunk_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		ArrayList<Object> chunk = null;

		fixture.appendChunk(chunk);

		// add additional test code here
	}

	/**
	 * Run the void appendChunk(ArrayList<Object>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testAppendChunk_2()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		ArrayList chunk = new ArrayList();
		chunk.add(new Object());

		fixture.appendChunk(chunk);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.ClassCastException: java.lang.Object cannot be cast to java.lang.Integer
		//       at uk.ac.imperial.lsds.seep.infrastructure.api.datastructure.SeepMatrix.appendChunk(SeepMatrix.java:228)
	}

	/**
	 * Run the void appendChunk(ArrayList<Object>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testAppendChunk_3()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		ArrayList<Object> chunk = new ArrayList();

		fixture.appendChunk(chunk);

		// add additional test code here
	}

	/**
	 * Run the Object getFromBackup(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetFromBackup_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		Object key = new Object();

		Object result = fixture.getFromBackup(key);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.infrastructure.api.datastructure.SeepMatrix.getFromBackup(SeepMatrix.java:240)
		assertNotNull(result);
	}

	/**
	 * Run the Iterator getIterator() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetIterator_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		Iterator result = fixture.getIterator();

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.hasNext());
	}

	/**
	 * Run the ArrayList<Component> getRowVectorWithTag(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetRowVectorWithTag_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;

		ArrayList<Component> result = fixture.getRowVectorWithTag(rowTag);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the ArrayList<Component> getRowVectorWithTag(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetRowVectorWithTag_2()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;

		ArrayList<Component> result = fixture.getRowVectorWithTag(rowTag);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the ArrayList<Component> getRowVectorWithTag(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetRowVectorWithTag_3()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;

		ArrayList<Component> result = fixture.getRowVectorWithTag(rowTag);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the int getSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetSize_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		int result = fixture.getSize();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getTotalNumberOfChunks(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetTotalNumberOfChunks_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int chunkSize = 1;

		int result = fixture.getTotalNumberOfChunks(chunkSize);

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the void lockStateAccess() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testLockStateAccess_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		fixture.lockStateAccess();

		// add additional test code here
	}

	/**
	 * Run the void lockStateAccess() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testLockStateAccess_2()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		fixture.lockStateAccess();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_2()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_3()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		fixture.ctime = 1L;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_4()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		fixture.ctime = 1L;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_5()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		fixture.ctime = 1L;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_6()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		fixture.ctime = 1L;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_7()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_8()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_9()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_10()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_11()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		fixture.ctime = 1L;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void reconcile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReconcile_12()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		fixture.ctime = 1L;

		fixture.reconcile();

		// add additional test code here
	}

	/**
	 * Run the void releaseStateAccess() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReleaseStateAccess_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		fixture.releaseStateAccess();

		// add additional test code here
	}

	/**
	 * Run the void reset() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testReset_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;

		fixture.reset();

		// add additional test code here
	}

	/**
	 * Run the void setDirtyMode(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testSetDirtyMode_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		boolean newValue = true;

		fixture.setDirtyMode(newValue);

		// add additional test code here
	}

	/**
	 * Run the ArrayList<Object> streamSplitState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testStreamSplitState_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int chunkSize = 1;

		ArrayList<Object> result = fixture.streamSplitState(chunkSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.infrastructure.api.datastructure.SeepMatrix.streamSplitState(SeepMatrix.java:279)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Object> streamSplitState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testStreamSplitState_2()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int chunkSize = 2;

		ArrayList<Object> result = fixture.streamSplitState(chunkSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.infrastructure.api.datastructure.SeepMatrix.streamSplitState(SeepMatrix.java:279)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Object> streamSplitState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testStreamSplitState_3()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int chunkSize = 1;

		ArrayList<Object> result = fixture.streamSplitState(chunkSize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.infrastructure.api.datastructure.SeepMatrix.streamSplitState(SeepMatrix.java:279)
		assertNotNull(result);
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_1()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_2()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_3()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_4()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_5()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_6()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_7()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_8()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_9()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_10()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_11()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_12()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_13()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_14()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_15()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

		// add additional test code here
	}

	/**
	 * Run the void updateMatrixByReplacingValue(int,int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testUpdateMatrixByReplacingValue_16()
		throws Exception {
		SeepMatrix fixture = new SeepMatrix();
		fixture.etime = 1L;
		fixture.realIndexWhileAppendingChunks = 1;
		fixture.a2 = 1L;
		fixture.a1 = 1L;
		fixture.dtime = 1L;
		fixture.cco = 1L;
		fixture.ftime = 1L;
		fixture.aco = 1L;
		fixture.bco = 1L;
		fixture.ctime = 1L;
		fixture.atime = 1L;
		fixture.reptime = 1L;
		fixture.dco = 1L;
		fixture.its = 1;
		fixture.rows = new ArrayList();
		fixture.a3 = 1L;
		fixture.totalco = 1L;
		fixture.cotime = 1L;
		fixture.rowIds = new HashMap();
		fixture.totaltime = 1L;
		fixture.btime = 1L;
		fixture.rowSize = 1;
		int rowTag = 1;
		int col = 1;
		int value = 1;

		fixture.updateMatrixByReplacingValue(rowTag, col, value);

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
			junit.textui.TestRunner.run(SeepMatrixTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new SeepMatrixTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}