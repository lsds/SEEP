package operators;

import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Processor extends Operator implements StatelessOperator{

	private static final long serialVersionUID = 1L;

	@Override
	public void processData(DataTuple data) {
		int value1 = data.getInt("value1");
		int value2 = data.getInt("value2");
		int value3 = data.getInt("value3");
		
		value1 = value2+value3;
		value2 = value2/value3;
		value3 = value3*value3;
		
		DataTuple outputTuple = data.setValues(value1, value2, value3);
		send(outputTuple);
	}

	@Override
	public void processData(ArrayList<DataTuple> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
	}

}
