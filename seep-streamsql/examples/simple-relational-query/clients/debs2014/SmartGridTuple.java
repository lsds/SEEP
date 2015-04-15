public class SmartGridTuple {
	
	private long    counter;
	private long  timestamp;
	private float     value;
	private int    property;
	private int        plug;
	private int   household;
	private int       house;
	
	public SmartGridTuple () {
		/* Initialise all attributes */
		reset ();
	}
	
	private void reset () {
		
		this.counter   = 0L;
		this.timestamp = 0L;
		this.value     = 0;
		this.property  = 0;
		this.plug      = 0;
		this.household = 0;
		this.house     = 0;
	}
	
	public static void parse (String line, SmartGridTuple t) {
		String [] s = line.split(",");
		
		t.counter   = Long.parseLong  (s[ 0]); 
		t.timestamp = Long.parseLong  (s[ 1]) - ; 
		t.value     = Float.parseFloat(s[ 2]);
		t.property  = Integer.parseInt(s[ 3]);
		t.plug      = Integer.parseInt(s[ 4]);
		t.household = Integer.parseInt(s[ 5]);
		t.house     = Integer.parseInt(s[ 6]);
	}
	
	public long  getCounter()   { return   counter; }
	public long  getTimestamp() { return timestamp; }
	public float getValue()     { return     value; }
	public int   getProperty()  { return  property; }
	public int   getPlug()      { return      plug; }
	public int   getHousehold() { return household; }
	public int   getHouse()     { return     house; }
}

