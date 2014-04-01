package uk.ac.imperial.lsds.seep.operator;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import junit.framework.*;

/**
 * The class <code>OperatorContextTest</code> contains tests for the class <code>{@link OperatorContext}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:09
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class OperatorContextTest extends TestCase {
	/**
	 * Run the OperatorContext() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testOperatorContext_1()
		throws Exception {

		OperatorContext result = new OperatorContext();

		// add additional test code here
		assertNotNull(result);
		assertEquals("@null", result.toString());
		assertEquals(false, result.isSink());
		assertEquals(0, result.getDownstreamSize());
		assertEquals(false, result.isDownstreamStateful());
		assertEquals(null, result.minimumUpstream());
		assertEquals(null, result.getOperatorStaticInformation());
		assertEquals(0, result.getUpstreamSize());
		assertEquals(false, result.doesRequireLogicalRouting());
		assertEquals(null, result.getKeyAttribute());
		assertEquals(null, result.getDeclaredWorkingAttributes());
		assertEquals(false, result.isSource());
	}

	/**
	 * Run the void addDownstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testAddDownstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opID = 1;

		fixture.addDownstream(opID);

		// add additional test code here
	}

	/**
	 * Run the void addOriginalDownstream(Integer) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testAddOriginalDownstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		Integer opId = new Integer(1);

		fixture.addOriginalDownstream(opId);

		// add additional test code here
	}

	/**
	 * Run the void addUpstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testAddUpstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opID = 1;

		fixture.addUpstream(opID);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(opId, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(opId, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(opId, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_4()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(opId, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_5()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(opId, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_6()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(opId, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_7()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(opId, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_8()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(opId, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(int,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_9()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(opId, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_10()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(oldIp, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_11()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(oldIp, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_12()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(oldIp, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_13()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(oldIp, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_14()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(oldIp, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_15()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(oldIp, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_16()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(oldIp, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_17()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(oldIp, newIp);

		// add additional test code here
	}

	/**
	 * Run the void changeLocation(InetAddress,InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testChangeLocation_18()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress oldIp = InetAddress.getLocalHost();
		InetAddress newIp = InetAddress.getLocalHost();

		fixture.changeLocation(oldIp, newIp);

		// add additional test code here
	}

	/**
	 * Run the boolean doesRequireLogicalRouting() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testDoesRequireLogicalRouting_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		boolean result = fixture.doesRequireLogicalRouting();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean doesRequireLogicalRouting() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testDoesRequireLogicalRouting_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		boolean result = fixture.doesRequireLogicalRouting();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the OperatorContext.PlacedOperator findDownstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testFindDownstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opID = 1;

		OperatorContext.PlacedOperator result = fixture.findDownstream(opID);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.opID());
		assertEquals(0, result.index());
	}

	/**
	 * Run the OperatorContext.PlacedOperator findDownstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testFindDownstream_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opID = 1;

		OperatorContext.PlacedOperator result = fixture.findDownstream(opID);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.opID());
		assertEquals(0, result.index());
	}

	/**
	 * Run the OperatorContext.PlacedOperator findDownstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testFindDownstream_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opID = 1;

		OperatorContext.PlacedOperator result = fixture.findDownstream(opID);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.opID());
		assertEquals(0, result.index());
	}

	/**
	 * Run the OperatorContext.PlacedOperator findUpstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testFindUpstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		OperatorContext.PlacedOperator result = fixture.findUpstream(opId);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.opID());
		assertEquals(0, result.index());
	}

	/**
	 * Run the OperatorContext.PlacedOperator findUpstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testFindUpstream_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		OperatorContext.PlacedOperator result = fixture.findUpstream(opId);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.opID());
		assertEquals(0, result.index());
	}

	/**
	 * Run the OperatorContext.PlacedOperator findUpstream(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testFindUpstream_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		OperatorContext.PlacedOperator result = fixture.findUpstream(opId);

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.opID());
		assertEquals(0, result.index());
	}

	/**
	 * Run the List<String> getDeclaredWorkingAttributes() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetDeclaredWorkingAttributes_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		List<String> result = fixture.getDeclaredWorkingAttributes();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the int getDownOpIdFromIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetDownOpIdFromIndex_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int index = 1;

		int result = fixture.getDownOpIdFromIndex(index);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getDownOpIdFromIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetDownOpIdFromIndex_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int index = 1;

		int result = fixture.getDownOpIdFromIndex(index);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getDownOpIdFromIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetDownOpIdFromIndex_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int index = 1;

		int result = fixture.getDownOpIdFromIndex(index);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getDownOpIndexFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetDownOpIndexFromOpId_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		int result = fixture.getDownOpIndexFromOpId(opId);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getDownOpIndexFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetDownOpIndexFromOpId_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		int result = fixture.getDownOpIndexFromOpId(opId);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getDownOpIndexFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetDownOpIndexFromOpId_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		int result = fixture.getDownOpIndexFromOpId(opId);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the OperatorStaticInformation getDownstreamLocation(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetDownstreamLocation_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opID = 1;

		OperatorStaticInformation result = fixture.getDownstreamLocation(opID);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
		//       at java.util.ArrayList.rangeCheck(ArrayList.java:604)
		//       at java.util.ArrayList.get(ArrayList.java:382)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext.getDownstreamLocation(OperatorContext.java:201)
		assertNotNull(result);
	}

	/**
	 * Run the int getDownstreamSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetDownstreamSize_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		int result = fixture.getDownstreamSize();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the Map<Integer, QuerySpecificationI.InputDataIngestionMode> getInputDataIngestionModePerUpstream() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetInputDataIngestionModePerUpstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		Map<Integer, InputDataIngestionMode> result = fixture.getInputDataIngestionModePerUpstream();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the String getKeyAttribute() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetKeyAttribute_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		String result = fixture.getKeyAttribute();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the ArrayList<Integer> getListOfDownstreamIndexes() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetListOfDownstreamIndexes_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		ArrayList<Integer> result = fixture.getListOfDownstreamIndexes();

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.contains(new Integer(0)));
	}

	/**
	 * Run the ArrayList<Integer> getListOfDownstreamIndexes() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetListOfDownstreamIndexes_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		ArrayList<Integer> result = fixture.getListOfDownstreamIndexes();

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.contains(new Integer(0)));
	}

	/**
	 * Run the int getOpIdFromUpstreamIp(InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetOpIdFromUpstreamIp_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress ip = InetAddress.getLocalHost();

		int result = fixture.getOpIdFromUpstreamIp(ip);

		// add additional test code here
		assertEquals(-1000, result);
	}

	/**
	 * Run the int getOpIdFromUpstreamIp(InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetOpIdFromUpstreamIp_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress ip = InetAddress.getLocalHost();

		int result = fixture.getOpIdFromUpstreamIp(ip);

		// add additional test code here
		assertEquals(-1000, result);
	}

	/**
	 * Run the int getOpIdFromUpstreamIp(InetAddress) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetOpIdFromUpstreamIp_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		InetAddress ip = InetAddress.getLocalHost();

		int result = fixture.getOpIdFromUpstreamIp(ip);

		// add additional test code here
		assertEquals(-1000, result);
	}

	/**
	 * Run the OperatorStaticInformation getOperatorStaticInformation() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetOperatorStaticInformation_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		OperatorStaticInformation result = fixture.getOperatorStaticInformation();

		// add additional test code here
		assertNotNull(result);
		assertEquals("node: Node [ip=null, port=0]inC: 1inD: 1", result.toString());
		assertEquals(1, result.getInC());
		assertEquals(true, result.isStatefull());
		assertEquals(1, result.getInD());
		assertEquals(1, result.getOriginalOpId());
		assertEquals(1, result.getOpId());
	}

	/**
	 * Run the ArrayList<Integer> getOriginalDownstream() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetOriginalDownstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		ArrayList<Integer> result = fixture.getOriginalDownstream();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the int getOriginalUpstreamFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetOriginalUpstreamFromOpId_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		int result = fixture.getOriginalUpstreamFromOpId(opId);

		// add additional test code here
		assertEquals(-1000, result);
	}

	/**
	 * Run the int getOriginalUpstreamFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetOriginalUpstreamFromOpId_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		int result = fixture.getOriginalUpstreamFromOpId(opId);

		// add additional test code here
		assertEquals(-1000, result);
	}

	/**
	 * Run the int getOriginalUpstreamFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetOriginalUpstreamFromOpId_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		int result = fixture.getOriginalUpstreamFromOpId(opId);

		// add additional test code here
		assertEquals(-1000, result);
	}

	/**
	 * Run the HashMap<Integer, ArrayList<Integer>> getRouteInfo() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetRouteInfo_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		HashMap<Integer, ArrayList<Integer>> result = fixture.getRouteInfo();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the int getUpOpIdFromIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpOpIdFromIndex_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int index = 1;

		int result = fixture.getUpOpIdFromIndex(index);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getUpOpIdFromIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpOpIdFromIndex_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int index = 1;

		int result = fixture.getUpOpIdFromIndex(index);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getUpOpIdFromIndex(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpOpIdFromIndex_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int index = 1;

		int result = fixture.getUpOpIdFromIndex(index);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getUpOpIndexFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpOpIndexFromOpId_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		int result = fixture.getUpOpIndexFromOpId(opId);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getUpOpIndexFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpOpIndexFromOpId_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		int result = fixture.getUpOpIndexFromOpId(opId);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getUpOpIndexFromOpId(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpOpIndexFromOpId_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		int result = fixture.getUpOpIndexFromOpId(opId);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the OperatorStaticInformation getUpstreamLocation(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpstreamLocation_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opID = 1;

		OperatorStaticInformation result = fixture.getUpstreamLocation(opID);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
		//       at java.util.ArrayList.rangeCheck(ArrayList.java:604)
		//       at java.util.ArrayList.get(ArrayList.java:382)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext.getUpstreamLocation(OperatorContext.java:205)
		assertNotNull(result);
	}

	/**
	 * Run the int getUpstreamNumberOfType(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpstreamNumberOfType_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int originalOpId = 1;

		int result = fixture.getUpstreamNumberOfType(originalOpId);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getUpstreamNumberOfType(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpstreamNumberOfType_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int originalOpId = 1;

		int result = fixture.getUpstreamNumberOfType(originalOpId);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getUpstreamNumberOfType(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpstreamNumberOfType_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int originalOpId = 1;

		int result = fixture.getUpstreamNumberOfType(originalOpId);

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the int getUpstreamSize() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testGetUpstreamSize_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		int result = fixture.getUpstreamSize();

		// add additional test code here
		assertEquals(0, result);
	}

	/**
	 * Run the boolean isDownstreamOperatorStateful(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsDownstreamOperatorStateful_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		boolean result = fixture.isDownstreamOperatorStateful(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
		//       at java.util.ArrayList.rangeCheck(ArrayList.java:604)
		//       at java.util.ArrayList.get(ArrayList.java:382)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext$PlacedOperator.location(OperatorContext.java:386)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext.isDownstreamOperatorStateful(OperatorContext.java:162)
		assertTrue(result);
	}

	/**
	 * Run the boolean isDownstreamOperatorStateful(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsDownstreamOperatorStateful_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		boolean result = fixture.isDownstreamOperatorStateful(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
		//       at java.util.ArrayList.rangeCheck(ArrayList.java:604)
		//       at java.util.ArrayList.get(ArrayList.java:382)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext$PlacedOperator.location(OperatorContext.java:386)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext.isDownstreamOperatorStateful(OperatorContext.java:162)
		assertTrue(result);
	}

	/**
	 * Run the boolean isDownstreamOperatorStateful(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsDownstreamOperatorStateful_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		boolean result = fixture.isDownstreamOperatorStateful(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
		//       at java.util.ArrayList.rangeCheck(ArrayList.java:604)
		//       at java.util.ArrayList.get(ArrayList.java:382)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext$PlacedOperator.location(OperatorContext.java:386)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext.isDownstreamOperatorStateful(OperatorContext.java:162)
		assertTrue(result);
	}

	/**
	 * Run the boolean isDownstreamOperatorStateful(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsDownstreamOperatorStateful_4()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;

		boolean result = fixture.isDownstreamOperatorStateful(opId);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
		//       at java.util.ArrayList.rangeCheck(ArrayList.java:604)
		//       at java.util.ArrayList.get(ArrayList.java:382)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext$PlacedOperator.location(OperatorContext.java:386)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext.isDownstreamOperatorStateful(OperatorContext.java:162)
		assertTrue(result);
	}

	/**
	 * Run the boolean isDownstreamStateful() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsDownstreamStateful_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		boolean result = fixture.isDownstreamStateful();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
		//       at java.util.ArrayList.rangeCheck(ArrayList.java:604)
		//       at java.util.ArrayList.get(ArrayList.java:382)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext$PlacedOperator.location(OperatorContext.java:386)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext.isDownstreamStateful(OperatorContext.java:176)
		assertTrue(result);
	}

	/**
	 * Run the boolean isDownstreamStateful() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsDownstreamStateful_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		boolean result = fixture.isDownstreamStateful();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
		//       at java.util.ArrayList.rangeCheck(ArrayList.java:604)
		//       at java.util.ArrayList.get(ArrayList.java:382)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext$PlacedOperator.location(OperatorContext.java:386)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext.isDownstreamStateful(OperatorContext.java:176)
		assertTrue(result);
	}

	/**
	 * Run the boolean isDownstreamStateful() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsDownstreamStateful_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		boolean result = fixture.isDownstreamStateful();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
		//       at java.util.ArrayList.rangeCheck(ArrayList.java:604)
		//       at java.util.ArrayList.get(ArrayList.java:382)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext$PlacedOperator.location(OperatorContext.java:386)
		//       at uk.ac.imperial.lsds.seep.operator.OperatorContext.isDownstreamStateful(OperatorContext.java:176)
		assertTrue(result);
	}

	/**
	 * Run the boolean isSink() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsSink_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		boolean result = fixture.isSink();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isSink() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsSink_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(false);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		boolean result = fixture.isSink();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the boolean isSource() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsSource_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		boolean result = fixture.isSource();

		// add additional test code here
		assertEquals(true, result);
	}

	/**
	 * Run the boolean isSource() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testIsSource_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(false);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		boolean result = fixture.isSource();

		// add additional test code here
		assertEquals(false, result);
	}

	/**
	 * Run the OperatorContext.PlacedOperator minimumUpstream() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testMinimumUpstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		OperatorContext.PlacedOperator result = fixture.minimumUpstream();

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.opID());
		assertEquals(0, result.index());
	}

	/**
	 * Run the OperatorContext.PlacedOperator minimumUpstream() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testMinimumUpstream_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		OperatorContext.PlacedOperator result = fixture.minimumUpstream();

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.opID());
		assertEquals(0, result.index());
	}

	/**
	 * Run the OperatorContext.PlacedOperator minimumUpstream() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testMinimumUpstream_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		OperatorContext.PlacedOperator result = fixture.minimumUpstream();

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.opID());
		assertEquals(0, result.index());
	}

	/**
	 * Run the OperatorContext.PlacedOperator minimumUpstream() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testMinimumUpstream_4()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		OperatorContext.PlacedOperator result = fixture.minimumUpstream();

		// add additional test code here
		assertNotNull(result);
		assertEquals(1, result.opID());
		assertEquals(0, result.index());
	}

	/**
	 * Run the void routeValueToDownstream(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testRouteValueToDownstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int streamId = 1;
		int downstream = 1;

		fixture.routeValueToDownstream(streamId, downstream);

		// add additional test code here
	}

	/**
	 * Run the void routeValueToDownstream(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testRouteValueToDownstream_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int streamId = 1;
		int downstream = 1;

		fixture.routeValueToDownstream(streamId, downstream);

		// add additional test code here
	}

	/**
	 * Run the void setDeclaredWorkingAttributes(List<String>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetDeclaredWorkingAttributes_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		List<String> declaredWorkingAttributes = new LinkedList();

		fixture.setDeclaredWorkingAttributes(declaredWorkingAttributes);

		// add additional test code here
	}

	/**
	 * Run the void setDownstreamOperatorStaticInformation(int,OperatorStaticInformation) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetDownstreamOperatorStaticInformation_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opID = 1;
		OperatorStaticInformation loc = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		fixture.setDownstreamOperatorStaticInformation(opID, loc);

		// add additional test code here
	}

	/**
	 * Run the void setInputDataIngestionModePerUpstream(int,InputDataIngestionMode) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetInputDataIngestionModePerUpstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opId = 1;
		InputDataIngestionMode mode = InputDataIngestionMode.ONE_AT_A_TIME;

		fixture.setInputDataIngestionModePerUpstream(opId, mode);

		// add additional test code here
	}

	/**
	 * Run the void setIsSink(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetIsSink_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		boolean isSink = true;

		fixture.setIsSink(isSink);

		// add additional test code here
	}

	/**
	 * Run the void setIsSource(boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetIsSource_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		boolean isSource = true;

		fixture.setIsSource(isSource);

		// add additional test code here
	}

	/**
	 * Run the void setKeyAttribute(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetKeyAttribute_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		String key = "";

		fixture.setKeyAttribute(key);

		// add additional test code here
	}

	/**
	 * Run the void setOperatorStaticInformation(OperatorStaticInformation) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetOperatorStaticInformation_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		OperatorStaticInformation location = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		fixture.setOperatorStaticInformation(location);

		// add additional test code here
	}

	/**
	 * Run the void setOriginalDownstream(ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetOriginalDownstream_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		ArrayList<Integer> originalDownstream = new ArrayList();

		fixture.setOriginalDownstream(originalDownstream);

		// add additional test code here
	}

	/**
	 * Run the void setUpstreamOperatorStaticInformation(int,OperatorStaticInformation) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testSetUpstreamOperatorStaticInformation_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);
		int opID = 1;
		OperatorStaticInformation loc = new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true);

		fixture.setUpstreamOperatorStaticInformation(opID, loc);

		// add additional test code here
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testToString_1()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		String result = fixture.toString();

		// add additional test code here
		assertEquals("@node: Node [ip=null, port=0]inC: 1inD: 1", result);
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testToString_2()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		String result = fixture.toString();

		// add additional test code here
		assertEquals("@node: Node [ip=null, port=0]inC: 1inD: 1", result);
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public void testToString_3()
		throws Exception {
		OperatorContext fixture = new OperatorContext();
		fixture.setKeyAttribute("");
		fixture.setOperatorStaticInformation(new OperatorStaticInformation(1, 1, new Node(1), 1, 1, true));
		fixture.setIsSink(true);
		fixture.setIsSource(true);
		fixture.setDeclaredWorkingAttributes(new LinkedList());
		fixture.setOriginalDownstream(new ArrayList());
		fixture.addUpstream(1);
		fixture.routeInfo = new HashMap();
		fixture.addDownstream(1);

		String result = fixture.toString();

		// add additional test code here
		assertEquals("@node: Node [ip=null, port=0]inC: 1inD: 1", result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:09
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
	 * @generatedBy CodePro at 18/10/13 19:09
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
	 * @generatedBy CodePro at 18/10/13 19:09
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(OperatorContextTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new OperatorContextTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}