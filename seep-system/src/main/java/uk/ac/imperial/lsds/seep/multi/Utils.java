package uk.ac.imperial.lsds.seep.multi;

public class Utils {
	
	public static int BATCH = 64;

	public static int BUNDLE = 1048576;
	
	// Default size of circular input buffers
	public static int _CIRCULAR_BUFFER_ = 1024 * 1024 * 1024; // * 1024 * 1024; // 1073741824;
	
	// Initial size of intermediate array buffers
	public static int _UNBOUNDED_BUFFER_ = 2 * 1024 * 1024; // 2 * 1024 * 1024; // * 1024; // 32 * 1024; // 1048576; // Integer.parseInt(Globals.valueFor("intermediate"));
	
	// Number of CPU threads
	public static int THREADS = Integer.parseInt(Globals.valueFor("threads"));
	public static int TASKS = 2 * 1024 * 1024;
	
	/* GPU-specific constants */
	
	public static boolean GPU = Boolean.parseBoolean(Globals.valueFor("GPU"));
	/* Check if hybrid execution mode is enabled */
	public static boolean CPU = Boolean.parseBoolean(Globals.valueFor("CPU"));
	
	public static boolean HYBRID = (CPU && GPU);
	
	public static int _GPU_INPUT_  = Integer.parseInt(Globals.valueFor( "in"));
	public static int _GPU_OUTPUT_ = Integer.parseInt(Globals.valueFor("out"));
}
