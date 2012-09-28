package seep.operator.collection.testing;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;
import seep.operator.workers.ACKWorker;

public class TestSink extends Operator implements StatelessOperator{

	
	private static final long serialVersionUID = 1L;
	
	int counter = 0;
	long t_start = 0;
	long i_time = 0;
	boolean first = true;
	
	private ACKWorker ackWorker;
	private long tACK;
	
	public long getTACK(){
		return tACK;
	}
	
	public TestSink(int opID) {
		super(opID);
		subclassOperator = this;
		ackWorker = new ACKWorker(this);
	}
	
	public void processData(DataTuple dt) {
//		System.out.println("RCV: "+dt.getId());
		tACK = dt.getTs();
		if(first){
			t_start = System.currentTimeMillis();
			first = false;
			new Thread(ackWorker).start();
		}
		i_time = System.currentTimeMillis();
		long currentTime = i_time - t_start;
		counter++;
		
		if(currentTime >= 1000){
			System.out.println("E/S: "+counter);
			t_start = System.currentTimeMillis();
			counter = 0;
		}
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
