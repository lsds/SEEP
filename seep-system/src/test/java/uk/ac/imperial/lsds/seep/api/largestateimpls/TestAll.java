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
package uk.ac.imperial.lsds.seep.api.largestateimpls;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all
 * of the tests within its package as well as within any subpackages of its
 * package.
 *
 * @generatedBy CodePro at 01/11/13 18:10
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class TestAll {

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Create a test suite that can run all of the test cases in this package
	 * and all subpackages.
	 *
	 * @return the test suite that was created
	 *
	 * @generatedBy CodePro at 01/11/13 18:10
	 */
	public static Test suite() {
		TestSuite suite;
	
		suite = new TestSuite("Tests in package uk.ac.imperial.lsds.seep.api.largestateimpls");
		suite.addTestSuite(SeepMapTest.class);
		return suite;
	}
}
