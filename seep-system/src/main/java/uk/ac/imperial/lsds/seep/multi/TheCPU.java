package uk.ac.imperial.lsds.seep.multi;

public class TheCPU {
	
	private static final String cpuLibrary = 
		Utils.SEEP_HOME + "/seep-system/clib/libCPU.so";
	
	private static final TheCPU cpuInstance = new TheCPU ();
	
	static {
		try {
			System.load (cpuLibrary);
		} catch (final UnsatisfiedLinkError e) {
			System.err.println(e.getMessage());
			System.err.println("error: failed to load CPU library");
			System.exit(1);
		}
	}
	
	public static TheCPU getInstance () { return cpuInstance; }
	
	/* Thread affinity functions */
	public native int getNumCores ();
	public native int bind (int cpu);
	public native int unbind ();
	public native int getCpuId ();
}
