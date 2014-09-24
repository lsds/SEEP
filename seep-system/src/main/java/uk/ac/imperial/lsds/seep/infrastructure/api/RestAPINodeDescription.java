package uk.ac.imperial.lsds.seep.infrastructure.api;

import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;

public class RestAPINodeDescription implements RestAPIRegistryEntry {

	private WorkerNodeDescription nodesDesc;
	
	public RestAPINodeDescription(WorkerNodeDescription nodesDesc) {
		this.nodesDesc = nodesDesc;
	}
	
	@Override
	public Object getAnswer() {
		return this.nodesDesc;
	}

}
