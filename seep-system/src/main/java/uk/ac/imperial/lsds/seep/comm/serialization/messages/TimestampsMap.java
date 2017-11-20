package uk.ac.imperial.lsds.seep.comm.serialization.messages;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import uk.ac.imperial.lsds.seep.comm.serialization.RangeUtil;

public class TimestampsMap implements Iterable<Timestamp> {
	private static final String kv2kvSep = ";";
	private static final String k2vSep = ".";
	
	Map<Integer, TreeSet<Timestamp>> tsMap;
	
	public TimestampsMap()
	{
		this.tsMap = new HashMap<>();
	}
	
	public TimestampsMap(Set<Timestamp> tsSet)
	{
		tsMap = new HashMap<Integer, TreeSet<Timestamp>>();
		for (Timestamp ts : tsSet)
		{
			Integer key = ts.getKey();
			if (!tsMap.containsKey(key)) { tsMap.put(key, new TreeSet<Timestamp>()); }
			tsMap.get(key).add(ts);
		}
	}
	

	public TimestampsMap(TimestampsMap other) 
	{ 
		this.tsMap = new HashMap<>();
		for (Integer key : other.tsMap.keySet())
		{
			if (other.tsMap.get(key).isEmpty()) { throw new RuntimeException("Logic error?"); }
			tsMap.put(key,  new TreeSet<Timestamp>());
			tsMap.get(key).addAll(other.tsMap.get(key));
		}
	} 
	
	private Map<Integer, Set<Long>> toCompactMap()
	{
		Map<Integer,Set<Long>> compactMap = new HashMap<>();
		//Assume exactly two levels of nesting for now?
		for (Integer tsKey : tsMap.keySet())
		{
			if(!compactMap.containsKey(tsKey)) { compactMap.put(tsKey, new HashSet<Long>()); }
			for (Timestamp ts : tsMap.get(tsKey))
			{
				compactMap.get(tsKey).add(ts.toLongArray()[1]);
			}
		}
		return compactMap;
	}
	
	public String convertToString() { 
		//Assume exactly two levels of nesting for now.		
		//Format key1.val1;key1.val2;...;keyn.valn
		Map<Integer, Set<Long>> compact = toCompactMap();
		String result = "";
		
		Iterator<Integer> keyIter = compact.keySet().iterator();
		
		while (keyIter.hasNext())
		{
			Integer key = keyIter.next();
			String valResultStr = RangeUtil.toRangeSetStr(compact.get(key));
			if (!valResultStr.isEmpty()) 
			{ 
				result += key + k2vSep + valResultStr;
				if (keyIter.hasNext()) { result += kv2kvSep ;}
			}
		}
		return result;
	}
	
	public static TimestampsMap parse(String tsMapStr) { 
		if (tsMapStr.contains(":")) { throw new RuntimeException("Logic error: invalid: "+tsMapStr); }
		TimestampsMap result = new TimestampsMap();
		String[] keysVals = tsMapStr.split(kv2kvSep);
		for (int i = 0; i < keysVals.length; i++)
		{
			
			String[] keyVal = keysVals[i].split(k2vSep);
			if (keyVal.length != 2) { throw new RuntimeException("Logic error"); }
			Integer key = Integer.parseInt(keyVal[0]);
			Set<Long> vals = RangeUtil.parseRangeSet(keyVal[1]);
			TreeSet<Timestamp> orderedKeyVals = new TreeSet<>();
			for (Long val : vals) { orderedKeyVals.add(new Timestamp(key, val)); }
			result.tsMap.put(key, orderedKeyVals);
		}
		return result;
	}
	public Set<Timestamp> toSet() 
	{ 
		Set<Timestamp> result = new HashSet<>();
		for (Integer key : tsMap.keySet()) 
		{
			result.addAll(tsMap.get(key));
		}
		return result; 
	}
	
	public boolean contains(Timestamp ts)  
	{ 
		Integer key = ts.getKey();
		return tsMap.containsKey(key) && tsMap.get(key).contains(ts);  
	}
	
	public boolean compact(TimestampMap lws) 
	{ 
		boolean changed = false;
		TimestampsMap rawLws = lws.asTimestampsMap();
		for (Integer k : rawLws.tsMap.keySet())
		{
			if (!tsMap.containsKey(k)) { continue; }
			
			Timestamp lw = rawLws.tsMap.get(k).first();
			Timestamp next = lw.nextSameKey();
			
			while(tsMap.get(k).contains(next))
			{
				tsMap.get(k).remove(next);
				//N.B. Assuming here this will reflect back on lws (i.e. rawLws is the underlying map).
				rawLws.tsMap.get(k).remove(lw);
				rawLws.tsMap.get(k).add(next);

				changed = true;
				
				lw = next;
				next = next.nextSameKey();
			}
		}
		
		return changed;
	}
	
	public boolean add(Timestamp ts) 
	{ 
		Integer key = ts.getKey();
		if (!tsMap.containsKey(key)) { tsMap.put(key, new TreeSet<Timestamp>()); }
		boolean isNew = !tsMap.get(key).contains(ts);
		tsMap.get(key).add(ts);
		return isNew;
	}
	
	public boolean addAll(TimestampsMap other) 
	{ 
		boolean someNew = false;
		for (Integer key : other.tsMap.keySet())
		{
			for (Timestamp ts : other.tsMap.get(key)) 
			{ someNew = this.add(ts) || someNew; }
		}
		return someNew;
	}
	
	public void remove(Timestamp ts)
	{
		Integer key = ts.getKey();
		if (tsMap.containsKey(key)) 
		{ 
			tsMap.get(key).remove(ts); 
			if (tsMap.get(key).isEmpty()) { tsMap.remove(key); }
		}
	}
	
	
	public boolean isEmpty() 
	{ 
		return tsMap.isEmpty(); 
	}
	
	public boolean isCovered(TimestampMap lws, TimestampsMap other) 
	{ 
		TimestampsMap rawLws = lws.asTimestampsMap();
		for (Integer k : tsMap.keySet())
		{
			assertNotEmpty(k);
			if (!rawLws.tsMap.containsKey(k)) { return false; }
			Timestamp lw = rawLws.tsMap.get(k).first();
			for (Timestamp uncovered : tsMap.get(k).tailSet(lw, false))
			{
				if (!other.tsMap.containsKey(k) || !other.tsMap.get(k).contains(uncovered)) { return false; }
			}
		}
		return true;
	}

	public boolean coveringMerge(TimestampMap lws, TimestampsMap newAcks) {
		boolean changed = false;
		TimestampsMap rawLws = lws.asTimestampsMap();
		changed = addAll(newAcks) || changed;
		changed = coveringRemove(lws, null) || changed;
		return changed;
	}
	
	boolean maxMerge(TimestampMap lws)
	{
		boolean changed = false;
		TimestampsMap rawLws = lws.asTimestampsMap();
		for (Integer k : tsMap.keySet())
		{
			if (rawLws.tsMap.containsKey(k))
			{
				Timestamp currentMax = tsMap.get(k).last();
				Timestamp lw = rawLws.tsMap.get(k).last();
				if (lw.compareTo(currentMax) > 0)
				{
					tsMap.get(k).clear();
					tsMap.get(k).add(lw);
					changed = true;
				}
			}
		}
		
		for (Integer k : rawLws.tsMap.keySet())
		{
			if (!tsMap.containsKey(k))
			{
				tsMap.put(k,  new TreeSet<Timestamp>());
				tsMap.get(k).add(rawLws.tsMap.get(k).last());
				changed = true;
			}
		}
		
		return changed;
	}
	
	boolean maxCovers(Timestamp ts)
	{
		Integer k = ts.getKey();
		return tsMap.containsKey(k) && tsMap.get(k).last().compareTo(ts) > 0;
	}

	boolean maxCovers(TimestampMap lws)
	{
		TimestampsMap rawLws = lws.asTimestampsMap();
		if (tsMap.size() < rawLws.tsMap.size()) { return false; }
		
		for (Integer k : rawLws.tsMap.keySet())
		{
			if (!tsMap.containsKey(k)) { return false; }
			if (tsMap.get(k).last().compareTo(rawLws.tsMap.get(k).last()) < 0) { return false; }
		}
		
		return true;
	}
	
	public boolean coveringRemove(TimestampMap lws, TimestampsMap acks) {
		return coveringRemoveFromSortedMap(lws, acks, null);
		/*
		boolean changed = false;
		TimestampsMap rawLws = lws.asTimestampsMap();
		Iterator<Entry<Integer, TreeSet<Timestamp>>> iter = tsMap.entrySet().iterator();
		
		while (iter.hasNext())
		{
			Integer k = iter.next().getKey();
			if (rawLws.tsMap.containsKey(k))
			{
				Timestamp lw = rawLws.tsMap.get(k).first();
				int pre = tsMap.get(k).size();
				tsMap.get(k).headSet(lw, true).clear();
				changed |= pre > tsMap.get(k).size();
			}
			
			if (tsMap.get(k).isEmpty()) { iter.remove(); }
			else if(acks != null && acks.tsMap.containsKey(k))
			{
				changed = tsMap.get(k).removeAll(acks.tsMap.get(k)) || changed; 
			}
		}
		
		return changed;
		*/
	}

	public boolean coveringRemoveFromSortedMap(TimestampMap lws, TimestampsMap acks, SortedMap<Timestamp, ? extends Object> sorted)
	{
		boolean changed = false;
		TimestampsMap rawLws = lws.asTimestampsMap();
		Iterator<Entry<Integer, TreeSet<Timestamp>>> iter = tsMap.entrySet().iterator();
		
		while (iter.hasNext())
		{
			Integer k = iter.next().getKey();
			if (rawLws.tsMap.containsKey(k))
			{
				Timestamp lw = rawLws.tsMap.get(k).first();
				int pre = tsMap.get(k).size();
				Set<Timestamp> toRemove = tsMap.get(k).headSet(lw, true);
				if (sorted != null) { sorted.keySet().removeAll(toRemove); }
				toRemove.clear();
				changed |= pre > tsMap.get(k).size();
			}
			
			if (tsMap.get(k).isEmpty()) { iter.remove(); }
			else if(acks != null && acks.tsMap.containsKey(k))
			{ 
				changed = tsMap.get(k).removeAll(acks.tsMap.get(k)) || changed;
				if (sorted != null) { sorted.keySet().removeAll(acks.tsMap.get(k)); }
			}
		}
		
		return changed;
		
	}
	
	public boolean covers(Timestamp ts)
	{
		Integer key = ts.getKey();
		return !tsMap.containsKey(key) || !tsMap.get(key).contains(ts);
	}
	
	public TimestampsMap uncovered(TimestampMap lws, TimestampsMap acks, TimestampsMap alives)
	{
		TimestampsMap result = new TimestampsMap();
		for (Integer k : tsMap.keySet())
		{
			for (Timestamp ts : tsMap.get(k))
			{
				if (!lws.covers(ts) && !acks.covers(ts) && !alives.covers(ts))
				{ result.add(ts); }	
			}
		}
		return result;
	}
	
	
	public long coveredSizeInclusive(Timestamp ts) {
		//Should return the number of timestamps in the map greater less than or equal to the ts
		Integer key = ts.getKey();
		if (tsMap.containsKey(key) && tsMap.get(key).contains(ts)) 
		{ throw new RuntimeException("Logic error?" + ts + "," +tsMap); }
		
		//Why prioritize one query over the other? Could force max to be multiple of the number of
		//queries instead.
		long total = 0;
		for (Integer k : tsMap.keySet())
		{
			total += tsMap.get(k).headSet(ts, true).size();
		}
		
		return total;
	}
	
	long maxInterval(Timestamp ts)
	{
		long total = 0;
		for (Integer k : tsMap.keySet())
		{
			Timestamp max = tsMap.get(k).last();
			long interval = max.interval(ts); 
			if (interval > 0) { total += interval; }
		}
		if (!tsMap.containsKey(ts.getKey())) { total += ts.interval(); }
		return total;
	}
	
	public void clear() { tsMap.clear(); }
	
	void assertSizes(int size) 
	{ 
		for (Integer key : tsMap.keySet()) 
		{ 
			if (tsMap.get(key).size() != size) 
			{ throw new RuntimeException("Logic error : assertion failed for size="+size+", tsMap="+tsMap); }
		}
	}
	
	public int size()
	{
		int total = 0;
		for (Integer k : tsMap.keySet())
		{
			total += tsMap.get(k).size();
		}
		return total;
	}
	
	private void assertNotEmpty(Integer k)
	{
		if (tsMap.get(k).isEmpty()) { throw new RuntimeException("Logic error, "+k+" is empty, tsMap="+tsMap); }
	}
	
	@Override
	public Iterator<Timestamp> iterator()
	{
		return new TMIterator();
	}
	
	private class TMIterator implements Iterator<Timestamp>
	{
		private Iterator<Integer> keyIter = tsMap.keySet().iterator();
		private Iterator<Timestamp> valsIter = null;
		
		@Override
		public boolean hasNext() {
			return keyIter.hasNext() || (valsIter != null && valsIter.hasNext()); 
		}

		@Override
		public Timestamp next() {
			if (valsIter != null && valsIter.hasNext()) { return valsIter.next(); }
			else if (keyIter.hasNext()) 
			{
				Integer nextKey = keyIter.next();
				valsIter = tsMap.get(nextKey).iterator();
				return valsIter.next();
			}
			else { throw new NoSuchElementException("No more elements in "+tsMap); }
		}
	}
}
