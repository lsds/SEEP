package uk.ac.imperial.lsds.seep.comm.serialization;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Iterator;

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

		logger.trace("Converted range set: "+ rs);
		return result;
	}
	
	public static Set<Long> parseRangeSet(String rsStr)
	{
		logger.trace("Parsing serialized range set: "+ rsStr);
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

		if (range.startsWith("[") && range.endsWith(")")) { return Range.closedOpen(lower, upper).canonical(DiscreteDomain.longs()); } 
		else if (range.startsWith("[") && range.endsWith("]")) { return Range.closed(lower, upper).canonical(DiscreteDomain.longs()); }
		else if (range.startsWith("(") && range.endsWith("]")) { return Range.openClosed(lower, upper).canonical(DiscreteDomain.longs()); }
		else {return Range.open(lower, upper).canonical(DiscreteDomain.longs());} 
	}

	public static RangeSet<Long> toRangeSet(TreeSet<Long> constraints)
	{ 
		RangeSet<Long> result = TreeRangeSet.create();
		if (constraints == null || constraints.isEmpty()) { return result; }
		
		Iterator<Long> iter = constraints.iterator();
		Long rangeStart = null;
		Long rangeEnd = null;
		while(iter.hasNext())
		{
			Long next = iter.next();
			if (rangeStart == null)
			{
				rangeStart = next;
				rangeEnd = next;
				if (!iter.hasNext())
				{
					result.add(Range.closed(rangeStart, rangeEnd).canonical(DiscreteDomain.longs()));
					break;
				}
			}
			else if (next == rangeEnd + 1)
			{
				rangeEnd++;
				if (!iter.hasNext())
				{
					result.add(Range.closed(rangeStart, rangeEnd).canonical(DiscreteDomain.longs()));
					break;
				}
			}
			else
			{
				result.add(Range.closed(rangeStart, rangeEnd).canonical(DiscreteDomain.longs()));
				rangeStart = next;
				rangeEnd = next;
				if (!iter.hasNext())
				{
					result.add(Range.closed(rangeStart, rangeEnd).canonical(DiscreteDomain.longs()));
					break;
				}
			}
		}
		return result;
	}
}
