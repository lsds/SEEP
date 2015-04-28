package uk.ac.imperial.lsds.seep.multi;

public class Utils {
	
	public static int BATCH = 32;

	public static int BUNDLE = 1048576;
	
	// Default size of circular input buffers
	public static int _CIRCULAR_BUFFER_ = 1073741824;
	
	// Initial size of intermediate array buffers
	public static int _UNBOUNDED_BUFFER_ = 1048576;
	
	// Number of CPU threads
	public static int THREADS = Integer.parseInt(Globals.valueFor("threads"));
	public static int TASKS = 2 * 1024 * 1024;
	
	/* GPU-specific constants */
	
	public static boolean GPU = Boolean.parseBoolean(Globals.valueFor("GPU"));
	/* Check if hybrid execution mode is enabled */
	public static boolean CPU = Boolean.parseBoolean(Globals.valueFor("CPU"));
	
	public static boolean HYBRID = (CPU && GPU);
	
	public static boolean LATENCY_ON = false;
	
	public static String SEEP_HOME = "/home/akolious/seep";
	
	public static long pack (long left, long right) {
		return (left << 32) | right;
	}
	
	public static int unpack (int idx, long value) {
        if (idx == 0) { /* left */
            return (int) (value >> 32);
        } else
        if (idx == 1) { /* right value */
            return (int) value;
        } else {
            return -1;
        }
    }
}
