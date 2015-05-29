package uk.ac.imperial.lsds.seep.comm.routing;

import java.util.Map;
import java.util.Set;

public interface IRoutingObserver {

	public void routingChanged(Map<Integer, Set<Long>> newConstraints);
}
