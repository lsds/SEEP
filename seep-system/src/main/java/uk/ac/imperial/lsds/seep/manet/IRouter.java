package uk.ac.imperial.lsds.seep.manet;

import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.manet.GraphUtil.InetAddressNodeId;

public interface IRouter {

	public Integer route(long batchId);
	public void handleDownUp(DownUpRCtrl downUp);
	public void updateNetTopology(
			Map<InetAddressNodeId, Map<InetAddressNodeId, Double>> linkState);
}
