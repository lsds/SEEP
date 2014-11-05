package uk.ac.imperial.lsds.seepworker.core.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.api.ConnectionType;
import uk.ac.imperial.lsds.seep.api.PhysicalOperator;
import uk.ac.imperial.lsds.seep.api.UpstreamConnection;

public class CoreInputFactory {

	public static CoreInput buildCoreInputForOperator(PhysicalOperator o){
		List<InputAdapter> inputAdapters = new LinkedList<>();
		// Create an InputAdapter per upstream connection -> know with the streamId
		Map<Integer, List<UpstreamConnection>> streamToOpConn = new HashMap<>();
		for(UpstreamConnection uc : o.upstreamConnections()){
			int streamId = uc.getStreamId();
			if(streamToOpConn.containsKey(streamId)){
				streamToOpConn.get(streamId).add(uc);
			}
			else{
				List<UpstreamConnection> l = new ArrayList<>();
				l.add(uc);
				streamToOpConn.put(streamId, l);
			}
		}
		// Perform sanity check. All ops for a given streamId should have the same connType
		for(List<UpstreamConnection> l : streamToOpConn.values()){
			ConnectionType ct = null;
			for(UpstreamConnection oct : l){
				if(ct == null){
					ct = oct.getConnectionType();
				}
				if(!ct.equals(oct.getConnectionType())){
					// TODO: throw error
					System.out.println("Sanity check FAILED");
					System.exit(0);
				}
			}
		}
		// Build an input adapter per streamId
		for(Integer streamId : streamToOpConn.keySet()){
			List<UpstreamConnection> upCon = streamToOpConn.get(streamId);
			InputAdapter ia = InputAdapterFactory.buildInputAdapterOfTypeForOps(streamId, upCon);
			inputAdapters.add(ia);
		}
		CoreInput cInput = new CoreInput(inputAdapters);
		return cInput;
	}
	
//	public static CoreInput _buildCoreInputForOperator(PhysicalOperator o){
//		// Create an InputAdapter per upstream connection -> know with the streamId
//		Map<Integer, List<Operator_ConnType_Schema>> streamToOpConn = new HashMap<>();
//		for(UpstreamConnection uc : o.upstreamConnections()){
//			PhysicalOperator po = (PhysicalOperator)uc.getUpstreamOperator();
//			ConnectionType ct = uc.getConnectionType();
//			Schema s = uc.getExpectedSchema();
//			Operator_ConnType_Schema aggr = new Operator_ConnType_Schema(po, ct, s);
//			int streamId = uc.getStreamId();
//			if(streamToOpConn.containsKey(streamId)){
//				streamToOpConn.get(streamId).add(aggr);
//			}
//			else{
//				List<Operator_ConnType_Schema> l = new ArrayList<>();
//				l.add(aggr);
//				streamToOpConn.put(streamId, l);
//			}
//		}
//		// Perform sanity check. All ops for a given streamId should have the same connType
//		for(List<Operator_ConnType_Schema> l : streamToOpConn.values()){
//			ConnectionType ct = null;
//			for(Operator_ConnType_Schema oct : l){
//				if(ct == null){
//					ct = oct.ct;
//				}
//				if(!ct.equals(oct.ct)){
//					// TODO: throw error
//					System.out.println("Sanity check FAILED");
//					System.exit(0);
//				}
//			}
//		}
//		// Build an input adapter per streamId
//		for(Integer streamId : streamToOpConn.keySet()){
//			List<Operator_ConnType_Schema> ops = streamToOpConn.get(streamId);
//			InputAdapter ia = InputAdapterFactory.buildInputAdapterOfTypeForOps(ops);
//			
//		}
//		return null;
//	}
	
//	static class Operator_ConnType_Schema {
//		
//		public final PhysicalOperator po;
//		public final ConnectionType ct;
//		public final Schema s;
//		
//		public Operator_ConnType_Schema(PhysicalOperator po, ConnectionType ct, Schema s){
//			this.po = po;
//			this.ct = ct;
//			this.s = s;
//		}
//	}
	
}
