package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;

public class SubQuery {
	
	private int id;
	
	private ISubQueryConnectable parent;
	private Set<IMicroOperatorConnectable> microOperators;
	private Set<IMicroOperatorConnectable> mostUpstreamMicroOperators;
	private IMicroOperatorConnectable mostDownstreamMicroOperator;

	private Map<Integer, IWindowDefinition>  inputWindowDefinitions;
	
	private SubQuery(Set<IMicroOperatorConnectable> microOperators, int id, Map<Integer, IWindowDefinition>  inputWindowDefinitions) {
		this.id = id;
		this.inputWindowDefinitions = inputWindowDefinitions;
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
			Set<IMicroOperatorConnectable> microOperators, int opId, Map<Integer, IWindowDefinition>  inputWindowDefinitions) {
		return new SubQuery(microOperators, opId, inputWindowDefinitions);
	}


	public void setParentSubQueryConnectable(
			ISubQueryConnectable subQueryConnectable) {
		this.parent = subQueryConnectable;
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
	
}
