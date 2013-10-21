package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import junit.framework.*;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;

/**
 * The class <code>StateChunkTest</code> contains tests for the class <code>{@link StateChunk}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:04
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class StateChunkTest extends TestCase {
	/**
	 * Run the StateChunk() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testStateChunk_1()
		throws Exception {

		StateChunk result = new StateChunk();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getKeeperOpId());
		assertEquals(null, result.getMemoryChunk());
		assertEquals(0, result.getSplittingKey());
		assertEquals(0, result.getOwnerOpId());
		assertEquals(0, result.getTotalChunks());
		assertEquals(0, result.getSequenceNumber());
	}

	/**
	 * Run the StateChunk(int,int,int,int,MemoryChunk,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testStateChunk_2()
		throws Exception {
		int opId = 1;
		int keeperOpId = 1;
		int seqNumber = 1;
		int totalChunks = 1;
		MemoryChunk mc = new MemoryChunk();
		int splittingKey = 1;

		StateChunk result = new StateChunk(opId, keeperOpId, seqNumber, totalChunks, mc, splittingKey);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.getKeeperOpId());
		assertEquals(1, result.getSplittingKey());
		assertEquals(1, result.getOwnerOpId());
		assertEquals(1, result.getTotalChunks());
		assertEquals(1, result.getSequenceNumber());
	}

	/**
	 * Run the int getKeeperOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetKeeperOpId_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);

		int result = fixture.getKeeperOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the MemoryChunk getMemoryChunk() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetMemoryChunk_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);

		MemoryChunk result = fixture.getMemoryChunk();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the int getOwnerOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetOwnerOpId_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);

		int result = fixture.getOwnerOpId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getSequenceNumber() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetSequenceNumber_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);

		int result = fixture.getSequenceNumber();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getSplittingKey() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetSplittingKey_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);

		int result = fixture.getSplittingKey();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the int getTotalChunks() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetTotalChunks_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);

		int result = fixture.getTotalChunks();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the void setKeeperOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSetKeeperOpId_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);
		int keeperOpId = 1;

		fixture.setKeeperOpId(keeperOpId);

		// add additional test code here
	}

	/**
	 * Run the void setOwnerOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSetOwnerOpId_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);
		int opId = 1;

		fixture.setOwnerOpId(opId);

		// add additional test code here
	}

	/**
	 * Run the void setSequenceNumber(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSetSequenceNumber_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);
		int sequenceNumber = 1;

		fixture.setSequenceNumber(sequenceNumber);

		// add additional test code here
	}

	/**
	 * Run the void setSplittingKey(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSetSplittingKey_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);
		int key = 1;

		fixture.setSplittingKey(key);

		// add additional test code here
	}

	/**
	 * Run the void setTotalChunks(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSetTotalChunks_1()
		throws Exception {
		StateChunk fixture = new StateChunk(1, 1, 1, 1, new MemoryChunk(), 1);
		int totalChunks = 1;

		fixture.setTotalChunks(totalChunks);

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
	 * @generatedBy CodePro at 18/10/13 19:04
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
	 * @generatedBy CodePro at 18/10/13 19:04
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
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(StateChunkTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new StateChunkTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}