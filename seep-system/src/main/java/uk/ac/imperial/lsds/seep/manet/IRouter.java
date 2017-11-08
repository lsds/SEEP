package uk.ac.imperial.lsds.seep.manet;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Timestamp;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;

public interface IRouter {

	public ArrayList<Integer> route(Timestamp batchId);
	public Map<Integer, Set<Timestamp>> handleDownUp(DownUpRCtrl downUp);
	public void handleDownFailed(int downOpId);
	public void updateNetTopology(
			Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> linkState);
	public Set<Timestamp> areConstrained(Set<Timestamp> queued);
	public Map<Integer, Set<Timestamp>> handleWeights(Map<Integer, Double> newWeights, Integer downUpdated);
}
