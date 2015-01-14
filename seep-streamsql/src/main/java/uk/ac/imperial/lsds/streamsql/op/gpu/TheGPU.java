package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class TheGPU {
	
	private static final String library = "/mnt/data/cccad3/akolious/SEEP/seep-streamsql/clib/libGPU.so";
	
	private static final TheGPU instance = new TheGPU ();
	
	private static Unsafe theUnsafe;
	
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
		theUnsafe = getUnsafeMemory ();
	}
	
	public static TheGPU getInstance () { return instance; }
	
	private byte [] inputArray;
	private int start;
	private int end;
	
	private byte [] outputArray;
	
	public void setInputBuffer (byte [] inputArray) {
		setInputBuffer (inputArray, 0, inputArray.length);
	}
	
	public void setInputBuffer(byte [] inputArray, int start, int end) {
		this.inputArray = inputArray;
		this.start = start;
		this.end = end;
	}
	
	public void setOutputBuffer (byte [] outputArray) {
		this.outputArray = outputArray;
	}
	
	public void inputDataMovementCallback (int qid, int ndx, long inputAddr, int size, int offset) {
		
		theUnsafe.copyMemory (
				inputArray, 
				Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, 
				null, 
				inputAddr, 
				size
			);
		/*
		if (end > start) {
			theUnsafe.copyMemory (
				this.inputArray, 
				Unsafe.ARRAY_BYTE_BASE_OFFSET + start, 
				null, 
				inputAddr, 
				end - start
			);
		} else {
			//
			// TODO: copy in two parts: 
			// 1) from start, `size - start` bytes; and
			// 2) from 0, `end` bytes
			//
			System.err.println("Fatal error.");
			System.exit(1);
		}
		*/
	}
	
	public void outputDataMovementCallback (int qid, int ndx, long outputAddr, int size, int offset) {
		theUnsafe.copyMemory(
				null, 
				outputAddr, 
				outputArray, 
				Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, 
				size
			);
	}
	
	public native int init (int N);
	public native int getQuery (String source, int kernels, int inputs, int outputs);
	public native int setInput (int queryId, int index, int size);
	public native int setOutput (int queryId, int index, int size, int writeOnly);
	public native int execute (int queryId, int threads, int threadsPerGroup);
	public native int free ();
	/* Operator-specific function calls */
	public native int setKernelDummy   (int queryId, int [] args);
	public native int setKernelProject (int queryId, int [] args);
	public native int setKernelSelect  (int queryId, int [] args);
	public native int setKernelReduce  (int queryId, int [] args);
}
