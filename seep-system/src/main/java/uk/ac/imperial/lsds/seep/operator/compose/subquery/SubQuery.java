package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;

public class SubQuery {
	
	private int id;
	
	private Set<IMicroOperatorConnectable> microOperators;
	private Set<IMicroOperatorConnectable> mostUpstreamMicroOperators;
	private IMicroOperatorConnectable mostDownstreamMicroOperator;

	private SubQueryConnectable parent;
	
	private SubQuery(Set<IMicroOperatorConnectable> microOperators, int id) {
		this.id = id;
		this.microOperators = microOperators;
		
		this.mostUpstreamMicroOperators = new HashSet<>();
		
		for (IMicroOperatorConnectable microOperatorConnectable : this.microOperators) {
			if (microOperatorConnectable.isMostLocalUpstream())
				mostUpstreamMicroOperators.add(microOperatorConnectable);
			if (microOperatorConnectable.isMostLocalDownstream())
				mostDownstreamMicroOperator = microOperatorConnectable;
		}
	}
	
	public int getId() {
		return id;
	}

	public static SubQuery newSubQuery (
			Set<IMicroOperatorConnectable> microOperators, int opId) {
		return new SubQuery(microOperators, opId);
	}

	public Set<IMicroOperatorConnectable> getMicroOperators() {
		return this.microOperators;
	}
	
	public Set<IMicroOperatorConnectable> getMostUpstreamMicroOperators() {
		return this.mostUpstreamMicroOperators;
	}
	
	public IMicroOperatorConnectable getMostDownstreamMicroOperator() {
		return this.mostDownstreamMicroOperator;
	}

	public SubQueryConnectable getParent() {
		return parent;
	}

	public void setParent(SubQueryConnectable parent) {
		this.parent = parent;
	}
}
