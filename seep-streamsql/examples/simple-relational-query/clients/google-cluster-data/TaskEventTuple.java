import java.util.HashMap;
import java.lang.NumberFormatException;

public class TaskEventTuple {
	
	private long   timestamp;
	private int      missing; 
	private long       jobId;
	private long      taskId;
	private long   machineId;
	private int    eventType;
	private int       userId;
	private int     category; /* Scheduling class */
	private int     priority;
	private float        cpu;
	private float        ram;
	private float       disk;
	private int   constraint;
	
	/* My Users */
	private static int nextUserId = 1;
	private static HashMap<String,Integer> users 
		= new HashMap<String,Integer>();
	
	public TaskEventTuple () {
		/* Initialise all attributes */
		reset ();
	}
	
	private void reset () {
		this.timestamp  = 0L;
		this.missing    = 0;
		this.jobId      = 0L;
		this.taskId     = 0L;
		this.machineId  = 0L;
		this.eventType  = 0;
		this.userId     = 0;
		this.category   = 0;
		this.priority   = 0;
		this.cpu        = 0;
		this.ram        = 0;
		this.disk       = 0;
		this.constraint = 0;
	}
	
	public static void parse (String line, TaskEventTuple t) {
		String [] s = line.split(",", -1); /* Do not ignore empty strings */
		
		t.jobId      = Long.parseLong  (s[ 2]);
		t.taskId     = Long.parseLong  (s[ 3]);
		t.eventType  = Integer.parseInt(s[ 5]);
		t.category   = Integer.parseInt(s[ 7]);
		t.priority   = Integer.parseInt(s[ 8]);
		
		t.missing    = (! s[ 1].isEmpty()) ? Integer.parseInt(s[ 1]) : -1;
		t.machineId  = (! s[ 4].isEmpty()) ? Long.parseLong  (s[ 4]) : -1;
		t.cpu        = (! s[ 9].isEmpty()) ? Float.parseFloat(s[ 9]) : -1;
		t.ram        = (! s[10].isEmpty()) ? Float.parseFloat(s[10]) : -1;
		t.disk       = (! s[11].isEmpty()) ? Float.parseFloat(s[11]) : -1;
		t.constraint = (! s[12].isEmpty()) ? Integer.parseInt(s[12]) : -1;
		
		/* Check for timestamps whose value is max uint64_t */
		try {
			t.timestamp  = Long.parseLong(s[0]);
			t.timestamp -= 600000000;
			t.timestamp /= 1000000; /* Convert to seconds */
		} catch (NumberFormatException e) {
			/* System.err.println(String.format("warning: invalid timestamp %s", s[0])); */
			t.timestamp = -1;
		}
		
		/* Convert user id strings to integers */
		if (s[6].isEmpty()) {
			System.err.println("warning: undefined user");
			t.userId = -1;
		} else {
			if (users.containsKey(s[6])) {
				t.userId = users.get(s[6]).intValue();
			} else {
				t.userId = nextUserId++;
				/* Check bounds */
				if (nextUserId == (Integer.MAX_VALUE - 1)) {
					System.err.println("error: invalid user id");
					System.exit(1);
				}
				users.put(s[6], new Integer(t.userId)); /* Update hashmap */
			}
		}
	}
	
	public long  getTimestamp ()  { return  timestamp; }
	public int   getMissing ()    { return    missing; }
	public long  getJobId ()      { return      jobId; }
	public long  getTaskId ()     { return     taskId; }
	public long  getMachineId ()  { return  machineId; }
	public int   getEventType ()  { return  eventType; }
	public int   getUserId ()     { return     userId; }
	public int   getCategory ()   { return   category; }
	public int   getPriority ()   { return   priority; }
	public float getCpu ()        { return        cpu; }
	public float getRam ()        { return        ram; }
	public float getDisk ()       { return       disk; }
	public int   getConstraint () { return constraint; }
}

