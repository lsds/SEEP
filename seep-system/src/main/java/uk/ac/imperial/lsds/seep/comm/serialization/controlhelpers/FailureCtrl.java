package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.RangeUtil;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Timestamp;

public class FailureCtrl {

	private volatile long lw;
	private final Set<Long> acks;	//Change to linked set
	private final Set<Long> alives;
	private final Object lock = new Object(){};
	
	public FailureCtrl()
	{
		this(-1, new HashSet<Long>(), new HashSet<Long>());
	}
	
	//public FailureCtrl(long lw, Set<Long> acks, Set<Long> alives)
	private FailureCtrl(long lw, Set<Long> acks, Set<Long> alives)
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

	public FailureCtrl(FailureCtrl other, boolean keepAlives)
	{
		this.lw = other.lw;
		this.acks = new HashSet<>(other.acks());
		if (keepAlives)  { this.alives = new HashSet<>(other.alives()); }
		else { this.alives = new HashSet<Long>(); }
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
			//return lw + ":" + joinLongs(acks) + ":" + joinLongs(alives);
			String ackStr = RangeUtil.toRangeSetStr(acks);
			String aliveStr = RangeUtil.toRangeSetStr(alives);
			return lw + ":" + (ackStr == null ? "" : ackStr) + ":" + (aliveStr == null ? "" : aliveStr);
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
	
	//public long lw() { return lw;	}
	public Timestamp lw() { 
		/*
		return lw;	
		*/
		throw new RuntimeException("TODO: How to handle multiple queries? Check if queries match?");
	}
	
	//public Set<Long> acks() { 
	public Set<Timestamp> acks() { 
		/*
		synchronized(lock) { return new HashSet<>(acks); }
		*/
		throw new RuntimeException("TODO: How to handle multiple queries? Check if queries match?");
	}

	
	//public Set<Long> alives() { 
	public Set<Timestamp> alives() { 
		/*
		synchronized(lock) { return new HashSet<>(alives); }
		*/
		throw new RuntimeException("TODO: How to handle multiple queries? Check if queries match?");
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
	
	public void ack(Timestamp ts)
	{
		/*
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
		*/
		throw new RuntimeException("TODO: How to handle multiple queries?");	
	}
	
	public boolean update(Timestamp newLw, Set<Timestamp> newAcks, Set<Timestamp> newAlives)
	{
		/*
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
		*/
		throw new RuntimeException("TODO: How to handle multiple queries?");	
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
	
	public boolean updateAlives(Timestamp newAlive)
	{
		/*
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
		*/
		throw new RuntimeException("TODO: How to handle multiple queries?");	
	}
	
	public void updateAlives(Set<Timestamp> newAlives)
	{
		/*
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
		*/
		throw new RuntimeException("TODO: How to handle multiple queries?");	
	}
	
	public void setAlives(Set<Timestamp> newAlives)
	{
		/*
		synchronized(lock)
		{
			alives.clear();
			updateAlives(newAlives);
		}	
		*/
		throw new RuntimeException("TODO: How to handle multiple queries? Check if queries match?");
	}
	
	
	public boolean isAcked(Timestamp ts)
	{
		/*
		synchronized(lock)
		{
			return ts <= lw || acks.contains(ts);
		}
		*/
		throw new RuntimeException("TODO: How to handle multiple queries? Check if queries match?");
	}

	public boolean isAlive(Timestamp ts)
	{
		/*
		synchronized(lock)
		{
			return alives.contains(ts) && !isAcked(ts);
		}
		*/
		throw new RuntimeException("TODO: How to handle multiple queries? Check if queries match?");
	}

	public long unacked(Timestamp ts)
	{
		/*
		synchronized(lock)
		{
			if (ts <= lw) { return 0; }
			long unacked = ts - lw;
			for (Long ack : acks) 
			{ 
				if (ack <= ts && ack > lw) 
				{ unacked--; }
			}
			if (unacked != (ts - lw - acks.size())) { throw new RuntimeException("Logic error: unacked="+unacked+",ts="+ts+",ls="+lw+",acks.size="+acks.size()+",acks="+acks); }
			return unacked; 
		}
		*/
		throw new RuntimeException("TODO: How to handle multiple queries?");	
	}

	public boolean coversAcks(FailureCtrl other)
	{
		throw new RuntimeException("TODO: See OutOfOrderInputQueue");	
	}
}
