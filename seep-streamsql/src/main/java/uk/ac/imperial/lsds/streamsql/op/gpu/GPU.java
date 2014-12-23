package uk.ac.imperial.lsds.streamsql.op.gpu;

import java.nio.ByteBuffer;

public class GPU {
	
	private static final String library = "/mnt/data/cccad3/akolious/SEEP/seep-streamsql/jni/GPU.so";
	
	private static final GPU instance = new GPU ();
	
	static {
		try {
			System.load (library);
		} catch (final UnsatisfiedLinkError e) {
			System.err.println(e.getMessage());
			System.err.println("error: failed to load GPU library");
			System.exit(1);
		}
	}
	
	public static GPU getInstance () { return instance; }
	
	public native int getPlatform ();
	public native int getDevice ();
	public native int createContext ();
	public native int createCommandQueue ();
	public native int createProgram (String source);
	public native long createInputBuffer (int size);
	public native long createOutputBuffer (int size);
	public native int createKernel (String name);
	public native int setKernelArgs (int tuples, int localSize, boolean overlap);
	public native int invokeKernel (int threads, int threadsPerGroup, boolean overlap, boolean profile);
	public native int releaseAll ();
	/* Operator-specific calls */
	public native int invokeSelectionOperatorKernel (Object [] args);
	public native int invokeProjectionOperatorKernel (Object [] args);
	public native int invokeMicroAggregationOperatorKernel (Object [] args);
}
