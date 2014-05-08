package operators;

import java.util.ArrayList;

import org.hamcrest.generator.qdox.junit.APITestCase;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Processor implements StatelessOperator{

	private static final long serialVersionUID = 1L;

	
	public void processData(DataTuple data) {
		int value1 = data.getInt("value1");
		int value2 = data.getInt("value2");
		int value3 = data.getInt("value3");
		
		value1 = value2+value3;
		value2 = value2/value3;
		value3 = value3*value3;
		
		DataTuple outputTuple = data.setValues(value1, value2, value3);
		api.send(outputTuple);
	}

	
	public void processData(ArrayList<DataTuple> arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void setUp() {
		// TODO Auto-generated method stub
		
	}

}
