package operators;

import java.util.ArrayList;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;


public class Source implements StatelessOperator {

	private static final long serialVersionUID = 1L;
	
	public void setUp() {
		
	}
	
	public void processData(DataTuple dt) {
		Map<String, Integer> mapper = api.getDataMapper();
		DataTuple data = new DataTuple(mapper, new TuplePayload());
		
		while(true){
			int value1 = 5;
			int value2 = 15;
			int value3 = 2;
			
			DataTuple output = data.newTuple(value1, value2, value3);
			
			api.send_toStreamId(output, 5);
			
			try {
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void processData(ArrayList<DataTuple> arg0) {
		// TODO Auto-generated method stub
		
	}
}


//DataTuple output = DataTuple.getNoopDataTuple();
//
//	TuplePayload tp = new TuplePayload();
//	Payload p = new Payload();
//	p.add(value1);
//	p.add(value2);
//	p.add(value3);
//	tp.attrValues = p;
//	output.set(tp);
