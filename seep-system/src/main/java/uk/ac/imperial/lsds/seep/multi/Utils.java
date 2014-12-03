package uk.ac.imperial.lsds.seep.multi;

public class Utils {
	
	public static int BUNDLE = 1048576;
	public static int _CIRCULAR_BUFFER_ = 1073741824;
	
	public static int THREADS = Integer.parseInt(Globals.valueFor("threads"));
	public static int TASKS = 1000000;
	
	public static int _TUPLE_ = 32;
	public static int [] OFFSETS = { 0, 8, 12, 16, 20, 24, 28 };
	
	public static int BATCH =      1;
	public static int RANGE =  65536;
	public static int SLIDE =  65536;
	public static WindowDefinition.WindowType TYPE = 
			WindowDefinition.WindowType.RANGE_BASED;
}
