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
package uk.ac.imperial.lsds.seep.comm.routing;

import java.util.ArrayList;
import junit.framework.*;

/**
 * The class <code>StatefulRoutingImplTest</code> contains tests for the class <code>{@link StatefulRoutingImpl}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:03
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class StatefulRoutingImplTest extends TestCase {
	/**
	 * Run the StatefulRoutingImpl(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testStatefulRoutingImpl_1()
		throws Exception {
		int realIndex = 1;

		StatefulRoutingImpl result = new StatefulRoutingImpl(realIndex);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the StatefulRoutingImpl(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testStatefulRoutingImpl_2()
		throws Exception {
		int realIndex = 1;

		StatefulRoutingImpl result = new StatefulRoutingImpl(realIndex);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the StatefulRoutingImpl(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testStatefulRoutingImpl_3()
		throws Exception {
		int realIndex = 1;

		StatefulRoutingImpl result = new StatefulRoutingImpl(realIndex);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the StatefulRoutingImpl(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testStatefulRoutingImpl_4()
		throws Exception {
		int realIndex = 1;

		StatefulRoutingImpl result = new StatefulRoutingImpl(realIndex);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the StatefulRoutingImpl(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testStatefulRoutingImpl_5()
		throws Exception {
		int realIndex = 1;

		StatefulRoutingImpl result = new StatefulRoutingImpl(realIndex);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the StatefulRoutingImpl(int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testStatefulRoutingImpl_6()
		throws Exception {
		int realIndex = 1;

		StatefulRoutingImpl result = new StatefulRoutingImpl(realIndex);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the StatefulRoutingImpl(ArrayList<Integer>,ArrayList<Integer>) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testStatefulRoutingImpl_7()
		throws Exception {
		ArrayList<Integer> keyToDownstreamRealIndex = new ArrayList();
		ArrayList<Integer> downstreamNodeKeys = new ArrayList();

		StatefulRoutingImpl result = new StatefulRoutingImpl(keyToDownstreamRealIndex, downstreamNodeKeys);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Integer> getDownstreamNodeKeys() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testGetDownstreamNodeKeys_1()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());

		ArrayList<Integer> result = fixture.getDownstreamNodeKeys();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the ArrayList<Integer> getKeyToDownstreamRealIndex() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testGetKeyToDownstreamRealIndex_1()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());

		ArrayList<Integer> result = fixture.getKeyToDownstreamRealIndex();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the int[] newReplica(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testNewReplica_1()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		int oldOpIndex = 1;
		int newOpIndex = 1;

		int[] result = fixture.newReplica(oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.ArrayIndexOutOfBoundsException: -1
		//       at java.util.ArrayList.elementData(ArrayList.java:371)
		//       at java.util.ArrayList.get(ArrayList.java:384)
		//       at uk.ac.imperial.lsds.seep.comm.routing.StatefulRoutingImpl.newReplica(StatefulRoutingImpl.java:75)
		assertNotNull(result);
	}

	/**
	 * Run the int[] newReplica(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testNewReplica_2()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		int oldOpIndex = 1;
		int newOpIndex = 1;

		int[] result = fixture.newReplica(oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.ArrayIndexOutOfBoundsException: -1
		//       at java.util.ArrayList.elementData(ArrayList.java:371)
		//       at java.util.ArrayList.get(ArrayList.java:384)
		//       at uk.ac.imperial.lsds.seep.comm.routing.StatefulRoutingImpl.newReplica(StatefulRoutingImpl.java:75)
		assertNotNull(result);
	}

	/**
	 * Run the int[] newReplica(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testNewReplica_3()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		int oldOpIndex = 1;
		int newOpIndex = 1;

		int[] result = fixture.newReplica(oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.ArrayIndexOutOfBoundsException: -1
		//       at java.util.ArrayList.elementData(ArrayList.java:371)
		//       at java.util.ArrayList.get(ArrayList.java:384)
		//       at uk.ac.imperial.lsds.seep.comm.routing.StatefulRoutingImpl.newReplica(StatefulRoutingImpl.java:75)
		assertNotNull(result);
	}

	/**
	 * Run the int[] newStaticReplica(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testNewStaticReplica_1()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		int oldOpIndex = 1;
		int newOpIndex = 1;

		int[] result = fixture.newStaticReplica(oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.ArrayIndexOutOfBoundsException: -1
		//       at java.util.ArrayList.elementData(ArrayList.java:371)
		//       at java.util.ArrayList.get(ArrayList.java:384)
		//       at uk.ac.imperial.lsds.seep.comm.routing.StatefulRoutingImpl.newStaticReplica(StatefulRoutingImpl.java:119)
		assertNotNull(result);
	}

	/**
	 * Run the int[] newStaticReplica(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testNewStaticReplica_2()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		int oldOpIndex = 1;
		int newOpIndex = 1;

		int[] result = fixture.newStaticReplica(oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.ArrayIndexOutOfBoundsException: -1
		//       at java.util.ArrayList.elementData(ArrayList.java:371)
		//       at java.util.ArrayList.get(ArrayList.java:384)
		//       at uk.ac.imperial.lsds.seep.comm.routing.StatefulRoutingImpl.newStaticReplica(StatefulRoutingImpl.java:119)
		assertNotNull(result);
	}

	/**
	 * Run the int[] newStaticReplica(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testNewStaticReplica_3()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		int oldOpIndex = 1;
		int newOpIndex = 1;

		int[] result = fixture.newStaticReplica(oldOpIndex, newOpIndex);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.ArrayIndexOutOfBoundsException: -1
		//       at java.util.ArrayList.elementData(ArrayList.java:371)
		//       at java.util.ArrayList.get(ArrayList.java:384)
		//       at uk.ac.imperial.lsds.seep.comm.routing.StatefulRoutingImpl.newStaticReplica(StatefulRoutingImpl.java:119)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Integer> route(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testRoute_1()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		int value = 1;

		ArrayList<Integer> result = fixture.route(value);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the ArrayList<Integer> route(ArrayList<Integer>,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testRoute_2()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		ArrayList targets = new ArrayList();
		targets.add(new Integer(-1));
		int value = 1;

		ArrayList<Integer> result = fixture.route(targets, value);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the ArrayList<Integer> route(ArrayList<Integer>,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testRoute_3()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		ArrayList targets = new ArrayList();
		targets.add(new Integer(-1));
		int value = 1;

		ArrayList<Integer> result = fixture.route(targets, value);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the ArrayList<Integer> route(ArrayList<Integer>,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testRoute_4()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		ArrayList<Integer> targets = new ArrayList();
		int value = 1;

		ArrayList<Integer> result = fixture.route(targets, value);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the ArrayList<Integer> route(ArrayList<Integer>,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testRoute_5()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		ArrayList<Integer> targets = new ArrayList();
		int value = 1;

		ArrayList<Integer> result = fixture.route(targets, value);

		// add additional test code here
		assertEquals(null, result);
	}

	/**
	 * Run the ArrayList<Integer> routeToAll() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testRouteToAll_1()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());

		ArrayList<Integer> result = fixture.routeToAll();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the ArrayList<Integer> routeToAll(ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testRouteToAll_2()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		ArrayList<Integer> targets = new ArrayList();

		ArrayList<Integer> result = fixture.routeToAll(targets);

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the void setDownstreamNodeKeys(ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public void testSetDownstreamNodeKeys_1()
		throws Exception {
		StatefulRoutingImpl fixture = new StatefulRoutingImpl(new ArrayList(), new ArrayList());
		ArrayList<Integer> downstreamNodeKeys = new ArrayList();

		fixture.setDownstreamNodeKeys(downstreamNodeKeys);

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
	 * @generatedBy CodePro at 18/10/13 19:03
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
	 * @generatedBy CodePro at 18/10/13 19:03
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
	 * @generatedBy CodePro at 18/10/13 19:03
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(StatefulRoutingImplTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new StatefulRoutingImplTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
