package uk.ac.imperial.lsds.seepworker.core.output;

import java.util.List;

import uk.ac.imperial.lsds.seep.api.DownstreamConnection;

public class OutputAdapterFactory {

	public static OutputAdapter buildOutputAdapterOfTypeForOps(int streamId, List<DownstreamConnection> cons){
		OutputAdapter oa = null;
		
		oa = new SimpleOutput(cons);
		
		return oa;
	}
}
