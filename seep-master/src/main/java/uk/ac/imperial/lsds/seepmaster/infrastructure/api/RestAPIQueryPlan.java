package uk.ac.imperial.lsds.seepmaster.infrastructure.api;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.util.MultiMap;

import uk.ac.imperial.lsds.seep.api.LogicalSeepQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RestAPIQueryPlan implements RestAPIRegistryEntry {

	public static final ObjectMapper mapper = new ObjectMapper();

	private LogicalSeepQuery queryPlan;
	
	private Map<String, Object> extractQPInformation() {
		Map<String, Object> qpInformation = new HashMap<String, Object>();

//		List<Object> nodes = new ArrayList<Object>();
//		List<Object> edges = new ArrayList<Object>();
//
//		for (Integer id : queryPlan.getElements().keySet()) {
//			Map<String, Object> nDetails = new HashMap<>();
//			
//			Connectable c = queryPlan.getElements().get(id);
//			nDetails.put("id", "" + c.getOperatorId());
//			
//			if (c.getOpContext().getOperatorStaticInformation() != null) {
//				nDetails.put("ip", c.getOpContext().getOperatorStaticInformation().getMyNode().getIp());
//				nDetails.put("port", c.getOpContext().getOperatorStaticInformation().getMyNode().getPort());
//			}
//			
//			if (c.getOpContext().isSource())
//				nDetails.put("type", "graph_type_source");
//			else if (c.getOpContext().isSink())
//				nDetails.put("type", "graph_type_sink");
//			else 
//				nDetails.put("type", "graph_type_query");
//			
//			Map<String, Object> nData = new HashMap<String, Object>();
//			nData.put("data", nDetails);
//			nodes.add(nData);
//			
//			Iterator<PlacedOperator> iter = c.getOpContext().downstreams.iterator();
//			while (iter.hasNext()) {
//				PlacedOperator po = iter.next();
//				Map<String, Object> eDetails = new HashMap<>();
//				eDetails.put("streamid", c.getOperatorId() + "-" + po.opID());
//				eDetails.put("source", "" + c.getOperatorId());
//				eDetails.put("target", "" + po.opID());
//				eDetails.put("type", "graph_edge_defaults");
//				Map<String, Object> eData = new HashMap<String, Object>();
//				eData.put("data", eDetails);
//				edges.add(eData);
//			}
//		}
//		
//		qpInformation.put("nodes", nodes);
//		qpInformation.put("edges", edges);
		return qpInformation;
	}
	
	public RestAPIQueryPlan(LogicalSeepQuery queryPlan) {
		this.queryPlan = queryPlan;
	}
	
	@Override
	public Object getAnswer(MultiMap<String> reqParameters) {
		return extractQPInformation();
	}

}
