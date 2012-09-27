package seep.operator.collection.testing;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

@SuppressWarnings("serial")
public class TestSource extends Operator implements StatelessOperator{

	public TestSource(int opID) {
		super(opID);
		subclassOperator = this;
	}

	
	public void processData(DataTuple dt) {
		int value = 0;
		int c = 0;
		int fake = 0;
		while(true){
			DataTuple data = new DataTuple();
			if(c == 2){
				value = 1;
				c = 0;
			}
//			data.setId(value);
			value = 0;
			c++;
			sendDown(data);
			fake++;
//			System.out.println("Sent: "+fake);
//			try{
//				Thread.sleep(500);
//			} 
//			catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
