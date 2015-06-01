package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class DownUpRCtrl {

	private static final Logger logger = LoggerFactory.getLogger(DownUpRCtrl.class);
	private int opId;
	private double weight;
	//private RangeSet<Long> unmatched;
	private String unmatched;
	
	public DownUpRCtrl() {}
	
	public DownUpRCtrl(int opId, double weight, RangeSet<Long> unmatched)
	{
		this.opId = opId;
		this.weight = weight;
		this.unmatched = convertToString(unmatched);
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
		logger.debug("Getting unmatched: "+unmatched);
		if (unmatched == null || unmatched.isEmpty())
		{
			return null;
		}
		else
		{
			return parseRangeSet(unmatched);
		}
		/*
		Set<Long> results = new HashSet<Long>();
		for (Range<Long> range: unmatched.asRanges())
		{
			results.addAll(ContiguousSet.create(range, DiscreteDomain.longs()));
		}
		return results;
		*/
	}
	
	public void setUnmatched(String unmatched)
	{
		this.unmatched = unmatched;
	}

	private String convertToString(RangeSet<Long> unmatched)
	{
		if (unmatched == null || unmatched.isEmpty()) { return null; }
		Set<Range<Long>> asRanges = unmatched.asRanges();
		List<String> rangeStrings = new ArrayList<>();
		for (Range<Long> range : asRanges)
		{
			rangeStrings.add(convertToString(range));
		}
		Joiner joiner = Joiner.on(",");
		return joiner.join(rangeStrings);
	}
	
	private String convertToString(Range<Long> range)
	{
		return "[" + range.lowerEndpoint()+":"+range.upperEndpoint()+"]";
	}
	
	private Set<Long> parseRangeSet(String str)
	{
		logger.trace("Parsing unmatched: "+ str);
		if (str == null || str.isEmpty()) { return null; }
		Set<Long> result = new HashSet<>();
		Splitter splitter = Splitter.on(",").trimResults().omitEmptyStrings();
		for (String range : splitter.split(str))
		{
			result.addAll(ContiguousSet.create(parseRange(range), DiscreteDomain.longs()));
		}
		logger.trace("Parsed unmatched: "+ result);
		return result;
	}
	
	private Range<Long> parseRange(String range)
	{
		Long lower = Long.parseLong(range.substring(1, range.indexOf(':')));
		Long upper = Long.parseLong(range.substring(range.indexOf(':')+1, range.length()-1));
		return Range.closed(lower, upper);
	}
	
	/*
	public void setUnmatched(Set<Long> unmatched) {
		this.unmatched = unmatched;
	}
	*/
	
	public String toString() { return "downOp="+opId+",weight="+weight+",unmatched="+unmatched; }
}
