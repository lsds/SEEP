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
	
	public void update(FailureCtrl other)
	{
		synchronized(lock)
		{
			if (other.lw() > lw)
			{
				lw = other.lw();
			}
			acks.addAll(other.acks());
	
			Iterator<Long> iter = acks.iterator();
			while (iter.hasNext())
			{
				if (iter.next() <= lw) 
				{ 
					iter.remove();
				}
			}
			alives.addAll(other.alives());
			iter = alives.iterator();
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
				}
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
	
}
