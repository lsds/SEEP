package uk.ac.imperial.lsds.streamsql.operator;

import java.util.ArrayList;
import java.util.List;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.streamsql.expressions.Constants;
import uk.ac.imperial.lsds.streamsql.visitors.OperatorVisitor;

public class Projection implements StatelessOperator, IStreamSQLOperator {

	/*
	 * Attributes for the projection
	 */
	List<String> attributes;

	public Projection(List<String> attributes) {
		this.attributes = attributes;
	}

	public Projection(String attribute) {
		this(new ArrayList<String>());
		attributes.add(attribute);
	}

	public Projection() {
		this(new ArrayList<String>());
	}

	@Override
	public void setUp() {
	}


	@Override
	public void processData(DataTuple data) {
		
		List<Object> projectedValues = new ArrayList<>();

		/*
		 * Add timestamp attribute
		 */
		projectedValues.add(data.getValue(Constants.TIMESTAMP));
		
		/*
		 * Add all attributes referred to in the projection operator
		 */
		for (String att : attributes) 
			projectedValues.add(data.getValue(att));
		
		/*
		 * Send the projected tuple
		 */
		DataTuple output = data.setValues(projectedValues);
		api.send(output);
	}


	@Override
	public void processData(List<DataTuple> dataList) {
		for (DataTuple tuple : dataList)
			processData(tuple);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Projection (");
		for (String att : attributes)
			sb.append(att.toString() + " ");
		sb.append(")");
		return sb.toString();
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);		
	}

}
