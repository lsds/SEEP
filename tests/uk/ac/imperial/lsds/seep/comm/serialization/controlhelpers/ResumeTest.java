package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.ArrayList;
import junit.framework.*;

/**
 * The class <code>ResumeTest</code> contains tests for the class <code>{@link Resume}</code>.
 *
 * @generatedBy CodePro at 18/10/13 18:59
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ResumeTest extends TestCase {
	/**
	 * Run the Resume() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testResume_1()
		throws Exception {

		Resume result = new Resume();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getOpId());
	}

	/**
	 * Run the Resume(ArrayList<Integer>) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testResume_2()
		throws Exception {
		ArrayList<Integer> opId = new ArrayList();

		Resume result = new Resume(opId);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the ArrayList<Integer> getOpId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testGetOpId_1()
		throws Exception {
		Resume fixture = new Resume(new ArrayList());

		ArrayList<Integer> result = fixture.getOpId();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the void setOpId(ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public void testSetOpId_1()
		throws Exception {
		Resume fixture = new Resume(new ArrayList());
		ArrayList<Integer> opId = new ArrayList();

		fixture.setOpId(opId);

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
	 * @generatedBy CodePro at 18/10/13 18:59
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
	 * @generatedBy CodePro at 18/10/13 18:59
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
	 * @generatedBy CodePro at 18/10/13 18:59
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(ResumeTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ResumeTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}