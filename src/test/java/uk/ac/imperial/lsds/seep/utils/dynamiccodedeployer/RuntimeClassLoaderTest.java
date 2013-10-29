package uk.ac.imperial.lsds.seep.utils.dynamiccodedeployer;

import java.net.URL;
import java.net.URLClassLoader;

import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.RuntimeClassLoader;
import junit.framework.*;

/**
 * The class <code>RuntimeClassLoaderTest</code> contains tests for the class <code>{@link RuntimeClassLoader}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:00
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class RuntimeClassLoaderTest extends TestCase {
	/**
	 * Run the RuntimeClassLoader(URL[],ClassLoader) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testRuntimeClassLoader_1()
		throws Exception {
		URL[] urls = new URL[] {};
		ClassLoader cl = new URLClassLoader(new URL[] {});

		RuntimeClassLoader result = new RuntimeClassLoader(urls, cl);

		// add additional test code here
		assertNotNull(result);
	}

	/**
	 * Run the void addURL(URL) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testAddURL_1()
		throws Exception {
		RuntimeClassLoader fixture = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));
		URL url = new URL("");

		fixture.addURL(url);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.net.MalformedURLException: no protocol: 
		//       at java.net.URL.<init>(URL.java:585)
		//       at java.net.URL.<init>(URL.java:482)
		//       at java.net.URL.<init>(URL.java:431)
	}

	/**
	 * Run the Class<Object> loadClass(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testLoadClass_1()
		throws Exception {
		RuntimeClassLoader fixture = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));
		String name = "";

//		Class<Object> result = fixture.loadClass(name);

		// add additional test code here
//		assertEquals(null, result);
	}

	/**
	 * Run the Class<Object> loadClass(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testLoadClass_2()
		throws Exception {
		RuntimeClassLoader fixture = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));
		String name = "";

//		Class<Object> result = fixture.loadClass(name);

		// add additional test code here
//		assertEquals(null, result);
	}

	/**
	 * Run the Class<Object> loadClass(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public void testLoadClass_3()
		throws Exception {
		RuntimeClassLoader fixture = new RuntimeClassLoader(new URL[] {}, new URLClassLoader(new URL[] {}));
		String name = "";

//		Class<Object> result = fixture.loadClass(name);

		// add additional test code here
//		assertEquals(null, result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:00
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
	 * @generatedBy CodePro at 18/10/13 19:00
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
	 * @generatedBy CodePro at 18/10/13 19:00
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(RuntimeClassLoaderTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new RuntimeClassLoaderTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}