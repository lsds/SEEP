package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FailureCtrl {

	private volatile long lw;
	private final Set<Long> acks;	//Change to linked set
	private final Set<Long> alives;
	private final Object lock = new Object(){};
	
	public FailureCtrl()
	{
		this(-1, new HashSet<Long>(), new HashSet<Long>());
	}
	
	public FailureCtrl(long lw, Set<Long> acks, Set<Long> alives)
	{
		this.lw = lw;
		this.acks = (acks == null ? new HashSet<Long>() : acks);
		this.alives = (alives == null ? new HashSet<Long>() : alives);
	}
	
	public FailureCtrl(FailureCtrl other)
	{
		this.lw = other.lw;
		this.acks = new HashSet<>(other.acks());
		this.alives = new HashSet<>(other.alives());
	}
	
	public FailureCtrl(String fctrl)
	{
		String[] splits = fctrl.split(":");
		lw = Long.parseLong(splits[0]);
		if (splits.length == 1)
		{
			acks = new HashSet<Long>();
			alives = new HashSet<Long>();
			return;
		}
		if (splits.length == 2)
		{
			acks = parseLongs(splits[1]);
			alives = new HashSet<Long>();
			return;
		}
		acks = parseLongs(splits[1]);
		alives = parseLongs(splits[2]);
	}
	
	public String toString()
	{
		synchronized(lock)
		{
			return lw + ":" + joinLongs(acks) + ":" + joinLongs(alives);
		}
	}
	
	private String joinLongs(Set<Long> longs)
	{
		String longStr = "";
		Iterator iter = longs.iterator();
		while (iter.hasNext())
		{
			longStr += iter.next();
			if (iter.hasNext()) { longStr += ","; }
		}
		return longStr;
	}
	
	private Set<Long> parseLongs(String longStr)
	{
		Set<Long> longs = new HashSet<Long>();
		if ("".equals(longStr)) { return longs; }
		String longSplits[] = longStr.split(",");
		for (int i = 0; i < longSplits.length; i++)
		{
			longs.add(Long.parseLong(longSplits[i]));
		}
		return longs;
	}
	
	public long lw() { return lw;	}
	
	public Set<Long> acks() { 
		synchronized(lock) { return new HashSet<>(acks); }
	}
	public Set<Long> alives() { 
		synchronized(lock) { return new HashSet<>(alives); }
	}
	
	public boolean update(FailureCtrl other)
	{
		boolean changed = false;
		synchronized(lock)
		{
			if (other.lw() > lw)
			{
				lw = other.lw();
				changed = true;
			}
			long prevAcksSize = acks.size();
			acks.addAll(other.acks());
	
			Iterator<Long> iter = acks.iterator();
			while (iter.hasNext())
			{
				long nxtAck = iter.next();
				if (nxtAck <= lw) 
				{ 
					iter.remove();
					if (!other.acks().contains(nxtAck)) { changed = true; }
				}
			}
			
			while(acks.contains(lw+1))
			{
				changed = true;
				acks.remove(lw);
				lw++;
			}
			
			long prevAlivesSize = alives.size();
			alives.addAll(other.alives());
			iter = alives.iterator();
			while (iter.hasNext())
			{
				long nxtAlive = iter.next();
				if (nxtAlive <= lw || acks.contains(nxtAlive))
				{
					iter.remove();
					if (!other.alives().contains(nxtAlive)) { changed = true; }
				}
			}
			return changed || acks.size() != prevAcksSize || alives.size() != prevAlivesSize;
		}
	}
	
	public void ack(long ts)
	{
		synchronized(lock)
		{
			if (!alives.isEmpty()) 
			{ 
				throw new RuntimeException("Tmp: Logic error - only for sink."); 
			}
		
			if (ts > lw && !acks().contains(ts))
			{
				acks.add(ts);
				for (long i = lw + 1; acks.contains(i); i++)
				{
					lw++;
					acks.remove(lw);
				}
			}
		}
	}
	
	public boolean update(long newLw, Set<Long> newAcks, Set<Long> newAlives)
	{
		boolean changed = false;
		synchronized(lock)
		{
			if (newLw > lw) { changed = true; }
			lw = Math.max(lw, newLw);
			
			int prevAcksSize = acks.size();
			acks.addAll(newAcks);
			
			Iterator<Long> iter = acks.iterator();
			while (iter.hasNext())
			{
				long nxtAck = iter.next();
				if (nxtAck <= lw) 
				{ 
					iter.remove();
					if (! newAcks.contains(nxtAck)) { changed = true; }
				}
			}
			
			while(acks.contains(lw+1))
			{
				changed = true;
				acks.remove(lw);
				lw++;
			}
			
			int prevAlivesSize = alives.size();
			if (newAlives != null) {  
				alives.addAll(newAlives);
			}
			
			iter =  alives.iterator();
			while (iter.hasNext())
			{
				long nxtAlive = iter.next();
				if (nxtAlive <= lw || acks.contains(nxtAlive)) 
				{ 
					iter.remove(); 
					if (newAlives == null || !newAlives.contains(nxtAlive)) { changed = true; }
				}
			}
			
			return changed || prevAcksSize != acks.size() || prevAlivesSize != alives.size();
		}
	}
	
	/*
	public boolean merge(long otherLw, Set<Long> otherAcks)
	{
		boolean changed = false;
		synchronized(lock)
		{
			if (!alives.isEmpty()) { throw new RuntimeException("Logic error: merge only for sinks:"+alives); }
			if (otherLw > lw) { changed = true; }
			lw = Math.max(lw, otherLw);
			
			int prevAcksSize = acks.size();
			acks.addAll(otherAcks);
			
			Iterator<Long> iter = acks.iterator();
			while (iter.hasNext())
			{
				long nxtAck = iter.next();
				if (nxtAck <= lw) 
				{ 
					iter.remove();
					if (! otherAcks.contains(nxtAck)) { changed = true; }
				}
			}
			
			while(acks.contains(lw+1))
			{
				changed = true;
				acks.remove(lw);
				lw++;
			}
			
			return changed || prevAcksSize != acks.size();
		}
	}
	*/
	
	public boolean updateAlives(long newAlive)
	{
		synchronized(lock)
		{
			if (newAlive <= lw || acks.contains(newAlive) || alives.contains(newAlive))
			{
				return false;
			}
			else
			{
				alives.add(newAlive);
				return true;
			}
		}
	}
	
	public void updateAlives(Set<Long> newAlives)
	{
		synchronized(lock)
		{
			alives.addAll(newAlives);
			Iterator<Long> iter = alives.iterator();
			while (iter.hasNext())
			{
				long nxtAlive = iter.next();
				if (nxtAlive <= lw || acks.contains(nxtAlive))
				{
					iter.remove();
				}
			}
		}
	}
	
	public void setAlives(Set<Long> newAlives)
	{
		synchronized(lock)
		{
			alives.clear();
			updateAlives(newAlives);
		}	
	}
	
	
	public boolean isAcked(long ts)
	{
		synchronized(lock)
		{
			return ts <= lw || acks.contains(ts);
		}
	}
}
