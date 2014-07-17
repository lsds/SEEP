package uk.ac.imperial.lsds.streamsql.op.stateless;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class Selection implements StatelessOperator, IStreamSQLOperator, IMicroOperatorCode {

	private static final long serialVersionUID = 1L;

	private IPredicate predicate;

	public Selection(IPredicate predicate) {
		this.predicate = predicate;
	}
	
	@Override
	public void processData(DataTuple data, API api) {

		/*
		 * Check whether predicate is satisfied for tuple 
		 */
		if (this.predicate.satisfied(data)) 
			/*
			 * Send the selected tuple
			 */
			api.send(data);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Selection (");
		sb.append(predicate.toString());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	@Override
	public void processData(List<DataTuple> dataList, API api) {
		for (DataTuple tuple : dataList)
			processData(tuple, api);
	}

	@Override
	public void processData(Map<Integer, IWindowBatch> windowBatches,
			IWindowAPI api) {
		
		assert(windowBatches.keySet().size() == 1);
		
		Iterator<List<DataTuple>> iter = windowBatches.values().iterator().next().windowIterator();
		while (iter.hasNext()) {
			List<DataTuple> windowResult = new ArrayList<>();
			for (DataTuple tuple : iter.next())
				/*
				 * Check whether predicate is satisfied for tuple 
				 */
				if (this.predicate.satisfied(tuple)) 
					windowResult.add(tuple);
			api.outputWindowResult(windowResult);
		}
	}

}
