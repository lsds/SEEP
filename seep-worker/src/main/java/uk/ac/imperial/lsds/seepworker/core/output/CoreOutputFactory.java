package uk.ac.imperial.lsds.seepworker.core.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.api.DownstreamConnection;
import uk.ac.imperial.lsds.seep.api.PhysicalOperator;

public class CoreOutputFactory {

	public static CoreOutput buildCoreOutputForOperator(PhysicalOperator o){
		List<OutputAdapter> outputAdapters = new LinkedList<>();
		// Create an InputAdapter per upstream connection -> know with the streamId
		Map<Integer, List<DownstreamConnection>> streamToOpConn = new HashMap<>();
		for(DownstreamConnection dc : o.downstreamConnections()){
			int streamId = dc.getStreamId();
			if(streamToOpConn.containsKey(streamId)){
				streamToOpConn.get(streamId).add(dc);
			}
			else{
				List<DownstreamConnection> l = new ArrayList<>();
				l.add(dc);
				streamToOpConn.put(streamId, l);
			}
		}
		// Perform sanity check. All ops for a given streamId should have same schema
		// TODO:
		// Build an input adapter per streamId
		for(Integer streamId : streamToOpConn.keySet()){
			List<DownstreamConnection> doCon = streamToOpConn.get(streamId);
			OutputAdapter oa = OutputAdapterFactory.buildOutputAdapterOfTypeForOps(streamId, doCon);
			outputAdapters.add(oa);
		}
		CoreOutput cOutput = new CoreOutput(outputAdapters);	
		return cOutput;
	}
}
