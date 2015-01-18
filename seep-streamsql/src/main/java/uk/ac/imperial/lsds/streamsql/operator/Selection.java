package uk.ac.imperial.lsds.streamsql.operator;

import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.streamsql.predicates.IPredicate;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class Selection implements StatelessOperator, IStreamSQLOperator {

	private static final long serialVersionUID = 1L;

	private IPredicate predicate;

	public Selection(IPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	public void processData(DataTuple data) {

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
	public void processData(List<DataTuple> dataList) {
		for (DataTuple tuple : dataList)
			processData(tuple);
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}


}
