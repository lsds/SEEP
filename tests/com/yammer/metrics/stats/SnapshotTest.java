package com.yammer.metrics.stats;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import junit.framework.*;

/**
 * The class <code>SnapshotTest</code> contains tests for the class <code>{@link Snapshot}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:10
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class SnapshotTest extends TestCase {
	/**
	 * Run the Snapshot(Collection<Long>) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testSnapshot_1()
		throws Exception {
		Collection<Long> values = new LinkedList();

		Snapshot result = new Snapshot(values);

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
		assertEquals(0.0, result.get75thPercentile(), 1.0);
		assertEquals(0.0, result.getMedian(), 1.0);
		assertEquals(0.0, result.get999thPercentile(), 1.0);
		assertEquals(0.0, result.get98thPercentile(), 1.0);
		assertEquals(0.0, result.get99thPercentile(), 1.0);
		assertEquals(0.0, result.get95thPercentile(), 1.0);
	}

	/**
	 * Run the Snapshot(Collection<Long>) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testSnapshot_2()
		throws Exception {
		Collection<Long> values = new LinkedList();

		Snapshot result = new Snapshot(values);

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
		assertEquals(0.0, result.get75thPercentile(), 1.0);
		assertEquals(0.0, result.getMedian(), 1.0);
		assertEquals(0.0, result.get999thPercentile(), 1.0);
		assertEquals(0.0, result.get98thPercentile(), 1.0);
		assertEquals(0.0, result.get99thPercentile(), 1.0);
		assertEquals(0.0, result.get95thPercentile(), 1.0);
	}

	/**
	 * Run the Snapshot(double[]) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testSnapshot_3()
		throws Exception {
		double[] values = new double[] {};

		Snapshot result = new Snapshot(values);

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
		assertEquals(0.0, result.get75thPercentile(), 1.0);
		assertEquals(0.0, result.getMedian(), 1.0);
		assertEquals(0.0, result.get999thPercentile(), 1.0);
		assertEquals(0.0, result.get98thPercentile(), 1.0);
		assertEquals(0.0, result.get99thPercentile(), 1.0);
		assertEquals(0.0, result.get95thPercentile(), 1.0);
	}

	/**
	 * Run the void dump(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testDump_1()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());
		File output = new File("");

		fixture.dump(output);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.FileOutputStream.<init>(FileOutputStream.java:203)
		//       at java.io.FileOutputStream.<init>(FileOutputStream.java:165)
		//       at java.io.PrintWriter.<init>(PrintWriter.java:263)
		//       at com.yammer.metrics.stats.Snapshot.dump(Snapshot.java:157)
	}

	/**
	 * Run the void dump(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testDump_2()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());
		File output = new File("");

		fixture.dump(output);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.FileOutputStream.<init>(FileOutputStream.java:203)
		//       at java.io.FileOutputStream.<init>(FileOutputStream.java:165)
		//       at java.io.PrintWriter.<init>(PrintWriter.java:263)
		//       at com.yammer.metrics.stats.Snapshot.dump(Snapshot.java:157)
	}

	/**
	 * Run the void dump(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testDump_3()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());
		File output = new File("");

		fixture.dump(output);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.SecurityException: Cannot write to files while generating test cases
		//       at com.instantiations.assist.eclipse.junit.CodeProJUnitSecurityManager.checkWrite(CodeProJUnitSecurityManager.java:76)
		//       at java.io.FileOutputStream.<init>(FileOutputStream.java:203)
		//       at java.io.FileOutputStream.<init>(FileOutputStream.java:165)
		//       at java.io.PrintWriter.<init>(PrintWriter.java:263)
		//       at com.yammer.metrics.stats.Snapshot.dump(Snapshot.java:157)
	}

	/**
	 * Run the double get75thPercentile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGet75thPercentile_1()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());

		double result = fixture.get75thPercentile();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double get95thPercentile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGet95thPercentile_1()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());

		double result = fixture.get95thPercentile();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double get98thPercentile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGet98thPercentile_1()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());

		double result = fixture.get98thPercentile();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double get999thPercentile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGet999thPercentile_1()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());

		double result = fixture.get999thPercentile();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double get99thPercentile() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGet99thPercentile_1()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());

		double result = fixture.get99thPercentile();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getMedian() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetMedian_1()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());

		double result = fixture.getMedian();

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getValue(double) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetValue_1()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());
		double quantile = 1.0;

		double result = fixture.getValue(quantile);

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getValue(double) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetValue_2()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());
		double quantile = 1.0;

		double result = fixture.getValue(quantile);

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getValue(double) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetValue_3()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());
		double quantile = 1.0;

		double result = fixture.getValue(quantile);

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getValue(double) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetValue_4()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());
		double quantile = 1.0;

		double result = fixture.getValue(quantile);

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getValue(double) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetValue_5()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());
		double quantile = 1.0;

		double result = fixture.getValue(quantile);

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getValue(double) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetValue_6()
		throws Exception {
		try {
			Snapshot fixture = new Snapshot(new LinkedList());
			double quantile = -4.9E-324;

			double result = fixture.getValue(quantile);

			// add additional test code here
			fail("The exception java.lang.IllegalArgumentException should have been thrown.");
		} catch (java.lang.IllegalArgumentException exception) {
			// The test succeeded by throwing the expected exception
		}
	}

	/**
	 * Run the double[] getValues() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testGetValues_1()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());

		double[] result = fixture.getValues();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.length);
	}

	/**
	 * Run the int size() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:10
	 */
	public void testSize_1()
		throws Exception {
		Snapshot fixture = new Snapshot(new LinkedList());

		int result = fixture.size();

		// add additional test code here
		assertEquals(0, result);
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
			junit.textui.TestRunner.run(SnapshotTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new SnapshotTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}