package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class DownUpRCtrl {

	private int opId;
	private double weight;
	private RangeSet<Long> unmatched;
	
	public DownUpRCtrl() {}
	
	public DownUpRCtrl(int opId, double weight, RangeSet<Long> unmatched)
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
		if (unmatched == null || unmatched.isEmpty())
		{
			return null;
		}
		Set<Long> results = new HashSet<Long>();
		for (Range<Long> range: unmatched.asRanges())
		{
			results.addAll(ContiguousSet.create(range, DiscreteDomain.longs()));
		}
		return results;
	}

	/*
	public void setUnmatched(Set<Long> unmatched) {
		this.unmatched = unmatched;
	}
	*/
	
	public String toString() { return "downOp="+opId+",weight="+weight+",unmatched="+unmatched; }
}
