import java.util.ArrayList;

import operators.Processor;
import operators.Sink;
import operators.Source;
import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.operator.Connectable;

public class Base implements QueryComposer{

	public QueryPlan compose() {
		/** Declare operators **/
		
		// Declare Source
		ArrayList<String> srcFields = new ArrayList<String>();
		srcFields.add("value1");
		srcFields.add("value2");
		srcFields.add("value3");
		Connectable src = QueryBuilder.newStatelessSource(new Source(), -1, srcFields);
		
		// Declare processor
		ArrayList<String> pFields = new ArrayList<String>();
		pFields.add("value1");
		pFields.add("value2");
		pFields.add("value3");
		Connectable p = QueryBuilder.newStatelessOperator(new Processor(), 1, pFields);
		
		// Declare sink
		ArrayList<String> snkFields = new ArrayList<String>();
		snkFields.add("value1");
		snkFields.add("value2");
		snkFields.add("value3");
		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), -2, snkFields);
		
		/** Connect operators **/
		src.connectTo(p, true, 0);
		p.connectTo(snk, true, 0);
		
		return QueryBuilder.build();
	}
}
