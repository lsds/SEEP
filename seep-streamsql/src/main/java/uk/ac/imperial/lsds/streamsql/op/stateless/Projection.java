package uk.ac.imperial.lsds.streamsql.op.stateless;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;
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

	private static final long serialVersionUID = 1L;

	/*
	 * Expressions for the extended projection
	 */
	private List<IValueExpression> expressions;
	
	private  Map<String, Integer> idxMapper = null;

	public Projection(List<IValueExpression> expressions, List<String> formatForOutput) {
		this(expressions);
		this.idxMapper = new HashMap<>();
		for (int i = 0 ; i < formatForOutput.size(); i++)
			this.idxMapper.put(formatForOutput.get(i),i);
	}

	public Projection(List<IValueExpression> expressions) {
		this.expressions = expressions;
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

	private Object[] process(DataTuple data) {
		
		Object[] projectedValues = new Object[expressions.size()];

		/*
		 * Add all the content as defined by the projection expressions
		 */
		for (int i = 0; i < expressions.size(); i++) 
			projectedValues[i] = expressions.get(i).eval(data);
		
		/*
		 * Return the projected values
		 */
		return projectedValues;
	}
	
	@Override
	public void processData(DataTuple data, API api) {
		api.send(data.newTuple(process(data)));
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
		
		assert(windowBatches.keySet().size() == 1);

		if (this.idxMapper == null) {
			this.idxMapper = new HashMap<>();
			for (int i = 0 ; i < expressions.size(); i++)
				this.idxMapper.put(expressions.get(i).toString(),i);
		}
		
		Iterator<List<DataTuple>> iter = windowBatches.values().iterator().next().windowIterator();
		while (iter.hasNext()) {
			List<DataTuple> windowResult = new ArrayList<>();
			for (DataTuple tuple : iter.next()) 
				windowResult.add(new DataTuple(this.idxMapper, new Payload(process(tuple))));
			api.outputWindowResult(windowResult);
		}
	}

}
