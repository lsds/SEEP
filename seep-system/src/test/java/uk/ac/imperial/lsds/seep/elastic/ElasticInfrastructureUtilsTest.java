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
package uk.ac.imperial.lsds.seep.elastic;

import java.net.URL;
import java.net.URLClassLoader;

import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.api.ScaleOutIntentBean;
import uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import java.util.ArrayList;
import uk.ac.imperial.lsds.seep.operator.Operator;
import junit.framework.*;

/**
 * The class <code>ElasticInfrastructureUtilsTest</code> contains tests for the class <code>{@link ElasticInfrastructureUtils}</code>.
 *
 * @generatedBy CodePro at 18/10/13 18:58
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ElasticInfrastructureUtilsTest extends TestCase {
	/**
	 * Run the ElasticInfrastructureUtils(Infrastructure) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testElasticInfrastructureUtils_1()
		throws Exception {
		Infrastructure inf = new Infrastructure(1);

		ElasticInfrastructureUtils result = new ElasticInfrastructureUtils(inf);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the void addDownstreamConnections(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddDownstreamConnections_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		Operator newOp = null;

		fixture.addDownstreamConnections(newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void addDownstreamConnections(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddDownstreamConnections_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		Operator newOp = null;

		fixture.addDownstreamConnections(newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the Operator addOperator(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddOperator_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		int newOpId = 1;

		Operator result = fixture.addOperator(opId, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator addOperator(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddOperator_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		int newOpId = 1;

		Operator result = fixture.addOperator(opId, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator addOperator(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddOperator_3()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		int newOpId = 1;

		Operator result = fixture.addOperator(opId, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator addOperator(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddOperator_4()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		int newOpId = 1;

		Operator result = fixture.addOperator(opId, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator addOperator(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddOperator_5()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		int newOpId = 1;

		Operator result = fixture.addOperator(opId, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator addOperator(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddOperator_6()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		int newOpId = 1;

		Operator result = fixture.addOperator(opId, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator addOperator(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddOperator_7()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		int newOpId = 1;

		Operator result = fixture.addOperator(opId, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator addOperator(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddOperator_8()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		int newOpId = 1;

		Operator result = fixture.addOperator(opId, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator addOperator(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddOperator_9()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		int newOpId = 1;

		Operator result = fixture.addOperator(opId, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the Operator addOperator(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddOperator_10()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		int newOpId = 1;

		Operator result = fixture.addOperator(opId, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the void addUpstreamConnections(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddUpstreamConnections_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		Operator newOp = null;

		fixture.addUpstreamConnections(newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void addUpstreamConnections(Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAddUpstreamConnections_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		Operator newOp = null;

		fixture.addUpstreamConnections(newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void alert(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAlert_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;

		fixture.alert(opIdToParallelize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void alertCPU(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAlertCPU_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;

		fixture.alertCPU(opIdToParallelize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void alertCPU(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testAlertCPU_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;

		fixture.alertCPU(opIdToParallelize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureOperatorContext(int,Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testConfigureOperatorContext_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		Operator newOp = null;

		fixture.configureOperatorContext(opId, newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureOperatorContext(int,Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testConfigureOperatorContext_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		Operator newOp = null;

		fixture.configureOperatorContext(opId, newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureOperatorContext(int,Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testConfigureOperatorContext_3()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		Operator newOp = null;

		fixture.configureOperatorContext(opId, newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureOperatorContext(int,Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testConfigureOperatorContext_4()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		Operator newOp = null;

		fixture.configureOperatorContext(opId, newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureOperatorContext(int,Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testConfigureOperatorContext_5()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		Operator newOp = null;

		fixture.configureOperatorContext(opId, newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureOperatorContext(int,Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testConfigureOperatorContext_6()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		Operator newOp = null;

		fixture.configureOperatorContext(opId, newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureOperatorContext(int,Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testConfigureOperatorContext_7()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		Operator newOp = null;

		fixture.configureOperatorContext(opId, newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureOperatorContext(int,Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testConfigureOperatorContext_8()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		Operator newOp = null;

		fixture.configureOperatorContext(opId, newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureOperatorContext(int,Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testConfigureOperatorContext_9()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		Operator newOp = null;

		fixture.configureOperatorContext(opId, newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void configureOperatorContext(int,Operator) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testConfigureOperatorContext_10()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;
		Operator newOp = null;

		fixture.configureOperatorContext(opId, newOp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void executeParallelRecovery(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testExecuteParallelRecovery_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		String oldIp_txt = "";

		fixture.executeParallelRecovery(oldIp_txt);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void executeParallelRecovery(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testExecuteParallelRecovery_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		String oldIp_txt = "";

		fixture.executeParallelRecovery(oldIp_txt);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void executeParallelRecovery(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testExecuteParallelRecovery_3()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		String oldIp_txt = "";

		fixture.executeParallelRecovery(oldIp_txt);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void executeParallelRecovery(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testExecuteParallelRecovery_4()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		String oldIp_txt = "";

		fixture.executeParallelRecovery(oldIp_txt);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void executeStaticScaleOutFromIntent(ArrayList<ScaleOutIntentBean>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testExecuteStaticScaleOutFromIntent_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		ArrayList<ScaleOutIntentBean> soib = new ArrayList();

		fixture.executeStaticScaleOutFromIntent(soib);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void executeStaticScaleOutFromIntent(ArrayList<ScaleOutIntentBean>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testExecuteStaticScaleOutFromIntent_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		ArrayList<ScaleOutIntentBean> soib = new ArrayList();

		fixture.executeStaticScaleOutFromIntent(soib);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void executeStaticScaleOutFromIntent(ArrayList<ScaleOutIntentBean>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testExecuteStaticScaleOutFromIntent_3()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		ArrayList<ScaleOutIntentBean> soib = new ArrayList();

		fixture.executeStaticScaleOutFromIntent(soib);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void executeStaticScaleOutFromIntent(ArrayList<ScaleOutIntentBean>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testExecuteStaticScaleOutFromIntent_4()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		ArrayList<ScaleOutIntentBean> soib = new ArrayList();

		fixture.executeStaticScaleOutFromIntent(soib);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void executeStaticScaleOutFromIntent(ArrayList<ScaleOutIntentBean>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testExecuteStaticScaleOutFromIntent_5()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		ArrayList<ScaleOutIntentBean> soib = new ArrayList();

		fixture.executeStaticScaleOutFromIntent(soib);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the String getOperatorClassName(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testGetOperatorClassName_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;

		String result = fixture.getOperatorClassName(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the String getOperatorClassName(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testGetOperatorClassName_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;

		String result = fixture.getOperatorClassName(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the String getOperatorClassName(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testGetOperatorClassName_3()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opId = 1;

		String result = fixture.getOperatorClassName(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the void largeScaleOutOperator(int,int,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testLargeScaleOutOperator_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;
		Node newNode = new Node(1);

		fixture.largeScaleOutOperator(opIdToParallelize, newOpId, newNode);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void largeScaleOutOperator(int,int,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testLargeScaleOutOperator_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;
		Node newNode = new Node(1);

		fixture.largeScaleOutOperator(opIdToParallelize, newOpId, newNode);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void lightScaleOutOperator(int,int,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testLightScaleOutOperator_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;
		Node newNode = new Node(1);

		fixture.lightScaleOutOperator(opIdToParallelize, newOpId, newNode);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void lightScaleOutOperator(int,int,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testLightScaleOutOperator_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;
		Node newNode = new Node(1);

		fixture.lightScaleOutOperator(opIdToParallelize, newOpId, newNode);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the boolean promptForUserValidation(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testPromptForUserValidation_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;

		boolean result = fixture.promptForUserValidation(opIdToParallelize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertTrue(result);
	}

	/**
	 * Run the boolean promptForUserValidation(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testPromptForUserValidation_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;

		boolean result = fixture.promptForUserValidation(opIdToParallelize);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertTrue(result);
	}

	/**
	 * Run the void scaleOutOperator(int,int,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testScaleOutOperator_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;
		Node newNode = new Node(1);

		fixture.scaleOutOperator(opIdToParallelize, newOpId, newNode);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void scaleOutOperator(int,int,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testScaleOutOperator_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;
		Node newNode = new Node(1);

		fixture.scaleOutOperator(opIdToParallelize, newOpId, newNode);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void scaleOutOperator(int,int,Node) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testScaleOutOperator_3()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;
		Node newNode = new Node(1);

		fixture.scaleOutOperator(opIdToParallelize, newOpId, newNode);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void sendDistributedScaleOutMessageToStarTopology(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendDistributedScaleOutMessageToStarTopology_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;

		fixture.sendDistributedScaleOutMessageToStarTopology(opIdToParallelize, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void sendDistributedScaleOutMessageToStarTopology(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendDistributedScaleOutMessageToStarTopology_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;

		fixture.sendDistributedScaleOutMessageToStarTopology(opIdToParallelize, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void sendDistributedScaleOutMessageToStarTopology(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendDistributedScaleOutMessageToStarTopology_3()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;

		fixture.sendDistributedScaleOutMessageToStarTopology(opIdToParallelize, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void sendDistributedScaleOutMessageToStarTopology(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendDistributedScaleOutMessageToStarTopology_4()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;

		fixture.sendDistributedScaleOutMessageToStarTopology(opIdToParallelize, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void sendDistributedScaleOutMessageToStarTopology(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendDistributedScaleOutMessageToStarTopology_5()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;

		fixture.sendDistributedScaleOutMessageToStarTopology(opIdToParallelize, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void sendDistributedScaleOutMessageToStarTopology(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSendDistributedScaleOutMessageToStarTopology_6()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		int opIdToParallelize = 1;
		int newOpId = 1;

		fixture.sendDistributedScaleOutMessageToStarTopology(opIdToParallelize, newOpId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the void setClassLoader(URLClassLoader) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testSetClassLoader_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		URLClassLoader ucl = new URLClassLoader(new URL[] {});

		fixture.setClassLoader(ucl);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
	}

	/**
	 * Run the ArrayList<ScaleOutIntentBean> staticInstantiateNewReplicaOperator(ArrayList<ScaleOutIntentBean>,QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testStaticInstantiateNewReplicaOperator_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		ArrayList<ScaleOutIntentBean> soib = new ArrayList();
		QueryPlan qp = new QueryPlan();

		ArrayList<ScaleOutIntentBean> result = fixture.staticInstantiateNewReplicaOperator(soib, qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<ScaleOutIntentBean> staticInstantiateNewReplicaOperator(ArrayList<ScaleOutIntentBean>,QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testStaticInstantiateNewReplicaOperator_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		ArrayList<ScaleOutIntentBean> soib = new ArrayList();
		QueryPlan qp = new QueryPlan();

		ArrayList<ScaleOutIntentBean> result = fixture.staticInstantiateNewReplicaOperator(soib, qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<ScaleOutIntentBean> staticInstantiateNewReplicaOperator(ArrayList<ScaleOutIntentBean>,QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testStaticInstantiateNewReplicaOperator_3()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		ArrayList<ScaleOutIntentBean> soib = new ArrayList();
		QueryPlan qp = new QueryPlan();

		ArrayList<ScaleOutIntentBean> result = fixture.staticInstantiateNewReplicaOperator(soib, qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<ScaleOutIntentBean> staticInstantiationNewReplicaOperators(QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testStaticInstantiationNewReplicaOperators_1()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		QueryPlan qp = new QueryPlan();

		ArrayList<ScaleOutIntentBean> result = fixture.staticInstantiationNewReplicaOperators(qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<ScaleOutIntentBean> staticInstantiationNewReplicaOperators(QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testStaticInstantiationNewReplicaOperators_2()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		QueryPlan qp = new QueryPlan();

		ArrayList<ScaleOutIntentBean> result = fixture.staticInstantiationNewReplicaOperators(qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<ScaleOutIntentBean> staticInstantiationNewReplicaOperators(QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testStaticInstantiationNewReplicaOperators_3()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		QueryPlan qp = new QueryPlan();

		ArrayList<ScaleOutIntentBean> result = fixture.staticInstantiationNewReplicaOperators(qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<ScaleOutIntentBean> staticInstantiationNewReplicaOperators(QueryPlan) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public void testStaticInstantiationNewReplicaOperators_4()
		throws Exception {
		ElasticInfrastructureUtils fixture = new ElasticInfrastructureUtils(new Infrastructure(1));
		fixture.setClassLoader(new URLClassLoader(new URL[] {}));
		QueryPlan qp = new QueryPlan();

		ArrayList<ScaleOutIntentBean> result = fixture.staticInstantiationNewReplicaOperators(qp);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NumberFormatException: null
		//       at java.lang.Integer.parseInt(Integer.java:454)
		//       at java.lang.Integer.parseInt(Integer.java:527)
		//       at uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure.<init>(Infrastructure.java:69)
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
	 * @generatedBy CodePro at 18/10/13 18:58
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
	 * @generatedBy CodePro at 18/10/13 18:58
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
	 * @generatedBy CodePro at 18/10/13 18:58
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(ElasticInfrastructureUtilsTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ElasticInfrastructureUtilsTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}
