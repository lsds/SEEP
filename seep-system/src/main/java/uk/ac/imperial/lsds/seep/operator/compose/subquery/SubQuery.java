package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBufferHandler;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryTaskSubmitter;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;

public class SubQuery {
	
	private int id;
	
	private Set<IMicroOperatorConnectable> microOperators;
	private Set<IMicroOperatorConnectable> mostUpstreamMicroOperators;
	private IMicroOperatorConnectable mostDownstreamMicroOperator;

	private Map<Integer, IWindowDefinition>  windowDefinitions;
	
	private SubQueryBufferHandler bufferHandler;

	private SubQueryTaskSubmitter taskSubmitter;

	private SubQueryConnectable parent;
	
	private SubQuery(Set<IMicroOperatorConnectable> microOperators, int id, Map<Integer, IWindowDefinition>  inputWindowDefinitions) {
		this.id = id;
		this.windowDefinitions = inputWindowDefinitions;
		this.microOperators = microOperators;
		
		this.mostUpstreamMicroOperators = new HashSet<>();
		
		for (IMicroOperatorConnectable microOperatorConnectable : this.microOperators) {
			if (microOperatorConnectable.isMostLocalUpstream())
				mostUpstreamMicroOperators.add(microOperatorConnectable);
			if (microOperatorConnectable.isMostLocalDownstream())
				mostDownstreamMicroOperator = microOperatorConnectable;
		}
		
		this.bufferHandler = new SubQueryBufferHandler(parent);
		this.taskSubmitter = new SubQueryTaskSubmitter(this);
	}
	
	public int getId() {
		return id;
	}

	public static SubQuery newSubQuery (
			Set<IMicroOperatorConnectable> microOperators, int opId, Map<Integer, IWindowDefinition>  inputWindowDefinitions) {
		return new SubQuery(microOperators, opId, inputWindowDefinitions);
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

	public Map<Integer, IWindowDefinition> getWindowDefinitions() {
		return windowDefinitions;
	}
	
	public void updateWindowsOnInputBuffer(SubQueryBuffer b, MultiOpTuple tuple) {
		this.bufferHandler.updateCurrentWindows(b, tuple);
	}
	
	public SubQueryConnectable getParent() {
		return parent;
	}

	public void setParent(SubQueryConnectable parent) {
		this.parent = parent;
	}
	
	public void dispatchTask(ISubQueryTaskCallable task) {
		this.taskSubmitter.dispatch(task);
	}

}
