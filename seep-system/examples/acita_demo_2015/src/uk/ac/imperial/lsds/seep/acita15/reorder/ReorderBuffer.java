package uk.ac.imperial.lsds.seep.acita15.reorder;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ReorderBuffer {

	private TreeMap buffer = new TreeMap();
	private long nextId = 0;
	
	public void add(Long key, Object message)
	{
		if (buffer.containsKey(key)) { throw new RuntimeException("Logic error."); }
		buffer.put(key, message);
	}
	
	/* Presumes there is at least one entry */
	public List flush()
	{
		List result = new LinkedList();
		
		Iterator iter = buffer.entrySet().iterator();

		Long currentKey = null;
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry)iter.next();
			Long nextKey = (Long)entry.getKey();
			if (currentKey == null && nextKey.longValue() != nextId) { break; }
			if (currentKey == null || nextKey.longValue() == currentKey.longValue() + 1)
			{
				currentKey = nextKey;
				result.add(entry.getValue());
				nextId++;
				iter.remove();
			}
		}
		
		return result;
	}
	
	public List flushKeys()
	{
		List result = new LinkedList();
		
		Iterator iter = buffer.entrySet().iterator();

		Long currentKey = null;
		while (iter.hasNext())
		{
			Map.Entry entry = (Map.Entry)iter.next();
			Long nextKey = (Long)entry.getKey();
			if (currentKey == null && nextKey.longValue() != nextId) { break; }
			if (currentKey == null || nextKey.longValue() == currentKey.longValue() + 1)
			{
				currentKey = nextKey;
				result.add(entry.getKey());
				nextId++;
				iter.remove();
			}
		}
		
		return result;
	}
	
	public int size() { return buffer.size(); }
	public String toString() { return buffer.keySet().toString(); }
	public String gaps()
	{
		return "TODO";
	}
	
	public Set keySet() { return buffer.keySet(); }
}
