package uk.ac.imperial.lsds.seep.comm.serialization.messages;

public class Timestamp implements Comparable
{
	public final static String SEP = ".";
	private long ts;
	private int query = 0;

	public Timestamp() { this(0,-1); }
	public Timestamp(long ts) { this(0, ts); }
	public Timestamp(int query, long ts) 
	{ 
		this.ts = ts;
		this.query = query;
	}

	Integer getKey() { return query; }

	/* Not sure this is the semantics you actually want! 

	In some cases you should never be comparing batches for different queries.
	*/
	
	@Override
	public int compareTo(Object o)
	{
		Timestamp other = (Timestamp)o;
		if (ts < other.ts || (ts == other.ts && query < other.query))
		{
			return -1;
		} 
		else if (ts > other.ts || (ts == other.ts && query > other.query))
		{
			return 1;
		}
		else { return 0; }
	}


	public int compareTsOnly(Timestamp other)
	{
		if (query != other.query) { throw new RuntimeException("Incomparable queries."); }
		else { return this.compareTo(other); }
	}

	public int compareQuery(Timestamp other)
	{
		if (query == other.query) { return 0; }
		else if (query < other.query) { return -1; }
		else { return 1; }
	}

	@Override
	public boolean equals(Object o)
	{
		Timestamp other = (Timestamp)o;
		if (ts == other.ts && query == other.query) { return true; }
		else { return false; }
	}

	public boolean equalsTsOnly(Timestamp other)
	{
		if (query != other.query) { throw new RuntimeException("Incomparable queries."); }
		return (ts == other.ts);
	}

	public boolean equalsQuery(Timestamp other)
	{
		return query == other.query;
	} 

	public static Timestamp max(Timestamp t1, Timestamp t2)
	{
		throw new RuntimeException("TODO");
	}
	
	public long interval(Timestamp other)
	{
		if (query != other.query) { throw new RuntimeException("Logic error?"); }
		//start exclusive, end inclusive
		return other.ts - ts;
	}
	
	public long interval() { return ts +1; }
	
	@Override
	public int hashCode()
	{
		throw new RuntimeException("TODO");
	}

	public String[] toStrings() { return new String[] { ""+query, ""+ts } ; }
	
	@Override
	public String toString()
	{
		return ""+ts+"."+query;
	}
	
	//Bit weird putting this here, but want to avoid exposing the underlying
	//ts's type for the moment
	public int index(int divisor, int modulo)
	{
		if (ts < 0 || (ts / divisor) > Integer.MAX_VALUE) { throw new RuntimeException("Logic error: ts="+ts+",div="+divisor+",mod="+modulo); }
		return (int)(ts / divisor) % modulo;
	}
	
	//This is for removeOlderInclusive. Should return the strictly next timestamp
	public Timestamp next()
	{
		return new Timestamp(query+1, ts);
	}
	
	public Timestamp nextSameKey()
	{
		return new Timestamp(query, ts+1);
	}
	
	Long[] toLongArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
