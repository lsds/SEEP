package uk.ac.imperial.lsds.seep.manet;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;

public interface IRouter {

	public ArrayList<Integer> route(long batchId);
	public Map<Integer, Set<Long>> handleDownUp(DownUpRCtrl downUp);
	public void updateNetTopology(
			Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> linkState);
}
