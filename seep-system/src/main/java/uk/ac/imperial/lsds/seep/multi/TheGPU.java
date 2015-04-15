package uk.ac.imperial.lsds.seep.multi;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class TheGPU {
	
	private static final int maxQueries =  5;
	private static final int maxBuffers = 10;
	
	private static final String gpuLibrary = 
		"/home/akolious/tests/saber-jni/libGPU.so";
	
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
	
	public TheGPU () {
		
		inputs  = new byte [maxQueries][maxBuffers][];
		start   = new int  [maxQueries][maxBuffers];
		end     = new int  [maxQueries][maxBuffers];
		outputs = new byte [maxQueries][maxBuffers][];
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
	
	public void inputDataMovementCallback (int qid, int ndx, long address, int size, int offset) {
		/* Check bounds */
		if (qid < 0 || qid >= maxQueries)
			throw new IllegalArgumentException ("error: invalid query id");
		
		if (ndx < 0 || ndx >= maxBuffers)
			throw new IllegalArgumentException ("error: invalid buffer id");
		
		System.out.println(String.format("[DBG] copy input: q %d ndx %d size %d (%d) offset %d", 
			qid, ndx, size, end[qid][ndx] - start[qid][ndx], offset));
		
		if (end[qid][ndx] > start[qid][ndx]) {
			theUnsafe.copyMemory (
				inputs[qid][ndx], 
				Unsafe.ARRAY_BYTE_BASE_OFFSET + start[qid][ndx], 
				null, 
				address + offset, 
				end[qid][ndx] - start[qid][ndx]
			);
		} else {
			System.err.println("Fatal error: unsupported operation");
			System.exit(1);
		}
	}
	
	public void outputDataMovementCallback (int qid, int ndx, long address, int size, int offset) {
		/* Check bounds */
		if (qid < 0 || qid >= maxQueries)
			throw new IllegalArgumentException ("error: invalid query id");
		
		if (ndx < 0 || ndx >= maxBuffers)
			throw new IllegalArgumentException ("error: invalid buffer id");
		
		System.out.println(String.format("[DBG] copy output: q %d ndx %d size %d offset %d", 
			qid, ndx, size, offset));
		
		theUnsafe.copyMemory(
			null, 
			address, 
			outputs[qid][ndx], 
			Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, 
			size
		);
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
}
