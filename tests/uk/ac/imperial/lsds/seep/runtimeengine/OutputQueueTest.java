package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.PipedReader;
import java.net.InetAddress;
import java.net.Socket;
import uk.ac.imperial.lsds.seep.buffer.OutputLogEntry;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.utils.dynamiccodedeployer.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.buffer.Buffer;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import java.util.Map;
import java.util.Scanner;
import junit.framework.*;

/**
 * The class <code>OutputQueueTest</code> contains tests for the class <code>{@link OutputQueue}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:12
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class OutputQueueTest extends TestCase {
	/**
	 * Run the OutputQueue(CoreRE) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testOutputQueue_1()
		throws Exception {
		CoreRE owner = new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {})));

		OutputQueue result = new OutputQueue(owner);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void replay(SynchronousCommunicationChannel) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testReplay_1()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		SynchronousCommunicationChannel oi = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		//oi.setSharedIterator(new Scanner(new PipedReader()));

		fixture.replay(oi);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void replay(SynchronousCommunicationChannel) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testReplay_2()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		SynchronousCommunicationChannel oi = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		//oi.setSharedIterator(new Scanner(new PipedReader()));

		fixture.replay(oi);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void replay(SynchronousCommunicationChannel) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testReplay_3()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		SynchronousCommunicationChannel oi = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());
		//oi.setSharedIterator(new Scanner(new PipedReader()));

		fixture.replay(oi);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void replayTuples(SynchronousCommunicationChannel) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testReplayTuples_1()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		SynchronousCommunicationChannel cci = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.replayTuples(cci);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_1()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_2()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_3()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_4()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_5()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple();
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_6()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple();
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_7()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_8()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_9()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_10()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_11()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_12()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_13()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_14()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple(new HashMap(), new TuplePayload());
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_15()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple();
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void sendToDownstream(DataTuple,EndPoint) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testSendToDownstream_16()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));
		DataTuple tuple = new DataTuple();
		EndPoint dest = new SynchronousCommunicationChannel(1, new Socket(), new Socket(), new Socket(), new Buffer());

		fixture.sendToDownstream(tuple, dest);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel.<init>(SynchronousCommunicationChannel.java:53)
	}

	/**
	 * Run the void start() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testStart_1()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.start();

		// add additional test code here
	}

	/**
	 * Run the void start() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testStart_2()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.start();

		// add additional test code here
	}

	/**
	 * Run the void stop() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:12
	 */
	public void testStop_1()
		throws Exception {
		OutputQueue fixture = new OutputQueue(new CoreRE(new WorkerNodeDescription(InetAddress.getLocalHost(), 1), new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}))));

		fixture.stop();

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
			junit.textui.TestRunner.run(OutputQueueTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new OutputQueueTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}