package uk.ac.imperial.lsds.seep.comm.serialization;

import java.util.HashMap;
import java.util.Map;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import junit.framework.*;

/**
 * The class <code>DataTupleTest</code> contains tests for the class <code>{@link DataTuple}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:05
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class DataTupleTest extends TestCase {
	/**
	 * Run the DataTuple() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testDataTuple_1()
		throws Exception {

		DataTuple result = new DataTuple();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getPayload());
	}

	/**
	 * Run the DataTuple(Map<String,Integer>,TuplePayload) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testDataTuple_2()
		throws Exception {
		Map<String, Integer> idxMapper = new HashMap();
		TuplePayload payload = new TuplePayload();

		DataTuple result = new DataTuple(idxMapper, payload);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the boolean getBoolean(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetBoolean_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		boolean result = fixture.getBoolean(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getBoolean(DataTuple.java:153)
		assertTrue(result);
	}

	/**
	 * Run the Byte getByte(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetByte_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		Byte result = fixture.getByte(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getByte(DataTuple.java:86)
		assertNotNull(result);
	}

	/**
	 * Run the byte[] getByteArray(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetByteArray_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		byte[] result = fixture.getByteArray(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getByteArray(DataTuple.java:91)
		assertNotNull(result);
	}

	/**
	 * Run the Character getChar(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetChar_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		Character result = fixture.getChar(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getChar(DataTuple.java:96)
		assertNotNull(result);
	}

	/**
	 * Run the Double getDouble(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetDouble_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		Double result = fixture.getDouble(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getDouble(DataTuple.java:101)
		assertNotNull(result);
	}

	/**
	 * Run the double[] getDoubleArray(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetDoubleArray_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		double[] result = fixture.getDoubleArray(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getDoubleArray(DataTuple.java:106)
		assertNotNull(result);
	}

	/**
	 * Run the Float getFloat(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetFloat_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		Float result = fixture.getFloat(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getFloat(DataTuple.java:111)
		assertNotNull(result);
	}

	/**
	 * Run the Integer getInt(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetInt_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		Integer result = fixture.getInt(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getInt(DataTuple.java:116)
		assertNotNull(result);
	}

	/**
	 * Run the int[] getIntArray(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetIntArray_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		int[] result = fixture.getIntArray(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getIntArray(DataTuple.java:121)
		assertNotNull(result);
	}

	/**
	 * Run the Long getLong(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetLong_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		Long result = fixture.getLong(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getLong(DataTuple.java:126)
		assertNotNull(result);
	}

	/**
	 * Run the HashMap<String, Integer> getMap() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetMap_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());

		HashMap<String, Integer> result = fixture.getMap();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the DataTuple getNoopDataTuple() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetNoopDataTuple_1()
		throws Exception {

		DataTuple result = DataTuple.getNoopDataTuple();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getPayload());
	}

	/**
	 * Run the TuplePayload getPayload() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetPayload_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());

		TuplePayload result = fixture.getPayload();

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the Short getShort(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetShort_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		Short result = fixture.getShort(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getShort(DataTuple.java:131)
		assertNotNull(result);
	}

	/**
	 * Run the String getString(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetString_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		String result = fixture.getString(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getString(DataTuple.java:136)
		assertNotNull(result);
	}

	/**
	 * Run the String[] getStringArray(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetStringArray_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		String[] result = fixture.getStringArray(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getStringArray(DataTuple.java:141)
		assertNotNull(result);
	}

	/**
	 * Run the Object getValue(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testGetValue_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		String attribute = "";

		Object result = fixture.getValue(attribute);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.getValue(DataTuple.java:147)
		assertNotNull(result);
	}

	/**
	 * Run the DataTuple newTuple(Object[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testNewTuple_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());

		DataTuple result = fixture.newTuple();

		// add additional test code here
		assertNotNull(result);
		assertEquals("VAL ", result.toString());
		assertEquals(0, result.size());
	}

	/**
	 * Run the void set(TuplePayload) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSet_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());
		TuplePayload tuplePayload = new TuplePayload();

		fixture.set(tuplePayload);

		// add additional test code here
	}

	/**
	 * Run the DataTuple setValues(Object[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSetValues_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());

		DataTuple result = fixture.setValues();

		// add additional test code here
		assertNotNull(result);
		assertEquals("VAL ", result.toString());
		assertEquals(0, result.size());
	}

	/**
	 * Run the void setValuesMutable(Object[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSetValuesMutable_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());

		fixture.setValuesMutable();

		// add additional test code here
	}

	/**
	 * Run the int size() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testSize_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());

		int result = fixture.size();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.size(DataTuple.java:40)
		assertEquals(0, result);
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public void testToString_1()
		throws Exception {
		DataTuple fixture = new DataTuple(new HashMap(), new TuplePayload());

		String result = fixture.toString();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload.toString(TuplePayload.java:29)
		//       at uk.ac.imperial.lsds.seep.comm.serialization.DataTuple.toString(DataTuple.java:158)
		assertNotNull(result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:05
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
	 * @generatedBy CodePro at 18/10/13 19:05
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
	 * @generatedBy CodePro at 18/10/13 19:05
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(DataTupleTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new DataTupleTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}