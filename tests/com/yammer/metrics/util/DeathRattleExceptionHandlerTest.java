package com.yammer.metrics.util;

import com.yammer.metrics.core.Counter;
import junit.framework.*;

/**
 * The class <code>DeathRattleExceptionHandlerTest</code> contains tests for the class <code>{@link DeathRattleExceptionHandler}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:07
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class DeathRattleExceptionHandlerTest extends TestCase {
	/**
	 * Run the DeathRattleExceptionHandler(Counter) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testDeathRattleExceptionHandler_1()
		throws Exception {
		Counter counter = null;

		DeathRattleExceptionHandler result = new DeathRattleExceptionHandler(counter);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void uncaughtException(Thread,Throwable) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testUncaughtException_1()
		throws Exception {
		DeathRattleExceptionHandler fixture = new DeathRattleExceptionHandler((Counter) null);
		Thread t = new Thread();
		Throwable e = new Throwable();

		fixture.uncaughtException(t, e);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.util.DeathRattleExceptionHandler.uncaughtException(DeathRattleExceptionHandler.java:50)
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
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
	 * @generatedBy CodePro at 18/10/13 19:07
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
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(DeathRattleExceptionHandlerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new DeathRattleExceptionHandlerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}