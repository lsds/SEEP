package uk.ac.imperial.lsds.streamsql.op.stateless;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.operator.compose.micro.IMicroOperatorCode;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowAPI;
import uk.ac.imperial.lsds.seep.operator.compose.window.IWindowBatch;
import uk.ac.imperial.lsds.streamsql.conversion.StringConversion;
import uk.ac.imperial.lsds.streamsql.expressions.ColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.op.IStreamSQLOperator;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class Projection implements StatelessOperator, IStreamSQLOperator, IMicroOperatorCode {

	/*
	 * Expressions for the extended projection
	 */
	List<IValueExpression> expressions;

	public Projection(List<IValueExpression> expression) {
		this.expressions = expression;
	}

	public Projection(String attribute) {
		this.expressions = new ArrayList<IValueExpression>();
		this.expressions.add(new ColumnReference(new StringConversion(), attribute));
	}
	
	public Projection(String[] attributes) {
		this.expressions = new ArrayList<IValueExpression>();
		for (String attribute : attributes)
		this.expressions.add(new ColumnReference(new StringConversion(), attribute));
	}

	@Override
	public void setUp() {
	}

	private DataTuple process(DataTuple data) {
		
		List<Object> projectedValues = new ArrayList<>();

		/*
		 * Add all the content as defined by the projection expressions
		 */
		for (IValueExpression expression : expressions) 
			projectedValues.add(expression.eval(data));
		
		/*
		 * Return the projected tuple
		 */
		return data.setValues(projectedValues);
	}
	
	@Override
	public void processData(DataTuple data, API api) {
		api.send(process(data));
	}


	@Override
	public void processData(List<DataTuple> dataList, API api) {
		for (DataTuple tuple : dataList)
			processData(tuple, api);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Projection (");
		for (IValueExpression att : expressions)
			sb.append(att.toString() + " ");
		sb.append(")");
		return sb.toString();
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);		
	}

	@Override
	public void processData(Map<Integer, IWindowBatch> windowBatches,
			IWindowAPI api) {
		for (Integer streamID : windowBatches.keySet()) {
			Iterator<List<DataTuple>> iter = windowBatches.get(streamID).windowIterator();
			while (iter.hasNext()) {
				List<DataTuple> windowResult = new ArrayList<>();
				for (DataTuple tuple : iter.next()) 
					windowResult.add(process(tuple));
				api.outputWindowResult(streamID, windowResult);
			}
		}
	}

}
