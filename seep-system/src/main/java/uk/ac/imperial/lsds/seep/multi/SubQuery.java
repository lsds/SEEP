package uk.ac.imperial.lsds.seep.multi;

import java.util.Set;


public class SubQuery {
	
	private int id;
	
	private Set<MicroOperator> microOperators;
	
	private MicroOperator mostUpstreamMicroOperator;
	private MicroOperator mostDownstreamMicroOperator;

	private MultiOperator parent;
	
	private SubQuery upstreamSubQuery = null;
	private SubQuery downstreamSubQuery = null;
	
	private IQueryBuffer inputBuffer;
	
	private WindowDefinition windowDef;
	
	public SubQuery(int id, Set<MicroOperator> microOperators, WindowDefinition windowDef) {
		this.id = id;
		this.microOperators = microOperators;
		this.windowDef = windowDef;
		
		for (MicroOperator o : this.microOperators) {
			if (o.isMostUpstream())
				mostUpstreamMicroOperator = o;
			if (o.isMostDownstream())
				mostDownstreamMicroOperator = o;
		}
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isMostUpstream() {
		return (this.upstreamSubQuery == null);
	}

	public boolean isMostDownstream() {
		return (this.downstreamSubQuery == null);
	}

	public MicroOperator getMostUpstreamMicroOperator() {
		return this.mostUpstreamMicroOperator;
	}
	
	public MicroOperator getMostDownstreamMicroOperator() {
		return this.mostDownstreamMicroOperator;
	}

	public IQueryBuffer getInputBuffer() {
		return inputBuffer;
	}

	public MultiOperator getParent() {
		return parent;
	}

	public void setParent(MultiOperator parent) {
		this.parent = parent;
	}
	
}
