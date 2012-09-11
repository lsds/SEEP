package seep.utils;

/// \fixme{this class must dissappear. When using this, if the class is unloaded by the JVM, then all variables becomes null}

public class ExecutionConfiguration{

	//Infrastructure dependent parameters
	public static int baseId = 50;
	public static int controlSocket = 50000;
	public static int dataSocket = 40000;
	
	public static boolean twitterStormModel = false;
	public static boolean upstreamBackupModel = false;
	public static boolean newModel = false;
	public static boolean parallelRecovery = false;
	//5 seconds
	public static int monitorInterval = 5;
	
	public static String mainAddr = "10.80.127.191";
//	public static String mainAddr = "146.169.5.130";

	public static int mainPort = 3500;
	public static int monitorManagerPort = 5555;
	public static double cpuUThreshold = 50;
	public static int numMaxAlerts = 2;
	public static boolean enableAutomaticScaleOut = true;
	public static long minimumTimeBetweenSplit = 6;
	public static String fileWithCpuU = "OUT";
	//the minimum number of nodes available, or node pool size
	public static int minimumNodesAvailable = 10;
	//activate-deactivate elastic+ft mechanism
	public static boolean eftMechanismEnabled = false;
//	public static boolean withRisk = true;
	
	/** SMART WORD COUNTER **/
	public static int sentenceSize = 568;
	public static boolean maxRate = false;
	public static int eventR = 1;
	public static int period = 1000;
	public static double th = 1;
	/** END SWC **/
	
	/** LRB query **/
	public static String pathToInputFile = "res/datafile3hours.dat";
	public static String pathToInputFileConstant = "res/inputConstant.dat";
	public static String pathToOutputFile = "res/output.dat";
	public static String pathToOutputFileConstant = "res/outputConstant.dat";
	public static boolean normalLRB = true;
	public static int numberOfXWays = 1000;
	
	public static double cpuUThresholdTC = 50;
	public static double cpuUThresholdFW = 30;
	
	/** END LRB **/
	
	/** BATCHING CONFIGURATION **/
	//batch tupleSize in bytes
	public static int tupleSize = 40;
	//packet size in bytes
	public static int packetSize = 16000;
	public static int batchLimit = 1;//((int)(packetSize/tupleSize)-1);
	//maximum latency allowed for a packet to be sent, in milliseconds
	public static int maxLatencyAllowed = 250;
	/** END BATCHING **/
	
	
	
	/** General todos**/
	/// \todo{contentBasedrouting with the loadBalancer structure is not good designed right now. The low level is well ordered, but the high level must be refactored.
	///it is necessary to define good interfaces, and then provide a high level API on top of that}
	
	/// \todo{The comm relies on GPB, this must be pluggable}
	
	/** **/
}
