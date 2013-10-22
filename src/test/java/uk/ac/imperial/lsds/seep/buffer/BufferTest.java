package uk.ac.imperial.lsds.seep.buffer;

import java.util.Iterator;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import junit.framework.*;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;

/**
 * The class <code>BufferTest</code> contains tests for the class <code>{@link Buffer}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:12
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class BufferTest extends TestCase {
	/**
	 * Run the Buffer() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testBuffer_1()
		throws Exception {

		Buffer result = new Buffer();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the BackupOperatorState getBackupState() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testGetBackupState_1()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());

		BackupOperatorState result = fixture.getBackupState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getState());
		assertEquals(null, result.getStateClass());
		assertEquals(null, result.getOutputBuffers());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the TimestampTracker getInputVTsForOutputTs(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testGetInputVTsForOutputTs_1()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());
		long output_ts = 1L;

		TimestampTracker result = fixture.getInputVTsForOutputTs(output_ts);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the TimestampTracker getInputVTsForOutputTs(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testGetInputVTsForOutputTs_2()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());
		long output_ts = 1L;

		TimestampTracker result = fixture.getInputVTsForOutputTs(output_ts);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the TimestampTracker getInputVTsForOutputTs(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testGetInputVTsForOutputTs_3()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());
		long output_ts = 1L;

		TimestampTracker result = fixture.getInputVTsForOutputTs(output_ts);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the Iterator<OutputLogEntry> iterator() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testIterator_1()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());

		Iterator<OutputLogEntry> result = fixture.iterator();

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.hasNext());
	}

	/**
	 * Run the void replaceBackupOperatorState(BackupOperatorState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testReplaceBackupOperatorState_1()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());
		BackupOperatorState bs = new BackupOperatorState();

		fixture.replaceBackupOperatorState(bs);

		// add additional test code here
	}

	/**
	 * Run the void replaceRawData(RawData) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testReplaceRawData_1()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());
		RawData rw = new RawData(1, new byte[] {});

		fixture.replaceRawData(rw);

		// add additional test code here
	}

	/**
	 * Run the void save(BatchTuplePayload,long,TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSave_1()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());
		BatchTuplePayload batch = new BatchTuplePayload();
		long outputTs = 1L;
		TimestampTracker inputTs = new TimestampTracker();

		fixture.save(batch, outputTs, inputTs);

		// add additional test code here
	}

	/**
	 * Run the int size() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSize_1()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());

		int result = fixture.size();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the TimestampTracker trim(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testTrim_1()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());
		long ts = 1L;

		TimestampTracker result = fixture.trim(ts);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the TimestampTracker trim(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testTrim_2()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());
		long ts = 1L;

		TimestampTracker result = fixture.trim(ts);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the TimestampTracker trim(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testTrim_3()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());
		long ts = 1L;

		TimestampTracker result = fixture.trim(ts);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the TimestampTracker trim(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testTrim_4()
		throws Exception {
		Buffer fixture = new Buffer();
		fixture.replaceRawData(new RawData(1, new byte[] {}));
		fixture.replaceBackupOperatorState(new BackupOperatorState());
		long ts = 1L;

		TimestampTracker result = fixture.trim(ts);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
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
	 * @generatedBy CodePro at 18/10/13 19:12
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
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(BufferTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new BufferTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}