package uk.ac.imperial.lsds.seep.comm.serialization.messages;

public class Timestamp implements Comparable
{

	private long ts;
	private int query = 0;

	public Timestamp() { this(0,0); }
	public Timestamp(long ts) { this(ts,0); }
	public Timestamp(long ts, int query) 
	{ 
		this.ts = ts;
		this.query = query;
	}


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
	
	@Override
	public int hashCode()
	{
		throw new RuntimeException("TODO");
	}

	@Override
	public String toString()
	{
		return ""+ts+"."+query;
	}
}
