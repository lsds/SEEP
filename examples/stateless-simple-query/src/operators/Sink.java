package operators;

import java.util.ArrayList;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class Sink extends Operator implements StatelessOperator {

	private static final long serialVersionUID = 1L;

	@Override
	public void setUp() {
		myId = this.getOperatorId();
	}
	
	//latency measurements
	int lc = 0;
	
	// time control variables
	int c = 0;
	long init = 0;
	int sec = 0;
	int myId = 0;
	
	@Override
	public void processData(DataTuple dt) {
		int value2 = dt.getInt("value2");
		// TIME CONTROL
		c++;
		if((System.currentTimeMillis() - init) > 1000){
			System.out.println("SNK: "+sec+" "+c+" ");
			c = 0;
			sec++;
			init = System.currentTimeMillis();
		}
	}

	@Override
	public void processData(ArrayList<DataTuple> arg0) {
	}
}