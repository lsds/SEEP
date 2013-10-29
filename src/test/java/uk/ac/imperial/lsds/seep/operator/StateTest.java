package uk.ac.imperial.lsds.seep.operator;

import java.net.URL;
import uk.ac.imperial.lsds.seep.elastic.MockState;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.RuntimeClassLoader;

import java.net.URLClassLoader;
import junit.framework.*;
import uk.ac.imperial.lsds.seep.runtimeengine.TimestampTracker;
import uk.ac.imperial.lsds.seep.state.State;

/**
 * The class <code>StateTest</code> contains tests for the class <code>{@link State}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:08
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class StateTest extends TestCase {
	/**
	 * Run the State clone() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testClone_1()
		throws Exception {
		State fixture = new MockState();

		State result = fixture.clone();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getCheckpointInterval());
		assertEquals(null, result.getStateTag());
		assertEquals(null, result.getData_ts());
		assertEquals(0, result.getOwnerId());
		assertEquals(null, result.getStateImpl());
	}

	/**
	 * Run the State clone() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testClone_2()
		throws Exception {
		State fixture = new MockState();

		State result = fixture.clone();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getCheckpointInterval());
		assertEquals(null, result.getStateTag());
		assertEquals(null, result.getData_ts());
		assertEquals(0, result.getOwnerId());
		assertEquals(null, result.getStateImpl());
	}

	/**
	 * Run the State deepCopy(State,RuntimeClassLoader) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testDeepCopy_1()
		throws Exception {
		State original = new MockState();
		RuntimeClassLoader rcl = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));

		State result = State.deepCopy(original, rcl);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		assertNotNull(result);
	}

	/**
	 * Run the State deepCopy(State,RuntimeClassLoader) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testDeepCopy_2()
		throws Exception {
		State original = new MockState();
		RuntimeClassLoader rcl = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));

		State result = State.deepCopy(original, rcl);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		assertNotNull(result);
	}

	/**
	 * Run the State deepCopy(State,RuntimeClassLoader) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testDeepCopy_3()
		throws Exception {
		State original = new MockState();
		RuntimeClassLoader rcl = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));

		State result = State.deepCopy(original, rcl);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		assertNotNull(result);
	}

	/**
	 * Run the State deepCopy(State,RuntimeClassLoader) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testDeepCopy_4()
		throws Exception {
		State original = new MockState();
		RuntimeClassLoader rcl = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));

		State result = State.deepCopy(original, rcl);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		assertNotNull(result);
	}

	/**
	 * Run the State deepCopy(State,RuntimeClassLoader) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testDeepCopy_5()
		throws Exception {
		State original = new MockState();
		RuntimeClassLoader rcl = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));

		State result = State.deepCopy(original, rcl);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		assertNotNull(result);
	}

	/**
	 * Run the State deepCopy(State,RuntimeClassLoader) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testDeepCopy_6()
		throws Exception {
		State original = new MockState();
		RuntimeClassLoader rcl = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));

		State result = State.deepCopy(original, rcl);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		assertNotNull(result);
	}

	/**
	 * Run the State deepCopy(State,RuntimeClassLoader) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testDeepCopy_7()
		throws Exception {
		State original = new MockState();
		RuntimeClassLoader rcl = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));

		State result = State.deepCopy(original, rcl);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.OutOfMemoryError: Java heap space
		assertNotNull(result);
	}

	/**
	 * Run the int getCheckpointInterval() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetCheckpointInterval_1()
		throws Exception {
		State fixture = new MockState();

		int result = fixture.getCheckpointInterval();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the TimestampTracker getData_ts() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetData_ts_1()
		throws Exception {
		State fixture = new MockState();

		TimestampTracker result = fixture.getData_ts();

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the int getOwnerId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetOwnerId_1()
		throws Exception {
		State fixture = new MockState();

		int result = fixture.getOwnerId();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the State getStateImpl() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetStateImpl_1()
		throws Exception {
		State fixture = new MockState();

		State result = fixture.getStateImpl();

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the String getStateTag() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetStateTag_1()
		throws Exception {
		State fixture = new MockState();

		String result = fixture.getStateTag();

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the void setCheckpointInterval(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetCheckpointInterval_1()
		throws Exception {
		State fixture = new MockState();
		int checkpointInterval = 1;

		fixture.setCheckpointInterval(checkpointInterval);

		// add additional test code here
	}

	/**
	 * Run the void setData_ts(TimestampTracker) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetData_ts_1()
		throws Exception {
		State fixture = new MockState();
		TimestampTracker data_ts = new TimestampTracker();

		fixture.setData_ts(data_ts);

		// add additional test code here
	}

	/**
	 * Run the void setOwnerId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetOwnerId_1()
		throws Exception {
		State fixture = new MockState();
		int ownerId = 1;

		fixture.setOwnerId(ownerId);

		// add additional test code here
	}

	/**
	 * Run the void setStateTag(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testSetStateTag_1()
		throws Exception {
		State fixture = new MockState();
		String stateTag = "";

		fixture.setStateTag(stateTag);

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
			junit.textui.TestRunner.run(StateTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new StateTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}