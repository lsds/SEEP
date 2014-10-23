package uk.ac.imperial.lsds.seep.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhysicalSeepQuery {

	private List<Operator> physicalOperators;
	private List<Operator> sources;
	private Operator sink;
	private Map<Operator, List<Operator>> instancesPerOriginalOp;
	
	private PhysicalSeepQuery(List<Operator> physicalOperators, List<Operator> pSources, Operator pSink,
			Map<Operator, List<Operator>> instancesPerOriginalOp) {
		this.physicalOperators = physicalOperators;
		this.sources = pSources;
		this.sink = pSink;
		this.instancesPerOriginalOp = instancesPerOriginalOp;
	}
	
	public static PhysicalSeepQuery buildPhysicalQueryFrom(Set<SeepQueryPhysicalOperator> physicalOperators, 
			Map<Operator, List<Operator>> instancesPerOriginalOp, LogicalSeepQuery lsq) {
		// create physical connections
		for(Operator o : physicalOperators) {
			// update all downstream connections -> this will update the downstream's upstreams
			for(DownstreamConnection dc : o.downstreamConnections()) {
				// this will replace a still logical connection with a physical one
				// note that we don't need to update connectionType, this info is already there
				o.connectTo(dc.getDownstreamOperator(), dc.getStreamId());
			}
		}
		List<Operator> pOps = new ArrayList<Operator>(physicalOperators);
		List<Operator> pSources = new ArrayList<>();
		Operator pSink = null;
		for(Operator o : lsq.getSources()) {
			// if this necessary?
			for(Operator po : pOps){
				if(po.getOperatorId() == o.getOperatorId()){
					pSources.add(po);
				}
				if(po.getOperatorId() == lsq.getSink().getOperatorId()){
					pSink = po;
				}
			}
		}
		return new PhysicalSeepQuery(pOps, pSources, pSink, instancesPerOriginalOp);
	}

}
