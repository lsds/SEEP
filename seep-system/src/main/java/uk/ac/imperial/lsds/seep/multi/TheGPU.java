package uk.ac.imperial.lsds.seep.multi;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class TheGPU {
	
	private static final int maxQueries =  5;
	private static final int maxBuffers = 10;
	
	private static final String gpuLibrary = 
		Utils.SEEP_HOME + "/seep-system/clib/libGPU.so";
	
	private static final TheGPU gpuInstance = new TheGPU ();
	
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
			System.load (gpuLibrary);
		} catch (final UnsatisfiedLinkError e) {
			System.err.println(e.getMessage());
			System.err.println("error: failed to load GPU library");
			System.exit(1);
		}
		theUnsafe = getUnsafeMemory ();
	}
	
	public static TheGPU getInstance () { return gpuInstance; }
	
	private byte [][][] inputs;
	private int  [][]   start; /* Start and end pointers for inputs, 
	since they are segments of a circular buffer */
	private int  [][]   end;
	private byte [][][] outputs;
	
	private int  [][] outputPositions;
	
	public TheGPU () {
		
		inputs  = new byte [maxQueries][maxBuffers][];
		start   = new int  [maxQueries][maxBuffers];
		end     = new int  [maxQueries][maxBuffers];
		outputs = new byte [maxQueries][maxBuffers][];
		
		outputPositions = new int [maxQueries][maxBuffers];
	}
	
	public void setInputBuffer (int qid, int ndx, byte [] input) {
		setInputBuffer (qid, ndx, input, 0, input.length);
	}
	
	public void setInputBuffer (int qid, int ndx, byte [] input, int start, int end) {
		/* Check bounds */
		if (qid < 0 || qid >= maxQueries)
			throw new IllegalArgumentException ("error: invalid query id");
		
		if (ndx < 0 || ndx >= maxBuffers)
			throw new IllegalArgumentException ("error: invalid buffer id");
		
		this.inputs [qid][ndx] = input;
		this.start  [qid][ndx] = start;
		this.end    [qid][ndx] =   end;
	}
	
	public void setOutputBuffer (int qid, int ndx, byte [] output) {
		/* Check bounds */
		if (qid < 0 || qid >= maxQueries)
			throw new IllegalArgumentException ("error: invalid query id");
		
		if (ndx < 0 || ndx >= maxBuffers)
			throw new IllegalArgumentException ("error: invalid buffer id");
		
		this.outputs [qid][ndx] = output;
	}
	
	public int getPosition (int qid, int ndx) {
		
		return outputPositions[qid][ndx];
	}
	
	public void inputDataMovementCallback (int qid, int ndx, long address, int size, int offset) {
		/* Check bounds */
		if (qid < 0 || qid >= maxQueries)
			throw new IllegalArgumentException ("error: invalid query id");
		
		if (ndx < 0 || ndx >= maxBuffers)
			throw new IllegalArgumentException ("error: invalid buffer id");
		/*
		System.out.println(String.format("[DBG] copy input: q %d ndx %d size %d (%d) offset %d", 
			qid, ndx, size, end[qid][ndx] - start[qid][ndx], offset));
		*/
		if (end[qid][ndx] > start[qid][ndx]) {
			theUnsafe.copyMemory (
				inputs[qid][ndx], 
				Unsafe.ARRAY_BYTE_BASE_OFFSET + start[qid][ndx], 
				null, 
				address + offset, 
				end[qid][ndx] - start[qid][ndx]
			);
		} else {
			/* Copy in two parts: from start to `capacity - start` (left) and from 0 to `end` (right) */
			int left  = inputs[qid][ndx].length - start[qid][ndx];
			int right = end   [qid][ndx];
			theUnsafe.copyMemory (
				inputs[qid][ndx], 
				Unsafe.ARRAY_BYTE_BASE_OFFSET + start[qid][ndx], 
				null, 
				address + offset, 
				left
			);
			theUnsafe.copyMemory (
				inputs[qid][ndx], 
				Unsafe.ARRAY_BYTE_BASE_OFFSET + 0, 
				null, 
				address + offset + left, 
				right
			);
		}
	}
	
	public void outputDataMovementCallback (int qid, int ndx, long address, int size, int offset) {
		/* Check bounds */
		if (qid < 0 || qid >= maxQueries)
			throw new IllegalArgumentException ("error: invalid query id");
		
		if (ndx < 0 || ndx >= maxBuffers)
			throw new IllegalArgumentException ("error: invalid buffer id");
		
		if (size > outputs[qid][ndx].length) {
			System.err.println(String.format("error: output buffer overflow (%d bytes)", size));
			System.exit(-1);
		}
		/*
		System.out.println(String.format("[DBG] copy output: q %d ndx %d size %d offset %d", 
			qid, ndx, size, offset));
		*/
		theUnsafe.copyMemory(
			null, 
			address, 
			outputs[qid][ndx], 
			Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, 
			size
		);
		/* This is used to set the position of the output buffer */
		outputPositions[qid][ndx] = size;
	}
	
	public native int init (int N);
	public native int getQuery (String source, int kernels, int inputs, int outputs);
	public native int setInput (int queryId, int index, int size);
	public native int setOutput (int queryId, int index, int size, 
		int writeOnly, int doNotMove, int bearsMark, int readEvent);
	public native int execute (int queryId, int [] threads, int [] threadsPerGroup);
	public native int free ();
	
	/* Operator-specific function calls */
	public native int setKernelDummy     (int queryId, int [] args);
	public native int setKernelProject   (int queryId, int [] args);
	public native int setKernelSelect    (int queryId, int [] args);
	public native int setKernelReduce    (int queryId, int [] args);
	public native int setKernelAggregate (int queryId, int [] args);
	public native int setKernelThetaJoin (int queryId, int [] args);
	
	public native int executePartialReduce 
		(int queryId, int [] threads, int [] threadsPerGroup, long [] args);
	
	public native int executePartialAggregate 
		(int queryId, int [] threads, int [] threadsPerGroup, long [] args);
	
	public native int setKernelPartialReduce    (int queryId, int [] intArgs, long [] longArgs);
	public native int setKernelPartialAggregate (int queryId, int [] intArgs, long [] longArgs);
	
	/* LRB UDF */
	public native int setKernelAggregateIStream (int queryId, int [] args);
	
	/*
	 * Direct ByteBuffers
	 * 
	 * The GPU library manages a pool of (up to MAX_BUFFERS) buffers,
	 * allocated using the OpenCL API. These buffers are set for DMA.
	 * 
	 * For every buffer allocated, we can return a direct byte buffer
	 * object via JNI, using the address of DMA-capable buffers.
	 */
	public native int allocateBuffer (int size, int readOnly);
	public native Object getDirectByteBuffer (int idx);
	/* 
	 * 
	 */
	public native int setDirectInput (int queryId, int index, int size, int bufferId);
	/*
	 * 
	 */
	public native int setDirectOutput (int queryId, int index, int size, 
		int writeOnly, int doNotMove, int bearsMark, int readEvent, int bufferId);
	/*
	 * 
	 */
	public native int executeDirect (int queryId, int [] threads, int [] threadsPerGroup, 
		int [] start, int [] end);

	public native int configurePartialReduce    (int qid, long[] longArgs);
	public native int configurePartialAggregate (int qid, long[] longArgs);
}
