package uk.ac.imperial.lsds.streamsql.operator;

import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.API;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.streamsql.conversion.StringConversion;
import uk.ac.imperial.lsds.streamsql.expressions.ColumnReference;
import uk.ac.imperial.lsds.streamsql.expressions.Constants;
import uk.ac.imperial.lsds.streamsql.expressions.IValueExpression;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class Projection implements StatelessOperator, IStreamSQLOperator {

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


	@Override
	public void processData(DataTuple data, API api) {
		
		List<Object> projectedValues = new ArrayList<>();

		/*
		 * Add timestamp attribute
		 */
		projectedValues.add(data.getValue(Constants.TIMESTAMP));
		
		/*
		 * Add all the content as defined by the projection expressions
		 */
		for (IValueExpression expression : expressions) 
			projectedValues.add(expression.eval(data));

		
		/*
		 * Send the projected tuple
		 */
		DataTuple output = data.setValues(projectedValues);
		api.send(output);
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

}
