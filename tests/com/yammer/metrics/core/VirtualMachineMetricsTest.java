package com.yammer.metrics.core;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServer;
import junit.framework.*;

/**
 * The class <code>VirtualMachineMetricsTest</code> contains tests for the class <code>{@link VirtualMachineMetrics}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:08
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class VirtualMachineMetricsTest extends TestCase {
	/**
	 * Run the VirtualMachineMetrics(MemoryMXBean,List<MemoryPoolMXBean>,OperatingSystemMXBean,ThreadMXBean,List<GarbageCollectorMXBean>,RuntimeMXBean,MBeanServer) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testVirtualMachineMetrics_1()
		throws Exception {
		MemoryMXBean memory = null;
		List<MemoryPoolMXBean> memoryPools = new LinkedList();
		OperatingSystemMXBean os = null;
		ThreadMXBean threads = null;
		List<GarbageCollectorMXBean> garbageCollectors = new LinkedList();
		RuntimeMXBean runtime = null;
		MBeanServer mBeanServer = null;

		VirtualMachineMetrics result = new VirtualMachineMetrics(memory, memoryPools, os, threads, garbageCollectors, runtime, mBeanServer);

		// add additional test code here
		assertNotNull(result);
		assertEquals("Java HotSpot(TM) 64-Bit Server VM", result.getName());
		assertEquals("1.7.0_17-b02", result.getVersion());
	}

	/**
	 * Run the Map<String, VirtualMachineMetrics.BufferPoolStats> getBufferPoolStats() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetBufferPoolStats_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, VirtualMachineMetrics.BufferPoolStats> result = fixture.getBufferPoolStats();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getBufferPoolStats(VirtualMachineMetrics.java:470)
		assertNotNull(result);
	}

	/**
	 * Run the Map<String, VirtualMachineMetrics.BufferPoolStats> getBufferPoolStats() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetBufferPoolStats_2()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, VirtualMachineMetrics.BufferPoolStats> result = fixture.getBufferPoolStats();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getBufferPoolStats(VirtualMachineMetrics.java:470)
		assertNotNull(result);
	}

	/**
	 * Run the Map<String, VirtualMachineMetrics.BufferPoolStats> getBufferPoolStats() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetBufferPoolStats_3()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, VirtualMachineMetrics.BufferPoolStats> result = fixture.getBufferPoolStats();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getBufferPoolStats(VirtualMachineMetrics.java:470)
		assertNotNull(result);
	}

	/**
	 * Run the Map<String, VirtualMachineMetrics.BufferPoolStats> getBufferPoolStats() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetBufferPoolStats_4()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, VirtualMachineMetrics.BufferPoolStats> result = fixture.getBufferPoolStats();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getBufferPoolStats(VirtualMachineMetrics.java:470)
		assertNotNull(result);
	}

	/**
	 * Run the Map<String, VirtualMachineMetrics.BufferPoolStats> getBufferPoolStats() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetBufferPoolStats_5()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, VirtualMachineMetrics.BufferPoolStats> result = fixture.getBufferPoolStats();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getBufferPoolStats(VirtualMachineMetrics.java:470)
		assertNotNull(result);
	}

	/**
	 * Run the Map<String, VirtualMachineMetrics.BufferPoolStats> getBufferPoolStats() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetBufferPoolStats_6()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, VirtualMachineMetrics.BufferPoolStats> result = fixture.getBufferPoolStats();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getBufferPoolStats(VirtualMachineMetrics.java:470)
		assertNotNull(result);
	}

	/**
	 * Run the Map<String, VirtualMachineMetrics.BufferPoolStats> getBufferPoolStats() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetBufferPoolStats_7()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, VirtualMachineMetrics.BufferPoolStats> result = fixture.getBufferPoolStats();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getBufferPoolStats(VirtualMachineMetrics.java:470)
		assertNotNull(result);
	}

	/**
	 * Run the int getDaemonThreadCount() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetDaemonThreadCount_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		int result = fixture.getDaemonThreadCount();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getDaemonThreadCount(VirtualMachineMetrics.java:317)
		assertEquals(0, result);
	}

	/**
	 * Run the Set<String> getDeadlockedThreads() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetDeadlockedThreads_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Set<String> result = fixture.getDeadlockedThreads();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getDeadlockedThreads(VirtualMachineMetrics.java:341)
		assertNotNull(result);
	}

	/**
	 * Run the Set<String> getDeadlockedThreads() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetDeadlockedThreads_2()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Set<String> result = fixture.getDeadlockedThreads();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getDeadlockedThreads(VirtualMachineMetrics.java:341)
		assertNotNull(result);
	}

	/**
	 * Run the Set<String> getDeadlockedThreads() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetDeadlockedThreads_3()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Set<String> result = fixture.getDeadlockedThreads();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getDeadlockedThreads(VirtualMachineMetrics.java:341)
		assertNotNull(result);
	}

	/**
	 * Run the Set<String> getDeadlockedThreads() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetDeadlockedThreads_4()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Set<String> result = fixture.getDeadlockedThreads();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getDeadlockedThreads(VirtualMachineMetrics.java:341)
		assertNotNull(result);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_2()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_3()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_4()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_5()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_6()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_7()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_8()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_9()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_10()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_11()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_12()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getFileDescriptorUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetFileDescriptorUsage_13()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getFileDescriptorUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getFileDescriptorUsage(VirtualMachineMetrics.java:256)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the Map<String, VirtualMachineMetrics.GarbageCollectorStats> getGarbageCollectors() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetGarbageCollectors_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, VirtualMachineMetrics.GarbageCollectorStats> result = fixture.getGarbageCollectors();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the Map<String, VirtualMachineMetrics.GarbageCollectorStats> getGarbageCollectors() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetGarbageCollectors_2()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, VirtualMachineMetrics.GarbageCollectorStats> result = fixture.getGarbageCollectors();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the double getHeapCommitted() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetHeapCommitted_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getHeapCommitted();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getHeapCommitted(VirtualMachineMetrics.java:208)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getHeapInit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetHeapInit_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getHeapInit();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getHeapInit(VirtualMachineMetrics.java:184)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getHeapMax() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetHeapMax_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getHeapMax();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getHeapMax(VirtualMachineMetrics.java:200)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getHeapUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetHeapUsage_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getHeapUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getHeapUsage(VirtualMachineMetrics.java:217)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getHeapUsed() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetHeapUsed_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getHeapUsed();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getHeapUsed(VirtualMachineMetrics.java:192)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the VirtualMachineMetrics getInstance() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetInstance_1()
		throws Exception {

		VirtualMachineMetrics result = VirtualMachineMetrics.getInstance();

		// add additional test code here
		assertNotNull(result);
		assertEquals("Java HotSpot(TM) 64-Bit Server VM", result.getName());
		assertEquals(5.34970368E8, result.getHeapCommitted(), 1.0);
		assertEquals(0.229736328125, result.getFileDescriptorUsage(), 1.0);
		assertEquals(6.85768704E8, result.getTotalCommitted(), 1.0);
		assertEquals(37, result.getDaemonThreadCount());
		assertEquals(8.53737472E8, result.getTotalMax(), 1.0);
		assertEquals(1.34217728E8, result.getHeapInit(), 1.0);
		assertEquals(0.47116676129792867, result.getNonHeapUsage(), 1.0);
		assertEquals(5.34970368E8, result.getHeapMax(), 1.0);
		assertEquals(3.3866396E8, result.getHeapUsed(), 1.0);
		assertEquals(0.6330518104509295, result.getHeapUsage(), 1.0);
		assertEquals(286903L, result.getUptime());
		assertEquals(1.58531584E8, result.getTotalInit(), 1.0);
		assertEquals(4.98894248E8, result.getTotalUsed(), 1.0);
		assertEquals(70, result.getThreadCount());
		assertEquals("1.7.0_17-b02", result.getVersion());
	}

	/**
	 * Run the Map<String, Double> getMemoryPoolUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetMemoryPoolUsage_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, Double> result = fixture.getMemoryPoolUsage();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the Map<String, Double> getMemoryPoolUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetMemoryPoolUsage_2()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, Double> result = fixture.getMemoryPoolUsage();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the Map<String, Double> getMemoryPoolUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetMemoryPoolUsage_3()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<String, Double> result = fixture.getMemoryPoolUsage();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	/**
	 * Run the String getName() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetName_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		String result = fixture.getName();

		// add additional test code here
		assertEquals("Java HotSpot(TM) 64-Bit Server VM", result);
	}

	/**
	 * Run the double getNonHeapUsage() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetNonHeapUsage_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getNonHeapUsage();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getNonHeapUsage(VirtualMachineMetrics.java:228)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the int getThreadCount() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadCount_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		int result = fixture.getThreadCount();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadCount(VirtualMachineMetrics.java:308)
		assertEquals(0, result);
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_2()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_3()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_4()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_5()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_6()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_7()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_8()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_9()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_10()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_11()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_12()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_13()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_14()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_15()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the void getThreadDump(OutputStream) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadDump_16()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);
		OutputStream out = new ByteArrayOutputStream();

		fixture.getThreadDump(out);

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadDump(VirtualMachineMetrics.java:398)
	}

	/**
	 * Run the Map<Thread.State, Double> getThreadStatePercentages() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadStatePercentages_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<Thread.State, Double> result = fixture.getThreadStatePercentages();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadStatePercentages(VirtualMachineMetrics.java:375)
		assertNotNull(result);
	}

	/**
	 * Run the Map<Thread.State, Double> getThreadStatePercentages() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadStatePercentages_2()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<Thread.State, Double> result = fixture.getThreadStatePercentages();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadStatePercentages(VirtualMachineMetrics.java:375)
		assertNotNull(result);
	}

	/**
	 * Run the Map<Thread.State, Double> getThreadStatePercentages() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetThreadStatePercentages_3()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		Map<Thread.State, Double> result = fixture.getThreadStatePercentages();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getThreadStatePercentages(VirtualMachineMetrics.java:375)
		assertNotNull(result);
	}

	/**
	 * Run the double getTotalCommitted() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetTotalCommitted_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getTotalCommitted();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getTotalCommitted(VirtualMachineMetrics.java:175)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getTotalInit() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetTotalInit_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getTotalInit();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getTotalInit(VirtualMachineMetrics.java:146)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getTotalMax() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetTotalMax_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getTotalMax();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getTotalMax(VirtualMachineMetrics.java:166)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the double getTotalUsed() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetTotalUsed_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		double result = fixture.getTotalUsed();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getTotalUsed(VirtualMachineMetrics.java:156)
		assertEquals(0.0, result, 0.1);
	}

	/**
	 * Run the long getUptime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetUptime_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		long result = fixture.getUptime();

		// add additional test code here
		// An unexpected exception was thrown in user code while executing this test:
		//    java.lang.NullPointerException
		//       at com.yammer.metrics.core.VirtualMachineMetrics.getUptime(VirtualMachineMetrics.java:299)
		assertEquals(0L, result);
	}

	/**
	 * Run the String getVersion() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public void testGetVersion_1()
		throws Exception {
		VirtualMachineMetrics fixture = new VirtualMachineMetrics((MemoryMXBean) null, new LinkedList(), (OperatingSystemMXBean) null, (ThreadMXBean) null, new LinkedList(), (RuntimeMXBean) null, (MBeanServer) null);

		String result = fixture.getVersion();

		// add additional test code here
		assertEquals("1.7.0_17-b02", result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:08
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
	 * @generatedBy CodePro at 18/10/13 19:08
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
	 * @generatedBy CodePro at 18/10/13 19:08
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(VirtualMachineMetricsTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new VirtualMachineMetricsTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}