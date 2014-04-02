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
package uk.ac.imperial.lsds.seep.comm.serialization;

import java.util.ArrayList;
import junit.framework.*;

/**
 * The class <code>BatchDataTupleTest</code> contains tests for the class <code>{@link BatchDataTuple}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:13
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class BatchDataTupleTest extends TestCase {
	/**
	 * Run the BatchDataTuple() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testBatchDataTuple_1()
		throws Exception {

		BatchDataTuple result = new BatchDataTuple();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getBatchSize());
	}

	/**
	 * Run the void addTuple(DataTuple) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testAddTuple_1()
		throws Exception {
		BatchDataTuple fixture = new BatchDataTuple();
		fixture.addTuple(new DataTuple());
		fixture.addTuple(new DataTuple());
		DataTuple data = new DataTuple();

		fixture.addTuple(data);

		// add additional test code here
	}

	/**
	 * Run the void clear() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testClear_1()
		throws Exception {
		BatchDataTuple fixture = new BatchDataTuple();
		fixture.addTuple(new DataTuple());
		fixture.addTuple(new DataTuple());

		fixture.clear();

		// add additional test code here
	}

	/**
	 * Run the BatchDataTuple getBatch() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetBatch_1()
		throws Exception {
		BatchDataTuple fixture = new BatchDataTuple();
		fixture.addTuple(new DataTuple());
		fixture.addTuple(new DataTuple());

		BatchDataTuple result = fixture.getBatch();

		// add additional test code here
		assertNotNull(result);
		assertEquals(2, result.getBatchSize());
	}

	/**
	 * Run the int getBatchSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetBatchSize_1()
		throws Exception {
		BatchDataTuple fixture = new BatchDataTuple();
		fixture.addTuple(new DataTuple());
		fixture.addTuple(new DataTuple());

		int result = fixture.getBatchSize();

		// add additional test code here
		assertEquals(2, result);
	}

	/**
	 * Run the DataTuple getTuple(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetTuple_1()
		throws Exception {
		BatchDataTuple fixture = new BatchDataTuple();
		fixture.addTuple(new DataTuple());
		fixture.addTuple(new DataTuple());
		int index = 1;

		DataTuple result = fixture.getTuple(index);

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getPayload());
	}

	/**
	 * Run the ArrayList<DataTuple> getTuples() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetTuples_1()
		throws Exception {
		BatchDataTuple fixture = new BatchDataTuple();
		fixture.addTuple(new DataTuple());
		fixture.addTuple(new DataTuple());

		ArrayList<DataTuple> result = fixture.getTuples();

		// add additional test code here
		assertNotNull(result);
		assertEquals(2, result.size());
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
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
	 * @generatedBy CodePro at 18/10/13 19:13
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
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(BatchDataTupleTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new BatchDataTupleTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
