import java.util.ArrayList;

import operators.Processor;
import operators.Sink;
import operators.Source;
import uk.ac.imperial.lsds.seep.api.BaseI;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.operator.Operator;


public class Base implements BaseI{

	public QueryPlan compose() {
		QueryPlan qp = new QueryPlan();
		
		/** Declare operators **/
		
		// Declare Source
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("value1");
		srcFields.add("value2");
		srcFields.add("value3");
		Operator src = qp.newStatelessSource(new Source(), -1, srcFields);

		// Declare processor
		ArrayList<String> pFields = new ArrayList<String>();
		pFields.add("value1");
		pFields.add("value2");
		pFields.add("value3");
		Operator p = qp.newStatelessOperator(new Processor(), 1, pFields);
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("value1");
		snkFields.add("value2");
		snkFields.add("value3");
		Operator snk = qp.newStatelessSink(new Sink(), -2, snkFields);
		
		/** Connect operators **/
		src.connectTo(p, true, 0);
		p.connectTo(snk, true, 0);
		
		// Assign physical machines to ops
		qp.place(src, new Node(0));
		qp.place(p, new Node(1));
		qp.place(snk, new Node(2));
		
		return qp;
	}
}
