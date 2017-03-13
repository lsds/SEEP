package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.RangeUtil;

public class OpFailureCtrl implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(OpFailureCtrl.class);
	private int opId;
	private long lw;
	private BitSet acks;
	private BitSet alives;
	private String rsAcks;
	private String rsAlives;
	private boolean useBitSet = false;

	public OpFailureCtrl() {}
	
	public OpFailureCtrl(int opId, long lw, Set<Long> acks, Set<Long> alives)
	{
		this.opId = opId;
		this.lw = lw;
		if (useBitSet)
		{
			this.acks = toBitSet(lw, acks);
			this.alives = toBitSet(lw, alives);
			this.rsAcks = null;
			this.rsAlives = null;
		}
		else
		{
			this.acks = null;
			this.alives = null; 
			this.rsAcks = RangeUtil.toRangeSetStr(acks);
			this.rsAlives = RangeUtil.toRangeSetStr(alives);

		}
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
		if (useBitSet)
		{
			return fromBitSet(lw, acks);
		}
		else
		{
			return RangeUtil.parseRangeSet(rsAcks);
		}
	}

	public void setAcks(Set<Long> newAcks) {
		if (useBitSet)
		{
			this.acks = toBitSet(lw, newAcks);
		}
		else
		{
			this.rsAcks = RangeUtil.toRangeSetStr(newAcks);
		}
	}

	public Set<Long> getAlives() {
		if (useBitSet)
		{
			return fromBitSet(lw, alives);
		}
		else
		{
			return RangeUtil.parseRangeSet(rsAlives);
		}
	}

	public void setAlives(Set<Long> newAlives) {
		if (useBitSet)
		{
			this.alives = toBitSet(lw, newAlives);
		}
		else
		{
			this.rsAlives = RangeUtil.toRangeSetStr(newAlives);
		}
	}

	public FailureCtrl getFailureCtrl() 
	{
		if (useBitSet)
		{
			return new FailureCtrl(lw, fromBitSet(lw, acks), fromBitSet(lw, alives));
		}
		else
		{
			return new FailureCtrl(lw, RangeUtil.parseRangeSet(rsAcks), RangeUtil.parseRangeSet(rsAlives));
		}
	}
	
	private BitSet toBitSet(long low, Set<Long> ids)
	{
		if (ids == null || ids.isEmpty()) { return null; }
		BitSet bits = new BitSet();
		for (Long id : ids)
		{
			long offset = id - low;
			if (offset > Integer.MAX_VALUE || offset < 0) { throw new RuntimeException("Logic error"); }
			bits.set((int)offset);
		}
		//logger.trace("Converted "+low+","+ids+" to bitset "+bits);
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
		//logger.trace("Converted bitset "+low+","+bits+" to ids "+ids);
		return ids;
	}
}
