package seep.operator.collection.lrbenchmark;

import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.InitState;
import seep.operator.Operator;
import seep.operator.StatefullOperator;
import seep.operator.StatelessOperator;

@SuppressWarnings("serial")
public class TollCollector extends Operator implements StatelessOperator{

	public TollCollector(int opID) {
		super(opID);
		subclassOperator = this;
	}
	
	//TESTING
	int counter = 0;
	long t_start = 0;
	boolean firstTime = true;
	long tinit = 0;
	
	
	@Override
	public synchronized void processData(Seep.DataTuple dt) {
		counter++;
		if(firstTime){
			firstTime = false;
			t_start = System.currentTimeMillis();
			tinit = System.currentTimeMillis();
		}
		
		sendDown(dt);
		
//		int t = (int) ((int)(System.currentTimeMillis() - tinit)/1000);
//	 	if(t > dt.getTime()+5){
//			System.out.println("TCol time: emit: "+dt.getTime()+" current: "+t);
//		}
//		if((System.currentTimeMillis()-t_start) >= 1000){
//		t_start = System.currentTimeMillis();
////		System.out.println("FW E/S: "+ackCounter);
//		System.out.println("TCol E/S: "+counter);
////		printRoutingInfo();
////		ackCounter = 0;
//		counter = 0;
//		}
	}


	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
