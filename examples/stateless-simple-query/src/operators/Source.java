package operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.Payload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;


public class Source extends Operator implements StatelessOperator {

	private static final long serialVersionUID = 1L;

	@Override
	public void setUp() {
		
	}
	
	@Override
	public void processData(DataTuple dt) {

		Map<String, Integer> mapper = new HashMap<String, Integer>();
		for(int i = 0; i<this.getOpContext().getDeclaredWorkingAttributes().size(); i++){
			System.out.println("MAP: "+this.getOpContext().getDeclaredWorkingAttributes().get(i));
			mapper.put(this.getOpContext().getDeclaredWorkingAttributes().get(i), i);
		}
		DataTuple data = new DataTuple(mapper, new TuplePayload());
		
		while(true){
			int value1 = 5;
			int value2 = 15;
			int value3 = 2;
		
//			DataTuple output = DataTuple.getNoopDataTuple();
//		
//			TuplePayload tp = new TuplePayload();
//			Payload p = new Payload();
//			p.add(value1);
//			p.add(value2);
//			p.add(value3);
//			tp.attrValues = p;
//			output.set(tp);
		
			DataTuple output = data.newTuple(value1, value2, value3);
		
			send(output);
		
			try {
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void processData(ArrayList<DataTuple> arg0) {
		// TODO Auto-generated method stub
		
	}

}
