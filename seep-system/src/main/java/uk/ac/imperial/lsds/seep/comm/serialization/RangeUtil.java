package uk.ac.imperial.lsds.seep.comm.serialization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.collect.BoundType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangeUtil
{
	private static final Logger logger = LoggerFactory.getLogger(RangeUtil.class);

	public static String toRangeSetStr(Set<Long> ids)
	{
		RangeSet<Long> rsIds = toRangeSet(ids);
		if (rsIds == null || rsIds.isEmpty()) { return null; }

		String rsIdsStr = RangeUtil.convertToString(rsIds);
		if (!parseRangeSet(rsIdsStr).equals(ids)) { throw new RuntimeException("Logic error: rsIdsStr="+rsIdsStr+",ids="+ids+",rsIds="+rsIds); }
		return rsIdsStr;
	}

	public static RangeSet<Long> toRangeSet(Set<Long> ids)
	{

		if (ids == null || ids.isEmpty()) { return null; }
		RangeSet<Long> rsIds = TreeRangeSet.create();
		for (Long id : ids)
		{
			rsIds.add(Range.singleton(id).canonical(DiscreteDomain.longs()));
		} 	
		return rsIds;
	}

	public static String convertToString(RangeSet<Long> rs)
	{
		logger.trace("Converting range set: "+ rs);
		if (rs == null || rs.isEmpty()) { return null; }
		Set<Range<Long>> asRanges = rs.asRanges();
		List<String> rangeStrings = new ArrayList<>();
		for (Range<Long> range : asRanges)
		{
			rangeStrings.add(convertToString(range));
		}
		Joiner joiner = Joiner.on(",");
		String result = joiner.join(rangeStrings);

		logger.debug("Converted range set: "+ rs);
		return result;
	}
	
	public static Set<Long> parseRangeSet(String rsStr)
	{
		logger.debug("Parsing serialized range set: "+ rsStr);
		if (rsStr == null || rsStr.isEmpty()) { return null; }
		Set<Long> result = new HashSet<>();
		Splitter splitter = Splitter.on(",").trimResults().omitEmptyStrings();
		for (String range : splitter.split(rsStr))
		{
			result.addAll(ContiguousSet.create(parseRange(range), DiscreteDomain.longs()));
		}
		logger.trace("Parsed serialized range set: "+ result);
		return result;
	}

	private static String convertToString(Range<Long> range)
	{
		String lowerBoundType = range.lowerBoundType().equals(BoundType.CLOSED) ? "[" : "(";
		String upperBoundType = range.upperBoundType().equals(BoundType.CLOSED) ? "]" : ")";
		return lowerBoundType + range.lowerEndpoint()+":"+range.upperEndpoint()+ upperBoundType;
	}
	
	
	private static Range<Long> parseRange(String range)
	{
		Long lower = Long.parseLong(range.substring(1, range.indexOf(':')));
		Long upper = Long.parseLong(range.substring(range.indexOf(':')+1, range.length()-1));

		if (range.startsWith("[") && range.endsWith(")")) { return Range.closedOpen(lower, upper); } 
		else if (range.startsWith("[") && range.endsWith("]")) { return Range.closed(lower, upper); }
		else if (range.startsWith("(") && range.endsWith("]")) { return Range.openClosed(lower, upper); }
		else {return Range.open(lower, upper);} 
	}
}
