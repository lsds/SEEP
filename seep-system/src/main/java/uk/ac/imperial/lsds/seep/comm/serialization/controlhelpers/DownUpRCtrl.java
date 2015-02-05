package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.Set;

public class DownUpRCtrl {

	private int opId;
	private double weight;
	private Set<Long> unmatched;
	
	public DownUpRCtrl() {}
	
	public DownUpRCtrl(int opId, double weight, Set<Long> unmatched)
	{
		this.opId = opId;
		this.weight = weight;
		this.unmatched = unmatched;
	}

	public int getOpId() {
		return opId;
	}

	public void setOpId(int opId) {
		this.opId = opId;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public Set<Long> getUnmatched() {
		return unmatched;
	}

	public void setUnmatched(Set<Long> unmatched) {
		this.unmatched = unmatched;
	}
	
	public String toString() { return "downOp="+opId+",weight="+weight+",unmatched="+unmatched; }
}
