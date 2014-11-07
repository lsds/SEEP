package uk.ac.imperial.lsds.seepworker.core.output.routing;

import java.util.List;

import uk.ac.imperial.lsds.seep.api.DownstreamConnection;

public class Router {

	private int streamId;
	private RoutingState rs;
	
	public Router(int streamId, RoutingState rs){
		this.streamId = streamId;
		this.rs = rs;
	}
	
	public static Router buildRouterFor(List<DownstreamConnection> cons) {
		int streamId = cons.get(0).getStreamId();
		boolean stateful = cons.get(0).getDownstreamOperator().isStateful();
		RoutingState rs = null;
		if(stateful){
			rs = new ConsistentHashingRoutingState();
		}
		else{
			rs = new RoundRobinRoutingState();
		}
		return new Router(streamId, rs);
	}
	
}
