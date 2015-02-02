package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class OpFailureCtrl implements Serializable {
	private int opId;
	private long lw;
	private Set<Long> acks;
	private Set<Long> alives;

	public OpFailureCtrl() {}
	
	public OpFailureCtrl(int opId, long lw, Set<Long> acks, Set<Long> alives)
	{
		this.opId = opId;
		this.lw = lw;
		this.acks = acks;
		this.alives = alives;
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
		return acks;
	}

	public void setAcks(Set<Long> acks) {
		this.acks = acks;
	}

	public Set<Long> getAlives() {
		return alives;
	}

	public void setAlives(Set<Long> alives) {
		this.alives = alives;
	}

	public FailureCtrl getFailureCtrl() 
	{
		return new FailureCtrl(lw, new HashSet<>(acks), new HashSet<>(alives));
	}
	
}
