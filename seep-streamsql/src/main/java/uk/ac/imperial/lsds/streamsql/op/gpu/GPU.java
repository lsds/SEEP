package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class GPU {
	
	private static final String library = "/mnt/data/cccad3/akolious/SEEP/seep-streamsql/jni/GPU.so";
	
	private static final GPU instance = new GPU ();
	
	private byte [] inputArray, outputArray;
	private int startPointer, endPointer;

	private int [] startPointers;
	private int [] endPointers;
	
	private long runs = 0L;
	
	private static Unsafe unsafe;
	
	private static Unsafe getUnsafeMemory () {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			return (Unsafe) theUnsafe.get(null);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	static {
		try {
			System.load (library);
		} catch (final UnsatisfiedLinkError e) {
			System.err.println(e.getMessage());
			System.err.println("error: failed to load GPU library");
			System.exit(1);
		}
		
		unsafe = getUnsafeMemory ();
	}
	
	public static GPU getInstance () { return instance; }
	
	public void setInputDataBuffer (byte [] inputArray) {
		this.inputArray = inputArray;
		
		this.startPointer = 0;
		this.endPointer = inputArray.length;
	}
	
	public void inc () {
		runs += 1;
	}
	
	public void debug() {
		System.out.println(String.format("[DBG] [GPU] %d runs", runs));
	}
	
	public void setInputDataBuffer(byte [] inputArray, int startPointer, int endPointer) {
		this.inputArray = inputArray;
		
		this.startPointer = startPointer;
		this.endPointer = endPointer;
	}
	
	public void setOutputDataBuffer (byte [] outputArray) {
		this.outputArray = outputArray;
	}
	
	public void setWindowStartPointersBuffer(int [] startPointers) {
		this.startPointers = startPointers;
	}
	
	public void setWindowEndPointersBuffer(int [] endPointers) {
		this.endPointers = endPointers;
	}
	
	public void inputDataMovementCallback (long inputAddr, int size) {
		unsafe.copyMemory (
				inputArray, 
				Unsafe.ARRAY_BYTE_BASE_OFFSET, 
				null, 
				inputAddr, 
				size
			);
	}
	
	public void inputDataMovementCallback (long inputAddr) {
		/* System.out.println(String.format("[DBG] inputDataMovementCallback (%d) input data size %d start %d end %d", 
				inputAddr, this.inputArray.length, this.startPointer, this.endPointer)); */
		if (endPointer > startPointer) {
			unsafe.copyMemory (
				this.inputArray, 
				Unsafe.ARRAY_BYTE_BASE_OFFSET + this.startPointer, 
				null, 
				inputAddr, 
				this.endPointer - this.startPointer
			);
		} else {
			/*
			 * TODO: copy in two parts: 
			 * 1) from start, `size - start` bytes; and
			 * 2) from 0, `end` bytes
			 */
			System.err.println("Fatal error.");
			System.exit(1);
		}
	}
	
	public void outputDataMovementCallback (long outputAddr, int size, int offset) {
		unsafe.copyMemory(
				null, 
				outputAddr, 
				outputArray, 
				Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, 
				size
			);
	}
	
	public void windowStartPointersDataMovementCallback (long inputAddr, int size) {
		unsafe.copyMemory (
				startPointers, 
				Unsafe.ARRAY_INT_BASE_OFFSET, 
				null, 
				inputAddr, 
				size
			);
	}
	
	public void windowEndPointersDataMovementCallback (long inputAddr, int size) {
		unsafe.copyMemory (
				endPointers, 
				Unsafe.ARRAY_INT_BASE_OFFSET, 
				null, 
				inputAddr, 
				size
			);
	}
	
	public native int getPlatform ();
	public native int getDevice ();
	public native int createContext ();
	public native int createCommandQueue ();
	
	public native long createInputBuffer (int size);
	public native long createOutputBuffer (int size);
	
	public native long createWindowStartPointersBuffer (int size);
	public native long createWindowEndPointersBuffer (int size);
	
	public native int createProgram (String source);
	public native int createKernel (String name);
	
	public native int setProjectionKernelArgs (int tuples, int localSize, boolean overlap);
	public native int setReductionKernelArgs (int tuples, int localSize, boolean overlap);
	
	public native int invokeKernel (int threads, int threadsPerGroup, boolean overlap, boolean profile);
	public native int releaseAll ();
	
	/* Operator-specific calls */
	public native int invokeSelectionOperatorKernel (Object [] args);
	public native int invokeProjectionOperatorKernel (Object [] args);
	public native int invokeMicroAggregationOperatorKernel (Object [] args);
	public native int invokeReductionOperatorKernel (int threads, int threadsPerGroup, boolean profile);
	
	/* Tests */
	public native void invokeInputDataMovementCallback ();
	public native void invokeGPUWrite ();
	public native void invokeGPURead ();
	public native void invokeOutputDataMovementCallback ();
	public native void invokeAlternativeInputDataMovementCallback ();
	public native void invokeAlternativeGPUWrite ();
	public native void invokeAlternativeGPURead ();
	public native void invokeAlternativeOutputDataMovementCallback ();
	public native void invokeNullKernel ();
}
