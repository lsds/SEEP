package uk.ac.imperial.lsds.seep.multi;

public class Utils {
	
	/* 1KB = 1024
	 * 1MB = 1048576
	 * 1GB = 1073741824
	 */
	public static int _BATCH_RECORDS = 1024;
	
	public static int BUNDLE = 1048576;
	
	public static int _CIRCULAR_BUFFER_ = 1073741824;
	
	public static int _UNBOUNDED_BUFFER_ = Integer.parseInt(Globals.valueFor("intermediate"));
	
	public static int THREADS = Integer.parseInt(Globals.valueFor("threads"));
	public static int TASKS = 1000000;
	
	public static int _TUPLE_ = 32;
	public static int [] OFFSETS = { 0, 8, 12, 16, 20, 24, 28 };
	
	public static int JOIN_BATCH = 10000;	
	public static int BATCH = Integer.parseInt(Globals.valueFor("batch"));
	public static int RANGE = Integer.parseInt(Globals.valueFor("range"));
	public static int SLIDE = Integer.parseInt(Globals.valueFor("slide"));
	public static WindowDefinition.WindowType TYPE = 
			WindowDefinition.WindowType.ROW_BASED;
	
	/* GPU-specific constants */
	
	public static boolean GPU = Boolean.parseBoolean(Globals.valueFor("GPU"));
	/* Check if hybrid execution mode is enabled */
	public static boolean CPU = Boolean.parseBoolean(Globals.valueFor("CPU"));
	
	public static boolean HYBRID = (CPU && GPU);
	
	public static int _GPU_INPUT_  = Integer.parseInt(Globals.valueFor( "in"));
	public static int _GPU_OUTPUT_ = Integer.parseInt(Globals.valueFor("out"));
}
