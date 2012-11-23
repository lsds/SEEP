package seep.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import seep.P;
import seep.operator.Operator;
import seep.operator.OperatorContext;
import seep.operator.QuerySpecificationI;
import seep.operator.StatefulOperator;

public class QueryPlan {
	
	static final int CONTROL_SOCKET = Integer.parseInt(P.valueFor("controlSocket"));
	static final int DATA_SOCKET = Integer.parseInt(P.valueFor("dataSocket"));
	
	private ArrayList<Operator> ops = new ArrayList<Operator>();
	public Map<Integer,QuerySpecificationI> elements = new HashMap<Integer, QuerySpecificationI>();
	//More than one source is supported
	private ArrayList<Operator> src = new ArrayList<Operator>();
	private Operator snk;
	
	public ArrayList<Operator> getOps() {
		return ops;
	}

	public Map<Integer, QuerySpecificationI> getElements() {
		return elements;
	}

	public ArrayList<Operator> getSrc() {
		return src;
	}

	public Operator getSnk() {
		return snk;
	}
	
	//This method is still valid to define which is the first operator in the query
	public void setSource(Operator source) {
		NodeManager.nLogger.info("Configured NEW SOURCE, Operator: "+src.toString());
		src.add(source);
	}

	public void setSink(Operator snk){
		NodeManager.nLogger.info("Configured SINK as Operator: "+snk.toString());
		this.snk = snk;
	}
	
	public void addOperator(Operator o) {
		ops.add(o);
		elements.put(o.getOperatorId(), o);
		NodeManager.nLogger.info("Added new Operator to Infrastructure: "+o.toString());
	}
	
	public void placeNew(Operator o, Node n) {
		int opID = o.getOperatorId();
		boolean isStatefull = (o instanceof StatefulOperator) ? true : false;
		OperatorStaticInformation l = new OperatorStaticInformation(n, CONTROL_SOCKET + opID, DATA_SOCKET + opID, isStatefull);
		o.getOpContext().setOperatorStaticInformation(l);
		
		for (OperatorContext.PlacedOperator downDescr: o.getOpContext().downstreams) {
			int downID = downDescr.opID();
			QuerySpecificationI downOp = elements.get(downID);
			downOp.getOpContext().setUpstreamOperatorStaticInformation(opID, l);
		}

		for (OperatorContext.PlacedOperator upDescr: o.getOpContext().upstreams) {
			int upID = upDescr.opID();
			QuerySpecificationI upOp = elements.get(upID);
			upOp.getOpContext().setDownstreamOperatorStaticInformation(opID, l);
		}
	}
	
}
