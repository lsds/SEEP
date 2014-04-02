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
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.net.InetAddress;
import junit.framework.*;

/**
 * The class <code>DisposableCommunicationChannelTest</code> contains tests for the class <code>{@link DisposableCommunicationChannel}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:07
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class DisposableCommunicationChannelTest extends TestCase {
	/**
	 * Run the DisposableCommunicationChannel(int,InetAddress) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testDisposableCommunicationChannel_1()
		throws Exception {
		int opId = 1;
		InetAddress ip = InetAddress.getLocalHost();

		DisposableCommunicationChannel result = new DisposableCommunicationChannel(opId, ip);

		// add additional test code here
		assertNotNull(result);
		assertEquals("OP-ID: 1 -> hedera.doc.ic.ac.uk/146.169.5.130", result.toString());
		assertEquals(1, result.getOperatorId());
	}

	/**
	 * Run the InetAddress getIp() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetIp_1()
		throws Exception {
		DisposableCommunicationChannel fixture = new DisposableCommunicationChannel(1, InetAddress.getLocalHost());

		InetAddress result = fixture.getIp();

		// add additional test code here
		assertNotNull(result);
		assertEquals("hedera.doc.ic.ac.uk/146.169.5.130", result.toString());
		assertEquals(false, result.isLinkLocalAddress());
		assertEquals(false, result.isMCGlobal());
		assertEquals(false, result.isLoopbackAddress());
		assertEquals(false, result.isMCLinkLocal());
		assertEquals(false, result.isMCOrgLocal());
		assertEquals("hedera.doc.ic.ac.uk", result.getCanonicalHostName());
		assertEquals(false, result.isMCNodeLocal());
		assertEquals(false, result.isMCSiteLocal());
		assertEquals(false, result.isMulticastAddress());
		assertEquals(false, result.isSiteLocalAddress());
		assertEquals(false, result.isAnyLocalAddress());
		assertEquals("146.169.5.130", result.getHostAddress());
		assertEquals("hedera.doc.ic.ac.uk", result.getHostName());
	}

	/**
	 * Run the int getOperatorId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testGetOperatorId_1()
		throws Exception {
		DisposableCommunicationChannel fixture = new DisposableCommunicationChannel(1, InetAddress.getLocalHost());

		int result = fixture.getOperatorId();

		// add additional test code here
		assertEquals(1, result);
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:07
	 */
	public void testToString_1()
		throws Exception {
		DisposableCommunicationChannel fixture = new DisposableCommunicationChannel(1, InetAddress.getLocalHost());

		String result = fixture.toString();

		// add additional test code here
		assertEquals("OP-ID: 1 -> hedera.doc.ic.ac.uk/146.169.5.130", result);
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
			junit.textui.TestRunner.run(DisposableCommunicationChannelTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new DisposableCommunicationChannelTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
