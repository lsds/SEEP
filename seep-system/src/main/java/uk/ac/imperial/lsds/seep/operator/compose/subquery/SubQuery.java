package uk.ac.imperial.lsds.seep.operator.compose.subquery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorConnectable;
import uk.ac.imperial.lsds.seep.operator.compose.multi.SubQueryBuffer;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowDefinition;

public class SubQuery {
	
	private int id;
	
	private ISubQueryConnectable parent;
	private Set<IMicroOperatorConnectable> microOperators;
	private Set<IMicroOperatorConnectable> mostUpstreamMicroOperators;
	private IMicroOperatorConnectable mostDownstreamMicroOperator;

	private Map<Integer, SubQueryBuffer> inputQueues;
	private Map<Integer, SubQueryBuffer> outputQueues;
	
	private Map<Integer, IWindowDefinition>  inputWindowDefinitions;
	
	private SubQuery(Set<IMicroOperatorConnectable> microOperators, int id, Map<Integer, IWindowDefinition>  inputWindowDefinitions) {
		this.id = id;
		this.inputQueues = new HashMap<>();
		this.outputQueues = new HashMap<>();
		this.inputWindowDefinitions = inputWindowDefinitions;
		this.microOperators = microOperators;
		
		for (IMicroOperatorConnectable microOperatorConnectable : this.microOperators)
			if (microOperatorConnectable.isMostLocalUpstream())
				mostUpstreamMicroOperators.add(microOperatorConnectable);
			else if (microOperatorConnectable.isMostLocalDownstream())
				mostDownstreamMicroOperator = microOperatorConnectable;
	}
	
	public int getId() {
		return id;
	}

	public void registerInputQueue(Integer upstreamOpId,
			SubQueryBuffer queue) {
		this.inputQueues.put(upstreamOpId, queue);
	}

	public void registerOutputQueue(Integer downstreamOpId,
			SubQueryBuffer queue) {
		this.outputQueues.put(downstreamOpId, queue);
	}

	public void pushData(List<DataTuple> tuples, int streamID) {
		for (DataTuple tuple : tuples)
			pushData(tuple, streamID);
	}

	public void pushData(DataTuple tuple, int streamID) {
		this.inputQueues.get(streamID).add(tuple);
	}
	public static SubQuery newSubQuery (
			Set<IMicroOperatorConnectable> microOperators, int opId, Map<Integer, IWindowDefinition>  inputWindowDefinitions) {
		return new SubQuery(microOperators, opId, inputWindowDefinitions);
	}


	public void setParentSubQueryConnectable(
			ISubQueryConnectable subQueryConnectable) {
		this.parent = subQueryConnectable;
	}

	public void pushDataToAllStreams(DataTuple data) {
		for (Integer streamID : this.inputQueues.keySet())
			pushData(data, streamID);
	};

	public Set<IMicroOperatorConnectable> getMicroOperators() {
		return this.microOperators;
	}
	
	public Set<IMicroOperatorConnectable> getMostUpstreamMicroOperators() {
		return this.mostUpstreamMicroOperators;
	}
	
	public IMicroOperatorConnectable getMostDownstreamMicroOperators() {
		return this.mostDownstreamMicroOperator;
	}
	
}
