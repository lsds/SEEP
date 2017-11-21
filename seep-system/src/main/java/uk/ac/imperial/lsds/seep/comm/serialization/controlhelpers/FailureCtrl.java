package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.RangeUtil;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Timestamp;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TimestampMap;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TimestampsMap;

public class FailureCtrl {

	//private volatile long lw;
	//private final Set<Long> acks;	//Change to linked set
	//private final Set<Long> alives;
	
	//Could alternatively make these timestamp maps?
	//private final Set<Timestamp> lws;
	//private final Set<Timestamp> acks;
	//private final Set<Timestamp> alives;
	private final static Logger logger = LoggerFactory.getLogger(FailureCtrl.class);

	private final TimestampMap lws;
	private final TimestampsMap acks;
	private final TimestampsMap alives;
	
	private final Object lock = new Object(){};
	
	public FailureCtrl()
	{
		//this(-1, new HashSet<Long>(), new HashSet<Long>());
		//this(new HashSet<Timestamp>(), new HashSet<Timestamp>(), new HashSet<Timestamp>());
		this.lws = new TimestampMap();
		this.acks = new TimestampsMap();
		this.alives = new TimestampsMap();
	}
	
	//public FailureCtrl(long lw, Set<Long> acks, Set<Long> alives)
	/*
	private FailureCtrl(Set<Timestamp> lws, Set<Timestamp> acks, Set<Timestamp> alives)
	{
		//this.lw = lw;
		//this.acks = (acks == null ? new HashSet<Long>() : acks);
		//this.alives = (alives == null ? new HashSet<Long>() : alives);
		//this.lws = (lws == null? new HashSet<Timestamp>() : lws);
		//this.acks = (acks == null ? new HashSet<Timestamp>() : acks);
		//this.alives = (alives == null ? new HashSet<Timestamp>() : alives);
		this.lws = new TimestampMap(lws);
		this.acks = new TimestampsMap(acks);
		this.alives = new TimestampsMap(alives);

	}
	*/
	private FailureCtrl(TimestampMap lws, TimestampsMap acks, TimestampsMap alives)
	{
		//this.lw = lw;
		//this.acks = (acks == null ? new HashSet<Long>() : acks);
		//this.alives = (alives == null ? new HashSet<Long>() : alives);
		//this.lws = (lws == null? new HashSet<Timestamp>() : lws);
		//this.acks = (acks == null ? new HashSet<Timestamp>() : acks);
		//this.alives = (alives == null ? new HashSet<Timestamp>() : alives);
		this.lws = new TimestampMap(lws);
		this.acks = new TimestampsMap(acks);
		this.alives = new TimestampsMap(alives);
	}
	
	public FailureCtrl(FailureCtrl other)
	{
		this(other, false);
		/*
		this.lw = other.lw;
		this.acks = new HashSet<>(other.acks());
		this.alives = new HashSet<>(other.alives());
		this.lws = new HashSet<>(other.lws());
		this.acks = new HashSet<>(other.acks());
		this.alives = new HashSet<>(other.alives());
		*/
	}

	public FailureCtrl(FailureCtrl other, boolean ignoreOtherAlives)
	{
		/*
		this.lws = new HashSet<>(other.lws());
		this.acks = new HashSet<>(other.acks());
		if (!ignoreOtherAlives)  { this.alives = new HashSet<>(other.alives()); }
		else { this.alives = new HashSet<Timestamp>(); }
		*/
		this.lws = new TimestampMap(other.lws());
		this.acks = new TimestampsMap(other.acks());
		this.alives = ignoreOtherAlives ? new TimestampsMap() : new TimestampsMap(other.alives());

	}
	
	public FailureCtrl(String fctrl)
	{
		/*
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
		*/
		logger.error("Parsing failure ctrl: "+fctrl);
		if (fctrl.length() != 3) { throw new RuntimeException("Logic error: "+fctrl); }
		
		String[] splits = fctrl.split(":");
		lws = "-".equals(splits[0]) ? new TimestampMap() : TimestampMap.parse(splits[0]);
		acks = "-".equals(splits[1]) ? new TimestampsMap() : TimestampsMap.parse(splits[1]);
		alives = "-".equals(splits[2]) ? new TimestampsMap() : TimestampsMap.parse(splits[2]);
	}
	
	public FailureCtrl atomicCopy()
	{
		synchronized(lock)
		{
			return new FailureCtrl(lws, acks, alives);
		}
	}
	
	public String toString()
	{
		synchronized(lock)
		{
			//return lw + ":" + joinLongs(acks) + ":" + joinLongs(alives);
			/*
			String ackStr = RangeUtil.toRangeSetStr(acks);
			String aliveStr = RangeUtil.toRangeSetStr(alives);
			return lw + ":" + (ackStr == null ? "" : ackStr) + ":" + (aliveStr == null ? "" : aliveStr);
			*/
			//throw new RuntimeException("TODO: Come up with new fctrl format.");
			String lwsStr = lws.convertToString();
			String ackStr = acks.convertToString();
			String aliveStr = alives.convertToString();
			String str = (lwsStr.isEmpty() ? "-": lwsStr) + ":" +
					(ackStr.isEmpty() ? "-": ackStr) + ":" +
					(aliveStr.isEmpty() ? "-": aliveStr);
			logger.error("Converted to string: "+str);
			return str;
		}
	}
	/*
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
	*/
	/*
	private Set<Timestamp> lwsRaw() { 
		//synchronized(lock) { return new HashSet<>(lws); }
		synchronized(lock) { return lws.toSet(); }
		//throw new RuntimeException("TODO don't expose sets?");
	}
	
	private Set<Timestamp> acksRaw() { 
		//synchronized(lock) { return new HashSet<>(acks); }
		synchronized(lock) { return acks.toSet(); }
		//throw new RuntimeException("TODO don't expose sets?");
	}

	private Set<Timestamp> alivesRaw() { 
		//synchronized(lock) { return new HashSet<>(alives); }
		synchronized(lock) { return alives.toSet(); }
		//throw new RuntimeException("TODO don't expose sets?");
	}*/
	
	public TimestampMap lws() {
		synchronized(lock) { return new TimestampMap(lws); }
	}
	
	public  TimestampsMap acks() {
		synchronized(lock) { return new TimestampsMap(acks); }
	}
	
	public TimestampsMap alives() {
		synchronized(lock) { return new TimestampsMap(alives); }
	}
	
	/*
	public boolean updateOld(FailureCtrl other)
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
	*/
	
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
		synchronized(lock)
		{
			if (!alives.isEmpty())
			{
				throw new RuntimeException("Logic error, only for sink?");
			}
			
			if (!lws.covers(ts) && !acks.contains(ts))
			{
				acks.add(ts);
				acks.compact(lws);
				//More efficient to merge to acks.compact(lws, ts)?
			}
		}
	}
	
	public boolean update(FailureCtrl other)
	{
		return update(other, false, false);
	}
	
	public boolean update(FailureCtrl other, boolean ignoreOtherAlives, boolean ignoreAlivesChanged)
	{
		synchronized(lock)
		{
			//This is all sooo inefficient!
			FailureCtrl otherCopy = other.atomicCopy();
			return update(otherCopy.lws, other.acks, other.alives, ignoreOtherAlives, ignoreAlivesChanged);
		}
	}
	
	private boolean update(TimestampMap newLws, TimestampsMap newAcks, 
			TimestampsMap newAlives, boolean ignoreOtherAlives, boolean ignoreAlivesChanged)
	{	
		synchronized(lock)
		{
			/*
			 * Strategy: essentially, want a method that doesn't *assume* the different sets are from different
			 * queries, but will perform efficiently if they are?
			 * 
			 * For newLw in newLws
			 * 		get from other based on query (no guarantee of being unique!).
			 * 		Could have a timestamps map and a timestamp map!
			 * lws.coveringMerge(newLw) Essentially removes all older.
			 */
			boolean changed = lws.coveringMerge(newLws);
			/*
			 * 
			 * Then just combine acks (don't need timestamp map?)
			 * Then for ack in acks
			 *       if lws.covers(ack)
			 *       	acks.remove(ack);
			 *       	todo if !newAcks.contains(nxtAck) change = true ?! Bug?
			 */
			
			changed = acks.coveringMerge(lws, newAcks) || changed;
			/*
			 * Then for lw in lws
			 * 		sqnTs = lw.sameQueryNext
			 * 		if acks.contains sqnTs
			 * 			lws.replace(lw, sqnTs)// or lws.remove(lw), lws.add(sqnTs)
			 * 			acks.remove(lw);
			 */
			
			changed = acks.compact(lws) || changed;
			/*
			 * Now Alives!
			 * Firstly just merge the alives, remembering the original size
			 * 		if lws.covers(alive) or acks.contains(alive) // this.isAcked(alive)?
			 * 			alives.remove(alive)
			 * 				if newAlives != null or !newalives contains alive ;; changed (again is this right)?
			 * 
			 * finally return whether changed.
			 */
			if (!ignoreOtherAlives) { alives.addAll(newAlives); }
			changed = (alives.coveringRemove(lws, acks) && !ignoreAlivesChanged) || changed;
		
			//if !changed then assert old covers new
			return changed;
		}
	}
	/*
	private boolean update(Set<Timestamp> newLws, Set<Timestamp> newAcks, Set<Timestamp> newAlives)
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
					if (! newAcks.contains(nxtAck)) { changed = true; } //This is wrong?! What if both old and new contained it?
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
		
		throw new RuntimeException("TODO: How to handle multiple queries?");	
	}
	*/
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
		synchronized(lock)
		{
			/*
			if (newAlive <= lw || acks.contains(newAlive) || alives.contains(newAlive))
			{
				return false;
			}
			else
			{
				alives.add(newAlive);
				return true;
			}
			
			*/
			if (lws.covers(newAlive) || acks.contains(newAlive) || alives.contains(newAlive)) { return false; }
			else
			{
				alives.add(newAlive);
				return true;
			}
		}	
	}
	
	/*
	public void updateAlives(Set<Timestamp> newAlives)
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
		*
		//throw new RuntimeException("TODO: How to handle multiple queries?");
		//TODO: Eventually change interface of above to take a timestampsMap instead of a set.
		updateAlives(new TimestampsMap(newAlives));
	}
	*/
	
	public void updateAlives(TimestampsMap newAlives)
	{
		synchronized(lock)
		{
			alives.addAll(newAlives);
			alives.coveringRemove(lws, acks);
		}
	}
	
	/*
	public void setAlives(Set<Timestamp> newAlives)
	{
		synchronized(lock)
		{
			alives.clear();
			updateAlives(newAlives);
		}	
	}*/
	
	public void setAlives(TimestampsMap newAlives)
	{
		synchronized(lock)
		{
			alives.clear();
			alives.addAll(newAlives);
		}	
	}
	
	
	public boolean isAcked(Timestamp ts)
	{
		
		synchronized(lock)
		{
			//return ts <= lw || acks.contains(ts);
			return !lws.covers(ts) && !acks.contains(ts);
		}
	}

	public boolean isAlive(Timestamp ts)
	{
		synchronized(lock)
		{
			return alives.contains(ts) && !isAcked(ts);
		}
	}

	public long unacked(Timestamp ts)
	{

		synchronized(lock)
		{
			/*
			if (ts <= lw) { return 0; }
			long unacked = ts - lw;
			for (Long ack : acks) 
			{ 
				if (ack <= ts && ack > lw) 
				{ unacked--; }
			}
			if (unacked != (ts - lw - acks.size())) { throw new RuntimeException("Logic error: unacked="+unacked+",ts="+ts+",ls="+lw+",acks.size="+acks.size()+",acks="+acks); }
			return unacked; 
			*/
			if (lws.covers(ts)) { return 0; }
			else 
			{
				long unacked = lws.uncoveredSizeInclusive(ts) - acks.coveredSizeInclusive(ts); 
				logger.error("Computed unacked: "+unacked);
				return unacked; 
			}
		}	
	}

	public TimestampsMap uncovered(TimestampsMap alivesToCheck)
	{
		synchronized(lock)
		{
			return alivesToCheck.uncovered(lws, acks, alives);
		}
	}
	
	public boolean coversAcks(FailureCtrl other)
	{
		synchronized(lock)
		{	
			//TODO: Seriously inefficient!
			FailureCtrl otherCopy = other.atomicCopy();
			return lws.covers(otherCopy.lws) && otherCopy.acks.isCovered(lws, acks);
			//throw new RuntimeException("TODO: See OutOfOrderInputQueue");
		}
	}
	
	public boolean emptyAlives()
	{
		synchronized(lock) { return alives.isEmpty(); }
	}

}
