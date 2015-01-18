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

import com.esotericsoftware.kryo.Kryo;
import junit.framework.*;

/**
 * The class <code>KryoSerializerTest</code> contains tests for the class <code>{@link KryoSerializer}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:07
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class KryoSerializerTest extends TestCase {
	/**
	 * Run the KryoSerializer() constructor test.
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testKryoSerializer_1()
		throws Exception {
		KryoSerializer result = new KryoSerializer();
		assertNotNull(result);
		// add additional test code here
	}

	/**
	 * Run the Object deserialize(byte[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testDeserialize_1()
		throws Exception {
		KryoSerializer fixture = new KryoSerializer();
		fixture.k = new Kryo();
		byte[] data = new byte[] {};

		Object result = fixture.deserialize(data);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the byte[] serialize(T) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testSerialize_1()
		throws Exception {
		KryoSerializer fixture = new KryoSerializer();
		fixture.k = new Kryo();

		byte[] result = fixture.serialize(null);

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
			junit.textui.TestRunner.run(KryoSerializerTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new KryoSerializerTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
