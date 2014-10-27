import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.api.QueryBuilder;
import uk.ac.imperial.lsds.seep.api.QueryComposer;
import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seep.gc14.operator.Filter;
import uk.ac.imperial.lsds.seep.gc14.operator.Q1Prediction;
import uk.ac.imperial.lsds.seep.gc14.operator.Sink;
import uk.ac.imperial.lsds.seep.gc14.operator.Source;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

public class Base implements QueryComposer {

	@Override
	public QueryPlan compose() {
		
		// Declare src
		List<String> srcFields = new ArrayList<String>();
		srcFields.add("id");	//int 
		srcFields.add("timestamp");	// int
		srcFields.add("value");	// float
		srcFields.add("property");	// int
		srcFields.add("plug_id");	// int
		srcFields.add("household_id");	// int
		srcFields.add("house_id");	// int
		
		Connectable src = QueryBuilder.newStatelessSource(new Source(), 0, srcFields);

		//Declare filter
		Connectable filter = QueryBuilder.newStatelessOperator(new Filter(), 1, srcFields);
		
		/*
		 * Query 1
		 */
		//Declare q1
		StateWrapper sw = new StateWrapper(2, 5000, null); // let's use this only if we do dynamic scale out
		Connectable q1 = QueryBuilder.newStatefulOperator(new Q1Prediction(), 2, sw, srcFields);

		//Declare sink
		List<String> snkFields = new ArrayList<String>();
		snkFields.add("startTimeForPrediction");
		snkFields.add("house_id");
		snkFields.add("household_id");
		snkFields.add("plug_id");
		snkFields.add("predictedLoadForPlug");

		Connectable snk = QueryBuilder.newStatelessSink(new Sink(), 5, snkFields);

		//Connect operators
		src.connectTo(filter, true, 0);
		filter.connectTo(q1, true, 0);
		q1.connectTo(snk, true, 0);
		
		return QueryBuilder.build();
	}
}
