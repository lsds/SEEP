package uk.ac.imperial.lsds.seepworker.core.input;

import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.api.ConnectionType;
import uk.ac.imperial.lsds.seep.api.DataOrigin;
import uk.ac.imperial.lsds.seep.api.Operator;
import uk.ac.imperial.lsds.seep.api.UpstreamConnection;
import uk.ac.imperial.lsds.seep.api.data.Schema;

public class InputAdapterFactory {

	public static InputAdapter buildInputAdapterOfTypeForOps(int streamId, List<UpstreamConnection> upc){
		InputAdapter ia = null;
		short cType = upc.get(0).getConnectionType().ofType();
		DataOrigin dOriginType = upc.get(0).getDataOrigin();
		Schema expectedSchema = upc.get(0).getExpectedSchema();
		List<Operator> ops = new ArrayList<>();
		for(UpstreamConnection uc : upc){
			ops.add(uc.getUpstreamOperator());
		}
		if(cType == ConnectionType.BATCH.ofType()){
			
		}
		else if(cType == ConnectionType.ONE_AT_A_TIME.ofType()){
			// TODO: here we'll need a factory to create different internal implementation, 
			// one-queue-per-conn, one-single-queue, etc
			ia = new DataStream(streamId, dOriginType, expectedSchema, ops);
		}
		else if(cType == ConnectionType.ORDERED.ofType()){
			
		}
		else if(cType == ConnectionType.UPSTREAM_SYNC_BARRIER.ofType()){
			
		}
		else if(cType == ConnectionType.WINDOW.ofType()){
			
		}
		return ia;
	}
	
}
