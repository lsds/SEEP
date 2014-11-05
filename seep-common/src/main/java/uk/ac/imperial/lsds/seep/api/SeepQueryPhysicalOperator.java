package uk.ac.imperial.lsds.seep.api;

import java.util.List;

import uk.ac.imperial.lsds.seep.api.data.Schema;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;

public class SeepQueryPhysicalOperator implements PhysicalOperator{
	
	private int opId;
	private String name;
	private SeepTask seepTask;
	private LogicalState state;
	private boolean stateful = false;
	private List<DownstreamConnection> downstreamConnections;
	private List<UpstreamConnection> upstreamConnections;
	private EndPoint ep;
	
	
	private SeepQueryPhysicalOperator(int opId, String name, SeepTask seepTask, 
									LogicalState state, List<DownstreamConnection> downstreamConnections, 
									List<UpstreamConnection> upstreamConnections, EndPoint ep) {
		this.opId = opId;
		this.name = name;
		this.seepTask = seepTask;
		this.state = state;
		if(this.state != null){
			this.stateful = true;
		}
		this.downstreamConnections = downstreamConnections;
		this.upstreamConnections = upstreamConnections;
		this.ep = ep;
	}
	
	public static SeepQueryPhysicalOperator createPhysicalOperatorFromLogicalOperatorAndEndPoint(int opId, Operator lo, EndPoint ep){
		return new SeepQueryPhysicalOperator(opId, lo.getOperatorName(), lo.getSeepTask(), 
				lo.getState(), lo.downstreamConnections(), lo.upstreamConnections(), ep);
	}
	
	public static SeepQueryPhysicalOperator createPhysicalOperatorFromLogicalOperatorAndEndPoint(Operator lo, EndPoint ep){
		return SeepQueryPhysicalOperator.createPhysicalOperatorFromLogicalOperatorAndEndPoint(lo.getOperatorId(), lo, ep);
	}

	@Override
	public int getOperatorId() {
		return opId;
	}

	@Override
	public String getOperatorName() {
		return name;
	}

	@Override
	public boolean isStateful() {
		return stateful;
	}

	@Override
	public LogicalState getState() {
		return state;
	}

	@Override
	public SeepTask getSeepTask() {
		return seepTask;
	}

	@Override
	public List<DownstreamConnection> downstreamConnections() {
		return this.downstreamConnections;
	}

	@Override
	public List<UpstreamConnection> upstreamConnections() {
		return this.upstreamConnections;
	}

	@Override
	public void connectTo(Operator downstreamOperator, int streamId, Schema schema) {
		
		replaceDownstream(downstreamOperator);
		
		replaceUpstream(downstreamOperator);
	}
	
	private void replaceDownstream(Operator downstreamOperator) {
		int opIdToUpdate = downstreamOperator.getOperatorId();
		for(DownstreamConnection dc : this.downstreamConnections) {
			if(dc.getDownstreamOperator().getOperatorId() == opIdToUpdate) {
				dc.replaceOperator(downstreamOperator);
			}
		}
	}
	
	private void replaceUpstream(Operator downstreamOperator) {
		for(UpstreamConnection uc : downstreamOperator.upstreamConnections()) {
			if(uc.getUpstreamOperator().getOperatorId() == this.getOperatorId()) {
				uc.replaceOperator(this);
			}
		}
	}

	@Override
	public void connectTo(Operator downstreamOperator, int streamId, Schema schema, ConnectionType connectionType) {
		// TODO REPLACEMENT IN THIS CASE
		
	}
	
	@Override
	public void connectTo(Operator downstreamOperator, int streamId, Schema schema, ConnectionType connectionType, DataOrigin dSrc) {
		// TODO REPLACEMENT IN THIS CASE
		
	}
	
	@Override
	public int getIdOfWrappingExecutionUnit() {
		return this.ep.getId();
	}
	
	@Override
	public EndPoint getWrappingEndPoint(){
		return ep;
	}
}
