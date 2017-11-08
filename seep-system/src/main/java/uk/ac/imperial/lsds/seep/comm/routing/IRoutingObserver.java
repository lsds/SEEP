package uk.ac.imperial.lsds.seep.comm.routing;

import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.messages.Timestamp;

public interface IRoutingObserver {

	public void routingChanged(Map<Integer, Set<Timestamp>> newConstraints);
}
