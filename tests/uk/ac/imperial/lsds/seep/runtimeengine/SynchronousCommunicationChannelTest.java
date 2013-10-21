package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.PipedReader;
import java.net.Socket;
import uk.ac.imperial.lsds.seep.buffer.OutputLogEntry;
import java.util.Iterator;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import com.esotericsoftware.kryo.io.Output;
import uk.ac.imperial.lsds.seep.buffer.Buffer;
import junit.framework.*;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;

/**
 * The class <code>SynchronousCommunicationChannelTest</code> contains tests for the class <code>{@link SynchronousCommunicationChannel}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:04
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class SynchronousCommunicationChannelTest extends TestCase {
	/**
	 * Run the SynchronousCommunicationChannel(int,Socket,Socket,Socket,Buffer) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSynchronousCommunicationChannel_1()
		throws Exception {
		int opId = 1;
		Socket downstreamSocketD = new Socket();
		Socket downstreamSocketC = new Socket();
		Socket blindSocket = new Socket();
		Buffer buffer = new Buffer();

		SynchronousCommunicationChannel result = new SynchronousCommunicationChannel(opId, downstreamSocketD, downstreamSocketC, blindSocket, buffer);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the SynchronousCommunicationChannel(int,Socket,Socket,Socket,Buffer) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSynchronousCommunicationChannel_2()
		throws Exception {
		int opId = 1;
		Socket downstreamSocketD = null;
		Socket downstreamSocketC = new Socket();
		Socket blindSocket = new Socket();
		Buffer buffer = new Buffer();

		SynchronousCommunicationChannel result = new SynchronousCommunicationChannel(opId, downstreamSocketD, downstreamSocketC, blindSocket, buffer);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the SynchronousCommunicationChannel(int,Socket,Socket,Socket,Buffer) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSynchronousCommunicationChannel_3()
		throws Exception {
		int opId = 1;
		Socket downstreamSocketD = new Socket();
		Socket downstreamSocketC = new Socket();
		Socket blindSocket = new Socket();
		Buffer buffer = new Buffer();

		SynchronousCommunicationChannel result = new SynchronousCommunicationChannel(opId, downstreamSocketD, downstreamSocketC, blindSocket, buffer);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the void addDataToBatch(TuplePayload) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testAddDataToBatch_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));
		TuplePayload payload = new TuplePayload();
		payload.timestamp = 1L;

		fixture.addDataToBatch(payload);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void cleanBatch() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testCleanBatch_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		fixture.cleanBatch();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void cleanBatch() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testCleanBatch_2()
		throws Exception {
		try {
			SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
			fixture.setTick(1L);
			fixture.setReconf_ts(new TimestampTracker());
//			fixture.setSharedIterator(new Scanner(new PipedReader()));

			fixture.cleanBatch();

			// add additional test code here
			fail("The exception java.lang.NumberFormatException should have been thrown.");
		} catch (java.lang.NumberFormatException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the void cleanBatch2() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testCleanBatch2_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		fixture.cleanBatch2();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the BatchTuplePayload getBatch() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetBatch_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		BatchTuplePayload result = fixture.getBatch();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the Socket getBlindSocket() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetBlindSocket_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		Socket result = fixture.getBlindSocket();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the Buffer getBuffer() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetBuffer_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		Buffer result = fixture.getBuffer();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the int getChannelBatchSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetChannelBatchSize_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		int result = fixture.getChannelBatchSize();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertEquals(0, result);
	}

	/**
	 * Run the Socket getDownstreamControlSocket() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetDownstreamControlSocket_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		Socket result = fixture.getDownstreamControlSocket();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the Socket getDownstreamDataSocket() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetDownstreamDataSocket_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		Socket result = fixture.getDownstreamDataSocket();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the long getLast_ts() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetLast_ts_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		long result = fixture.getLast_ts();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertEquals(0L, result);
	}

	/**
	 * Run the int getOperatorId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetOperatorId_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		int result = fixture.getOperatorId();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertEquals(0, result);
	}

	/**
	 * Run the Output getOutput() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetOutput_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		Output result = fixture.getOutput();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the TimestampTracker getReconf_ts() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetReconf_ts_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		TimestampTracker result = fixture.getReconf_ts();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the AtomicBoolean getReplay() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetReplay_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		AtomicBoolean result = fixture.getReplay();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the Iterator<OutputLogEntry> getSharedIterator() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetSharedIterator_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		Iterator<OutputLogEntry> result = fixture.getSharedIterator();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the AtomicBoolean getStop() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testGetStop_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		AtomicBoolean result = fixture.getStop();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the Socket reOpenBlindSocket() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testReOpenBlindSocket_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		Socket result = fixture.reOpenBlindSocket();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
		assertNotNull(result);
	}

	/**
	 * Run the void resetChannelBatchSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testResetChannelBatchSize_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));

		fixture.resetChannelBatchSize();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void setReconf_ts(TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSetReconf_ts_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));
		TimestampTracker ts = new TimestampTracker();

		fixture.setReconf_ts(ts);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void setSharedIterator(Iterator<OutputLogEntry>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSetSharedIterator_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));
//		Iterator<OutputLogEntry> i = new Scanner(new PipedReader());

//		fixture.setSharedIterator(i);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void setTick(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:04
	 */
	public void testSetTick_1()
		throws Exception {
		SynchronousCommunicationChannel fixture = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		fixture.setTick(1L);
		fixture.setReconf_ts(new TimestampTracker());
//		fixture.setSharedIterator(new Scanner(new PipedReader()));
		long tick = 1L;

		fixture.setTick(tick);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
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
			junit.textui.TestRunner.run(SynchronousCommunicationChannelTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new SynchronousCommunicationChannelTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}