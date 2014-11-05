package uk.ac.imperial.lsds.seep.api;

import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.api.data.Schema;

public class SeepQueryLogicalOperator implements LogicalOperator {

	private int opId;
	private String name;
	private boolean stateful;
	private SeepTask task;
	private LogicalState state;
	
	
	private List<DownstreamConnection> downstream = new ArrayList<DownstreamConnection>();
	private List<UpstreamConnection> upstream = new ArrayList<UpstreamConnection>();
	
	private SeepQueryLogicalOperator(int opId, SeepTask task, String name){
		this.opId = opId;
		this.task = task;
		this.name = name;
		this.stateful = false;
	}
	
	private SeepQueryLogicalOperator(int opId, SeepTask task, LogicalState state, String name){
		this.opId = opId;
		this.task = task;
		this.name = name;
		this.state = state;
		this.stateful = true;
	}
	
	public static LogicalOperator newStatelessOperator(int opId, SeepTask task){
		String name = new Integer(opId).toString();
		return SeepQueryLogicalOperator.newStatelessOperator(opId, task, name);
	}
	
	public static LogicalOperator newStatelessOperator(int opId, SeepTask task, String name){
		return new SeepQueryLogicalOperator(opId, task, name);
	}
	
	public static LogicalOperator newStatefulOperator(int opId, SeepTask task, LogicalState state){
		String name = new Integer(opId).toString();
		return SeepQueryLogicalOperator.newStatefulOperator(opId, task, state, name);
	}
	
	public static LogicalOperator newStatefulOperator(int opId, SeepTask task, LogicalState state, String name){
		return new SeepQueryLogicalOperator(opId, task, state, name);
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
	public void connectTo(Operator downstreamOperator, int streamId, Schema schema) {
		this.connectTo(downstreamOperator, streamId, schema, ConnectionType.ONE_AT_A_TIME);
	}

	@Override
	public void connectTo(Operator downstreamOperator, int streamId, Schema schema, ConnectionType connectionType) {
//		// Add downstream to this operator
//		this.addDownstream(downstreamOperator, streamId, schema);
//		// Add this, as upstream, to the downstream operator
//		((SeepQueryLogicalOperator)downstreamOperator).addUpstream(this, connectionType, streamId, schema);
		this.connectTo(downstreamOperator, streamId, schema, connectionType, DataOrigin.NETWORK);
	}
	
	@Override
	public void connectTo(Operator downstreamOperator, int streamId, Schema schema, ConnectionType connectionType, DataOrigin dSrc){
		// Add downstream to this operator
		this.addDownstream(downstreamOperator, streamId, schema, dSrc);
		// Add this, as upstrea, to the downstream operator
		((SeepQueryLogicalOperator)downstreamOperator).addUpstream(this, connectionType, streamId, schema, dSrc);
	}
	
	/* Methods to manage logicalOperator connections */
	
	private void addDownstream(Operator lo, int streamId, Schema schema, DataOrigin dSrc){
		DownstreamConnection dc = new DownstreamConnection(lo, streamId, schema, dSrc);
		this.downstream.add(dc);
	}
	
	private void addUpstream(Operator lo, ConnectionType connectionType, int streamId, Schema schema, DataOrigin dSrc){
		UpstreamConnection uc = new UpstreamConnection(lo, connectionType, streamId, schema, dSrc);
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
					+ ""+down.getDownstreamOperator().getOperatorId());
			sb.append(ls);
		}
		sb.append("#Upstream: "+this.upstream.size());
		sb.append(ls);
		for(int i = 0; i<this.upstream.size(); i++){
			UpstreamConnection up = upstream.get(i);
			sb.append("  Up-conn-"+i+"-> StreamId: "+up.getStreamId()+" to opId: "
					+ ""+up.getUpstreamOperator().getOperatorId()+""
							+ " with connType: "+up.getConnectionType());
			sb.append(ls);
		}
		return sb.toString();
	}

}
