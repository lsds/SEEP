package uk.ac.imperial.lsds.seep.manet;

import java.net.InetAddress;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;

public interface IRouter {

	public Integer route(long batchId);
	public void handleDownUp(DownUpRCtrl downUp);
	public void updateNetTopology(
			Map<InetAddress, Map<InetAddress, Double>> linkState);
}
