package uk.ac.imperial.lsds.seep.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seep.util.Utils;

public class PhysicalSeepQuery {

	private List<PhysicalOperator> physicalOperators;
	private List<PhysicalOperator> sources;
	private PhysicalOperator sink;
	private Map<PhysicalOperator, List<PhysicalOperator>> instancesPerOriginalOp;
	
	private PhysicalSeepQuery(List<PhysicalOperator> physicalOperators, List<PhysicalOperator> pSources, PhysicalOperator pSink,
			Map<PhysicalOperator, List<PhysicalOperator>> instancesPerOriginalOp) {
		this.physicalOperators = physicalOperators;
		this.sources = pSources;
		this.sink = pSink;
		this.instancesPerOriginalOp = instancesPerOriginalOp;
	}
	
	public static PhysicalSeepQuery buildPhysicalQueryFrom(Set<SeepQueryPhysicalOperator> physicalOperators, 
			Map<PhysicalOperator, List<PhysicalOperator>> instancesPerOriginalOp, LogicalSeepQuery lsq) {
		// create physical connections
		for(Operator o : physicalOperators) {
			// update all downstream connections -> this will update the downstream's upstreams
			for(DownstreamConnection dc : o.downstreamConnections()) {
				// this will replace a still logical connection with a physical one
				// note that we don't need to update connectionType, this info is already there
				o.connectTo(dc.getDownstreamOperator(), dc.getStreamId(), dc.getSchema());
			}
		}
		List<PhysicalOperator> pOps = new ArrayList<>(physicalOperators);
		List<PhysicalOperator> pSources = new ArrayList<>();
		PhysicalOperator pSink = null;
		for(Operator o : lsq.getSources()) {
			// if this necessary?
			for(Operator po : pOps){
				if(po.getOperatorId() == o.getOperatorId()){
					pSources.add((PhysicalOperator)po);
				}
				if(po.getOperatorId() == lsq.getSink().getOperatorId()){
					pSink = (PhysicalOperator)po;
				}
			}
		}
		return new PhysicalSeepQuery(pOps, pSources, pSink, instancesPerOriginalOp);
	}
	
	public List<PhysicalOperator> getOperators(){
		return physicalOperators;
	}
	
	public List<PhysicalOperator> getSources(){
		return sources;
	}
	
	public PhysicalOperator getSink(){
		return sink;
	}
	
	public PhysicalOperator getOperatorWithId(int opId){
		for(PhysicalOperator po : this.physicalOperators){
			if(po.getOperatorId() == opId){
				return po;
			}
		}
		return null;
	}
	
	public PhysicalOperator getOperatorLivingInExecutionUnitId(int euId){
		for(PhysicalOperator po : this.physicalOperators){
			if(po.getIdOfWrappingExecutionUnit() == euId){
				return po;
			}
		}
		return null;
	}
	
	public Set<EndPoint> getMeshTopology(int euId){
		Set<EndPoint> meshTopology = new HashSet<>();
		for(PhysicalOperator po : physicalOperators) {
			if( (!isSource(po)) && (!isSink(po)) && po.getOperatorId() != euId){
				meshTopology.add(po.getWrappingEndPoint());
			}
		}
		return meshTopology;
	}
	
	private boolean isSource(PhysicalOperator po){
		for(PhysicalOperator src : sources){
			if(src.getOperatorId() == po.getOperatorId()){
				return true;
			}
		}
		return false;
	}
	
	private boolean isSink(PhysicalOperator po){
		return po.getOperatorId() == sink.getOperatorId();
	}
	
	public Set<Integer> getIdOfEUInvolved(){
		Set<Integer> ids = new HashSet<>();
		for(PhysicalOperator o : this.physicalOperators){
			ids.add(o.getIdOfWrappingExecutionUnit());
		}
		return ids;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("##############");
		sb.append(Utils.NL);
		sb.append("Physical QUERY:");
		sb.append(Utils.NL);
		sb.append("Sources:");
		sb.append(Utils.NL);
		for(PhysicalOperator src : sources){
			sb.append(src.toString());
			sb.append(Utils.NL);
		}
		sb.append("All Operators:");
		sb.append(Utils.NL);
		for(PhysicalOperator op : physicalOperators){
			sb.append(op.toString());
			sb.append(Utils.NL);
		}
		sb.append("Sink:");
		sb.append(Utils.NL);
		sb.append(sink.toString());
		sb.append(Utils.NL);
		sb.append("##############");
		return sb.toString();
	}

}
