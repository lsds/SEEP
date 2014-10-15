package uk.ac.imperial.lsds.seep.api;

import java.util.ArrayList;
import java.util.List;

public class SeepQueryOperator implements LogicalOperator {

	private int opId;
	private String name;
	private boolean stateful;
	private SeepTask task;
	private LogicalState state;
	
	private List<DownstreamConnection> downstream = new ArrayList<DownstreamConnection>();
	private List<UpstreamConnection> upstream = new ArrayList<UpstreamConnection>();
	
	private SeepQueryOperator(int opId, SeepTask task, String name){
		this.opId = opId;
		this.task = task;
		this.name = name;
		this.stateful = false;
	}
	
	private SeepQueryOperator(int opId, SeepTask task, LogicalState state, String name){
		this.opId = opId;
		this.task = task;
		this.name = name;
		this.state = state;
		this.stateful = true;
	}
	
	public static LogicalOperator newStatelessOperator(int opId, SeepTask task){
		String name = new Integer(opId).toString();
		return SeepQueryOperator.newStatelessOperator(opId, task, name);
	}
	
	public static LogicalOperator newStatelessOperator(int opId, SeepTask task, String name){
		return new SeepQueryOperator(opId, task, name);
	}
	
	public static LogicalOperator newStatefulOperator(int opId, SeepTask task, LogicalState state){
		String name = new Integer(opId).toString();
		return SeepQueryOperator.newStatefulOperator(opId, task, state, name);
	}
	
	public static LogicalOperator newStatefulOperator(int opId, SeepTask task, LogicalState state, String name){
		return new SeepQueryOperator(opId, task, state, name);
	}
	
	@Override
	public int getLogicalOperatorId() {
		return opId;
	}

	@Override
	public String getLogicalOperatorName() {
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
		return task;
	}

	@Override
	public List<DownstreamConnection> downstreamConnections() {
		return downstream;
	}

	@Override
	public List<UpstreamConnection> upstreamConnections() {
		return upstream;
	}

	@Override
	public void connectTo(LogicalOperator downstreamOperator, int streamId) {
		this.connectTo(downstreamOperator, streamId, ConnectionType.ONE_AT_A_TIME);
	}

	@Override
	public void connectTo(LogicalOperator downstreamOperator, int streamId,
			ConnectionType connectionType) {
		// Add downstream to this operator
		this.addDownstream(downstreamOperator, streamId);
		// Add this, as upstream, to the downstream operator
		((SeepQueryOperator)downstreamOperator).addUpstream(this, connectionType, streamId);
	}
	
	/* Methods to manage logicalOperator connections */
	
	private void addDownstream(LogicalOperator lo, int streamId){
		DownstreamConnection dc = new DownstreamConnection(lo, streamId);
		this.downstream.add(dc);
	}
	
	private void addUpstream(LogicalOperator lo, ConnectionType connectionType, int streamId){
		UpstreamConnection uc = new UpstreamConnection(lo, connectionType, streamId);
		this.upstream.add(uc);
	}
	
	/* Methods to print info about the operator */
	public String toString(){
		String ls = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("LogicalOperator");
		sb.append(ls);
		sb.append("###############");
		sb.append(ls);
		sb.append("Name: "+this.name);
		sb.append(ls);
		sb.append("OpId: "+this.opId);
		sb.append(ls);
		sb.append("Stateful?: "+this.stateful);
		sb.append(ls);
		sb.append("#Downstream: "+this.downstream.size());
		sb.append(ls);
		for(int i = 0; i<this.downstream.size(); i++){
			DownstreamConnection down = downstream.get(i);
			sb.append("  Down-conn-"+i+"-> StreamId: "+down.getStreamId()+" to opId: "
					+ ""+down.getDownstreamLogicalOperator().getLogicalOperatorId());
			sb.append(ls);
		}
		sb.append("#Upstream: "+this.upstream.size());
		sb.append(ls);
		for(int i = 0; i<this.upstream.size(); i++){
			UpstreamConnection up = upstream.get(i);
			sb.append("  Up-conn-"+i+"-> StreamId: "+up.getStreamId()+" to opId: "
					+ ""+up.getUpstreamLogicalOperator().getLogicalOperatorId()+""
							+ " with connType: "+up.getConnectionType());
			sb.append(ls);
		}
		return sb.toString();
	}

}
