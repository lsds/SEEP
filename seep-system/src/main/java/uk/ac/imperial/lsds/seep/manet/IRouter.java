package uk.ac.imperial.lsds.seep.manet;

import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;

public interface IRouter {

	public Integer route(long batchId);
	public void handleDownUp(DownUpRCtrl downUp);
}
