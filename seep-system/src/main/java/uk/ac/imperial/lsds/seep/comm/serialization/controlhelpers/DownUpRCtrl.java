package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.RangeSet;

import uk.ac.imperial.lsds.seep.comm.serialization.RangeUtil;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Timestamp;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TimestampsMap;

public class DownUpRCtrl {

	private static final Logger logger = LoggerFactory.getLogger(DownUpRCtrl.class);
	private int opId;
	private double weight;
	private String unmatched;
	
	public DownUpRCtrl() {}
	
	public DownUpRCtrl(int opId, double weight, Set<Timestamp> unmatched)
	{
		this.opId = opId;
		this.weight = weight;
		//this.unmatched = RangeUtil.convertToString(unmatched);
		this.unmatched = new TimestampsMap(unmatched).convertToString();
	}
	/*
	public DownUpRCtrl(int opId, double weight, RangeSet<Long> unmatched)
	{
		this.opId = opId;
		this.weight = weight;
		this.unmatched = RangeUtil.convertToString(unmatched);
	}*/

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

	public Set<Timestamp> getUnmatched() {
		//return RangeUtil.parseRangeSet(unmatched);
		TimestampsMap unmatchedMap = TimestampsMap.parse(unmatched);
		if (unmatchedMap != null) { return unmatchedMap.toSet(); }
		else { return null; }
	}
	
	public void setUnmatched(String unmatched)
	{
		this.unmatched = unmatched;
	}

	public String toString() { return "downOp="+opId+",weight="+weight+",unmatched="+unmatched; }
}
