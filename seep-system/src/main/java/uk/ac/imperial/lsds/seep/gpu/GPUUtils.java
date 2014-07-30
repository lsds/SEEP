package uk.ac.imperial.lsds.seep.gpu;

public class GPUUtils {
	
	public static final boolean DEBUG = true;
	
	public static synchronized void out (String s) {
		
		if (DEBUG) {
			String x = String.format("[DBG] %s%n", s);
			System.out.print(x);
			System.out.flush();
		}
	}
	
	public static synchronized void err (String s) {
		
		String x = String.format("[ERR] %s%n", s);
		System.err.print(x);
		System.err.flush();
	}
}
