package uk.ac.imperial.lsds.seep.multi;

public class Utils {
	
	/* 1KB = 1024
	 * 1MB = 1048576
	 * 1GB = 1073741824
	 */
	public static int _BATCH_RECORDS = 1024;
	
	public static int BUNDLE = 1048576;
	
	public static int _CIRCULAR_BUFFER_ = 1073741824;
	
	public static int THREADS = Integer.parseInt(Globals.valueFor("threads"));
	public static int TASKS = 1000000;
	
	public static int _TUPLE_ = 32;
	public static int [] OFFSETS = { 0, 8, 12, 16, 20, 24, 28 };
	
	public static int BATCH =     1;
	public static int RANGE = 16384;
	public static int SLIDE = 16384;
	public static WindowDefinition.WindowType TYPE = 
			WindowDefinition.WindowType.ROW_BASED;
}
