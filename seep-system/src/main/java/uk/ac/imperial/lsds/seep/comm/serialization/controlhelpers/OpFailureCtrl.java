package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class OpFailureCtrl implements Serializable {
	private int opId;
	private long lw;
	private BitSet acks;
	private BitSet alives;
	/*
	private Set<Long> acks;
	private Set<Long> alives;
	*/
	public OpFailureCtrl() {}
	
	public OpFailureCtrl(int opId, long lw, Set<Long> acks, Set<Long> alives)
	{
		this.opId = opId;
		this.lw = lw;
		this.acks = toBitSet(lw, acks);
		this.alives = toBitSet(lw, alives);
	}
	
	public int getOpId() {
		return opId;
	}
	public void setOpId(int opId) {
		this.opId = opId;
	}

	public long getLw() {
		return lw;
	}

	public void setLw(long lw) {
		this.lw = lw;
	}
	
	public Set<Long> getAcks() {
		return fromBitSet(lw, acks);
	}

	public void setAcks(Set<Long> acks) {
		this.acks = toBitSet(lw, acks);
	}

	public Set<Long> getAlives() {
		return fromBitSet(lw, alives);
	}

	public void setAlives(Set<Long> alives) {
		this.alives = toBitSet(lw, alives);
	}

	public FailureCtrl getFailureCtrl() 
	{
		return new FailureCtrl(lw, fromBitSet(lw, acks), fromBitSet(lw, alives));
	}
	
	private BitSet toBitSet(long low, Set<Long> ids)
	{
		if (ids == null || ids.isEmpty()) { return null; }
		BitSet bits = new BitSet();
		for (Long id : ids)
		{
			long offset = id - low;
			if (offset > Integer.MAX_VALUE) { throw new RuntimeException("Logic error"); }
			bits.set((int)offset);
		}
		return bits;
	}
	
	private Set<Long> fromBitSet(long low, BitSet bits)
	{
		Set<Long> ids = new HashSet<>();
		if (bits == null || bits.isEmpty()) { return ids; }
		for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i+1))
		{
			ids.add(low + i);
		}
		return ids;
	}
	
}
