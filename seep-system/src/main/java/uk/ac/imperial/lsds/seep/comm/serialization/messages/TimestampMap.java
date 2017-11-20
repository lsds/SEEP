package uk.ac.imperial.lsds.seep.comm.serialization.messages;

import java.util.Set;

public class TimestampMap {

	//private Map<Integer, Timestamp> tsMap;
	private TimestampsMap tsMap;

	private TimestampMap(TimestampsMap tsMap) 
	{ 
		tsMap.assertSizes(1);
		this.tsMap = tsMap; 
	}
	
	public TimestampMap(Set<Timestamp> tsSet) 
	{  
		tsMap = new TimestampsMap(tsSet);
		tsMap.assertSizes(1);
	}
	
	public TimestampMap(TimestampMap other) 
	{
		other.tsMap.assertSizes(1);
		tsMap = new TimestampsMap(other.tsMap);
	} 
	
	public String convertToString()	{ return tsMap.convertToString(); }
	
	public static TimestampMap parse(String tsMapStr) 
	{
		if (tsMapStr.contains(":")) { throw new RuntimeException("Logic error: invalid: "+tsMapStr); }
		return new TimestampMap(TimestampsMap.parse(tsMapStr));  
	}
	
	public Set<Timestamp> toSet() { return tsMap.toSet(); }
	
	public boolean coveringMerge(TimestampMap other) 
	{ 
		return tsMap.maxMerge(other);
	}
	
	public boolean covers(Timestamp ts)
	{ 
		return tsMap.maxCovers(ts);
	}
	
	public boolean covers(TimestampMap other)
	{ 
		return tsMap.maxCovers(other); 
	}

	public long uncoveredSizeInclusive(Timestamp ts) {
		//Return the number of timestamps less less than or equal to ts but greater than the corresponding local timestamp.
		//For now query specific?
		return tsMap.maxInterval(ts);
	}
	
	TimestampsMap asTimestampsMap() { return tsMap; }
}
