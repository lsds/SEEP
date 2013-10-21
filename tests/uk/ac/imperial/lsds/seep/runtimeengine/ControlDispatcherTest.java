package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import java.net.InetAddress;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import java.util.ArrayList;
import uk.ac.imperial.lsds.seep.processingunit.PUContext;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import junit.framework.*;

/**
 * The class <code>ControlDispatcherTest</code> contains tests for the class <code>{@link ControlDispatcher}</code>.
 *
 * @generatedBy CodePro at 18/10/13 18:58
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ControlDispatcherTest extends TestCase {
	/**
	 * Run the ControlDispatcher(PUContext) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testControlDispatcher_1()
		throws Exception {
		PUContext puCtx = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		ControlDispatcher result = new ControlDispatcher(puCtx);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		//       at com.esotericsoftware.kryo.io.Output.<init>(Output.java:34)
		//       at com.esotericsoftware.kryo.io.Output.<init>(Output.java:24)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.ControlDispatcher.<init>(ControlDispatcher.java:171)
		assertNotNull(result);
	}

	/**
	 * Run the ControlDispatcher(PUContext) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testControlDispatcher_2()
		throws Exception {
		PUContext puCtx = new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList());

		ControlDispatcher result = new ControlDispatcher(puCtx);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		//       at com.esotericsoftware.kryo.io.Output.<init>(Output.java:34)
		//       at com.esotericsoftware.kryo.io.Output.<init>(Output.java:24)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.ControlDispatcher.<init>(ControlDispatcher.java:171)
		assertNotNull(result);
	}

	/**
	 * Run the void ackControlMessage(ControlTuple,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAckControlMessage_1()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple genericAck = new ControlTuple();
		OutputStream os = new ByteArrayOutputStream();

		fixture.ackControlMessage(genericAck, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void ackControlMessage(ControlTuple,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAckControlMessage_2()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple genericAck = new ControlTuple();
		OutputStream os = new ByteArrayOutputStream();

		fixture.ackControlMessage(genericAck, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the Object deepCopy(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testDeepCopy_1()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		Object toCopy = new Object();

		Object result = fixture.deepCopy(toCopy);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		assertNotNull(result);
	}

	/**
	 * Run the void initStateMessage(ControlTuple,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testInitStateMessage_1()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple initStateMsg = new ControlTuple();
		OutputStream os = new ByteArrayOutputStream();

		fixture.initStateMessage(initStateMsg, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void initStateMessage(ControlTuple,OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testInitStateMessage_2()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple initStateMsg = new ControlTuple();
		OutputStream os = new ByteArrayOutputStream();

		fixture.initStateMessage(initStateMsg, os);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendAllUpstreams(ControlTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendAllUpstreams_1()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();

		fixture.sendAllUpstreams(ct);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		//       at com.esotericsoftware.kryo.io.Output.<init>(Output.java:34)
		//       at com.esotericsoftware.kryo.io.Output.<init>(Output.java:24)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.ControlDispatcher.<init>(ControlDispatcher.java:171)
	}

	/**
	 * Run the void sendAllUpstreams(ControlTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendAllUpstreams_2()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();

		fixture.sendAllUpstreams(ct);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		//       at com.esotericsoftware.kryo.io.Output.<init>(Output.java:34)
		//       at com.esotericsoftware.kryo.io.Output.<init>(Output.java:24)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.ControlDispatcher.<init>(ControlDispatcher.java:171)
	}

	/**
	 * Run the void sendCloseSession(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendCloseSession_1()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendCloseSession(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendCloseSession(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendCloseSession_2()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendCloseSession(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendCloseSession(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendCloseSession_3()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendCloseSession(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendCloseSession(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendCloseSession_4()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendCloseSession(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendDownstream(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendDownstream_1()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendDownstream(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendDownstream(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendDownstream_2()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendDownstream(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendDownstream(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendDownstream_3()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendDownstream(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendDownstream(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendDownstream_4()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendDownstream(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendOpenSessionWaitACK(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendOpenSessionWaitACK_1()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendOpenSessionWaitACK(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendOpenSessionWaitACK(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendOpenSessionWaitACK_2()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendOpenSessionWaitACK(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendOpenSessionWaitACK(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendOpenSessionWaitACK_3()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendOpenSessionWaitACK(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendOpenSessionWaitACK(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendOpenSessionWaitACK_4()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendOpenSessionWaitACK(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendOpenSessionWaitACK(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendOpenSessionWaitACK_5()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendOpenSessionWaitACK(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendOpenSessionWaitACK(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendOpenSessionWaitACK_6()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendOpenSessionWaitACK(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendOpenSessionWaitACK(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendOpenSessionWaitACK_7()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendOpenSessionWaitACK(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendOpenSessionWaitACK(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendOpenSessionWaitACK_8()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendOpenSessionWaitACK(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_1()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendUpstream(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_2()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendUpstream(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_3()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendUpstream(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream_blind(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_blind_1()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendUpstream_blind(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream_blind(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_blind_2()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendUpstream_blind(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream_blind(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_blind_3()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendUpstream_blind(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream_blind(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_blind_4()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendUpstream_blind(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream_blind(ControlTuple,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_blind_5()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		ControlTuple ct = new ControlTuple();
		int index = 1;

		fixture.sendUpstream_blind(ct, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream_blind_metadata(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_blind_metadata_1()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		int data = 1;
		int index = 1;

		fixture.sendUpstream_blind_metadata(data, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream_blind_metadata(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_blind_metadata_2()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		int data = 1;
		int index = 1;

		fixture.sendUpstream_blind_metadata(data, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream_blind_metadata(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_blind_metadata_3()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		int data = 1;
		int index = 1;

		fixture.sendUpstream_blind_metadata(data, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream_blind_metadata(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_blind_metadata_4()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		int data = 1;
		int index = 1;

		fixture.sendUpstream_blind_metadata(data, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Run the void sendUpstream_blind_metadata(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendUpstream_blind_metadata_5()
		throws Exception {
		ControlDispatcher fixture = new ControlDispatcher(new PUContext(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new ArrayList()));
		int data = 1;
		int index = 1;

		fixture.sendUpstream_blind_metadata(data, index);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
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
	 * @generatedBy CodePro at 18/10/13 18:58
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
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(ControlDispatcherTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ControlDispatcherTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}