package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FailureCtrl {

	private volatile long lw;
	private final Set<Long> acks;	//Change to linked set
	private final Set<Long> alives;
	
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
	
	public FailureCtrl(String fctrl)
	{
		
	}
	
	public long lw() { return lw;	}
	
	public Set<Long> acks() { return acks; }
	public Set<Long> alives() { return alives; }
	
	public boolean update(FailureCtrl other)
	{
		boolean modified = false;
		if (other.lw > lw)
		{
			lw = other.lw;
			modified = true;
		}
		acks.addAll(other.acks());
		Iterator<Long> iter = acks.iterator();
		while (iter.hasNext())
		{
			if (iter.next() <= lw) 
			{ 
				iter.remove();
				modified = true;
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
				modified = true;
			}
		}
		
		return modified;
		throw new RuntimeException("TODO: thread safety");
	}
	
	public String toString() {}
	
}
