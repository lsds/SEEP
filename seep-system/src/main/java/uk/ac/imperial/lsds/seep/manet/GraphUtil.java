package uk.ac.imperial.lsds.seep.manet;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;
import java.net.InetAddress;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphUtil {
	private final static Logger log = LoggerFactory.getLogger(GraphUtil.class);
	public final static Double INFINITE_DISTANCE = new Double(1000000);
	public final static Double SUB_INFINITE_DISTANCE = new Double(999999);
	public final static Double MAX_BANDWIDTH = new Double(Double.MAX_VALUE);

	public static void logTopology(Map graph) {
		
		//TODO: Fix up generics
		if (graph == null)
		{
			log.info("Topology:<null>");
			return;
		}

		log.info("Topology:");
		Iterator srcIter = graph.keySet().iterator();
		while (srcIter.hasNext())
		{
			Comparable srcId = (Comparable)srcIter.next();
			String neighbourList = "Src=" + srcId + " : Neighbours=";
			Map links = (Map)graph.get(srcId);
			Iterator destIter = links.keySet().iterator();
			while (destIter.hasNext())
			{
				Comparable destId = (Comparable)destIter.next();
				neighbourList += " " + destId + "(" + links.get(destId) + ")";
			}
			log.info(neighbourList);
		}
	}

	public static void logShortestPaths(Map costs)
	{
		Iterator iter = costs.keySet().iterator();

		while (iter.hasNext())
		{
			Comparable dest = (Comparable)iter.next();
			Double cost = (Double)costs.get(dest);
			Object[] destCost = { dest, cost };
			log.info(MessageFormat.format("d({0,number,integer})={1,number,integer}", destCost));
		}
	}

	public static void testPrintShortestPaths()
	{
		Map test = new HashMap();
		test.put(new Integer(1), new Integer(5));
		logShortestPaths(test);
	}


	public static Map shortestPaths(Comparable source, Map graph)
	{
		log.debug("Computing shortest path from "+source+ " given graph: "+graph);
		Djikstra djikstra = new Djikstra(graph);
		djikstra.execute(source);
		return djikstra.getShortestDistances();
	}

	public static Comparable nextHop(Comparable source, Comparable dest, Map graph)
	{
		Djikstra djikstra = new Djikstra(graph);
		//TODO Should perhaps have another execute(source, dest) method
		djikstra.execute(source);
		return djikstra.getBestNextHop(source, dest);
	}


	private static abstract class AbstractDjikstra
	{
		/** Stores the neighbours map. */
		protected final Map map;

		public AbstractDjikstra(Map map) { this.map = map; }
		protected final Set settledNodes = new HashSet();

		protected boolean isSettled(Comparable id) { return settledNodes.contains(id); }

		protected final Map predecessors = new HashMap();
		protected void setPredecessor(Comparable id, Comparable predecessor) { predecessors.put(id, predecessor); }
		protected Comparable getPredecessor(Comparable id)
		{
			Object predecessor = predecessors.get(id);
			return (predecessor == null) ? null : (Comparable)predecessor;
		}

		//Unfortunately no PriorityQueue in Java 1.4 so just
		//brute force it for now. Might try and fix up Jist
		//to run under a newer version of Java at some point.
		protected final Set unsettledNodes = new HashSet();

		protected Set getNeighbours(Comparable id)
		{
			return ((Map)map.get(id)).keySet();
		}

		public abstract void execute(Comparable source);
		protected abstract Comparable getBestNextHop(Comparable source, Comparable dest);
		protected abstract void relaxNeighbours(Comparable u);
	}


	private interface NodeId extends Comparable {}
	
	/*
	public static class IntegerNodeId implements NodeId
	{
		private final Integer id;
		public IntegerNodeId(Integer id) { this.id = id; }
		@Override
		public int compareTo(Object other) { return id.compareTo(((IntegerNodeId) other).id); }
		@Override
		public boolean equals(Object other) { return id.equals(((IntegerNodeId)other).id); }
		@Override
		public int hashCode() { return id.hashCode(); }
	}
	*/
	public static class InetAddressNodeId implements NodeId
	{
		private final InetAddress id;
		public InetAddressNodeId(InetAddress id) { this.id = id; }
		@Override
		public int compareTo(Object other) { return this.toString().compareTo(((InetAddressNodeId)other).toString()); }
		@Override
		public boolean equals(Object other) { return id.equals(((InetAddressNodeId)other).id); }
		@Override
		public int hashCode() { return id.hashCode(); }
		public String toString() { return id.getHostAddress(); }
	}
	
	
	private static class Djikstra extends AbstractDjikstra
	{

		public Djikstra(Map map) { super(map); }

		private final Map shortestDistances = new HashMap();
		private void setShortestDistance(Comparable id, Double distance) 
		{ 
			shortestDistances.put(id, distance); 
		}
		
		private Double getShortestDistance(Comparable id)
		{
			Object d = shortestDistances.get(id);
			return (d == null) ? INFINITE_DISTANCE : (Double)d;
		}

		private final Map getShortestDistances() { return Collections.unmodifiableMap(shortestDistances); }

		//Presumes execute has been called for source
		@Override
		protected Comparable getBestNextHop(Comparable source, Comparable dest)
		{
			if (getShortestDistance(dest).doubleValue() >= SUB_INFINITE_DISTANCE.doubleValue())
			{
				//Infinite distance => no path
				return null;
			}

			Comparable best = dest;
			Comparable predecessor = getPredecessor(best);
			while (predecessor != null && !(predecessor.equals(source)))
			{
				best = predecessor;
				predecessor = getPredecessor(best);
			}
			return best;
		}

		private final Comparator comp = new Comparator()
		{
			@Override
			public int compare(Object o1, Object o2)
			{
				Comparable left = (Comparable)o1;
				Comparable right = (Comparable)o2;

				double shortestDistanceLeft = getShortestDistance(left).doubleValue();
				double shortestDistanceRight = getShortestDistance(right).doubleValue();

				if (shortestDistanceLeft > shortestDistanceRight) { return 1; }
				else if (shortestDistanceLeft < shortestDistanceRight) { return -1; }
				else { assert left.compareTo(right) != 0; return left.compareTo(right); }
			}
		};

		private Comparable extractMin()
		{
			Object min = Collections.min(unsettledNodes, comp);
			unsettledNodes.remove(min);
			return (Comparable)min;
		}

		private void init(Comparable source)
		{
			settledNodes.clear();
			unsettledNodes.clear();
			shortestDistances.clear();
			predecessors.clear();

			initShortestDistances();
			setShortestDistance(source, new Double(0));
			unsettledNodes.add(source);
		}

		private void initShortestDistances()
		{
			Iterator iter = map.keySet().iterator();
			while (iter.hasNext())
			{
				setShortestDistance((Comparable)iter.next(),INFINITE_DISTANCE);
			}
		}

		@Override
		public void execute(Comparable source)
		{
			init(source);
			Comparable u;

			while (!unsettledNodes.isEmpty())
			{
				u = extractMin();
				//if (u == destination) break;
				settledNodes.add(u);
				relaxNeighbours(u);
			}

		}

		@Override
		protected void relaxNeighbours(Comparable u)
		{
			Iterator neighbourIter = getNeighbours(u).iterator();
			while (neighbourIter.hasNext())
			{
				Comparable v = (Comparable)neighbourIter.next();
				if (isSettled(v)) continue;

				double shortDist = getShortestDistance(u).doubleValue() + getDistance(u,v).doubleValue();
				if (shortDist < getShortestDistance(v).doubleValue())
				{
					setShortestDistance(v, new Double(shortDist));
					setPredecessor(v,u);
					unsettledNodes.add(v);
				}
			}
		}

		private Double getDistance(Comparable u, Comparable v)
		{
			assert ((Map)map.get(u)).keySet().contains(v);

			return ((Map)map.get(u)).keySet().contains(v) ? (Double)((Map)map.get(u)).get(v) : INFINITE_DISTANCE /*Should never happen*/;
		}
	}

	public static Map widestPaths(Comparable source, Map graph)
	{
		DjikstraBandwidth djikstra = new DjikstraBandwidth(graph);
		djikstra.execute(source);
		return djikstra.getWidestPaths();
	}

	public static Comparable nextHopBandwidth(Comparable source, Comparable dest, Map graph)
	{
		DjikstraBandwidth djikstra = new DjikstraBandwidth(graph);
		//TODO Should perhaps have another execute(source, dest) method
		djikstra.execute(source);
		return djikstra.getBestNextHop(source, dest);
	}

	private static class DjikstraBandwidth extends AbstractDjikstra
	{
		public DjikstraBandwidth(Map map) { super(map); }

		private final Map getWidestPaths() { return Collections.unmodifiableMap(widestPaths); }
		private final Map widestPaths = new HashMap();
		private void setWidestPath(Comparable id, Double width) { widestPaths.put(id, width); }
		private Double getWidestPath(Comparable id)
		{
			Object w = widestPaths.get(id);
			return (w == null) ? new Double(0) : (Double)w;
		}

		private Double getWidth(Comparable u, Comparable v)
		{
			assert ((Map)map.get(u)).keySet().contains(v);

			return ((Map)map.get(u)).keySet().contains(v) ? (Double)((Map)map.get(u)).get(v) : new Double(0) /*Should never happen*/;
		}

		private void init(Comparable source)
		{
			settledNodes.clear();
			unsettledNodes.clear();
			widestPaths.clear();
			predecessors.clear();

			initWidestPaths();
			setWidestPath(source, new Double(Double.MAX_VALUE));
			unsettledNodes.add(source);
		}

		private void initWidestPaths()
		{
			Iterator iter = map.keySet().iterator();
			while (iter.hasNext())
			{
				setWidestPath((Comparable)iter.next(),new Double(0));
			}
		}

		@Override
		public void execute(Comparable source)
		{
			init(source);
			Comparable u;

			while (!unsettledNodes.isEmpty())
			{
				u = extractMax();
				//if (u == destination) break;
				settledNodes.add(u);
				relaxNeighbours(u);
			}

		}

		@Override
		protected void relaxNeighbours(Comparable u)
		{
			Iterator neighbourIter = getNeighbours(u).iterator();
			while (neighbourIter.hasNext())
			{
				Comparable v = (Comparable)neighbourIter.next();
				if (isSettled(v)) continue;

				double widestPath = Math.min(getWidestPath(u).doubleValue(), getWidth(u,v).doubleValue());
				if (widestPath > getWidestPath(v).doubleValue())
				{
					setWidestPath(v, new Double(widestPath));
					setPredecessor(v,u);
					unsettledNodes.add(v);
				}
			}
		}

		private Comparable extractMax()
		{
			Object max = Collections.max(unsettledNodes, comp);
			unsettledNodes.remove(max);
			return (Comparable)max;
		}

		private final Comparator comp = new Comparator()
		{
			@Override
			public int compare(Object o1, Object o2)
			{
				Comparable left = (Comparable)o1;
				Comparable right = (Comparable)o2;

				double widestPathLeft = getWidestPath(left).doubleValue();
				double widestPathRight = getWidestPath(right).doubleValue();

				if (widestPathLeft < widestPathRight) { return -1; }
				else if (widestPathLeft > widestPathRight) { return 1; }
				else { assert left.compareTo(right) != 0; return left.compareTo(right); }
			}
		};

		//Presumes execute has been called for source
		@Override
		protected Comparable getBestNextHop(Comparable source, Comparable dest)
		{
			if (getWidestPath(dest).intValue() == 0)
			{
				//Zero width => no path
				return null;
			}

			Comparable best = dest;
			Comparable predecessor = getPredecessor(best);
			while (predecessor != null && !(predecessor.equals(source)))
			{
				best = predecessor;
				predecessor = getPredecessor(best);
			}
			return best;
		}

	}

}
