package seep.operator.collection.testing;

import java.util.concurrent.ArrayBlockingQueue;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

@SuppressWarnings("serial")
public class TestSource extends Operator implements StatelessOperator{

	private ArrayBlockingQueue<DataTuple> outputBucket = new ArrayBlockingQueue<DataTuple>(1000000);
	
	
	public TestSource(int opID) {
		super(opID);
		subclassOperator = this;
	}

	
	public void processData(DataTuple dt) {
		int value = 0;
		int c = 0;
		int fake = 0;
		//Create and initialize a generator of data
//		Thread generator = new Thread(new Gen());
//		generator.start();
//		Thread worker = new Thread(new Worker());
//		worker.start();
		while(true){
			DataTuple data = new DataTuple();
			if(c == 2){
				value = 1;
				c = 0;
			}
			data.setId(value);
			value = 0;
			c++;
			sendDown(data);
			fake++;
			
/***
			try {
				sendDown(outputBucket.take());
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
****/
			
//			System.out.println("Sent: "+fake);
//			try{
//				Thread.sleep(50);
//			} 
//			catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}
	
	public class Worker implements Runnable{

		@Override
		public void run() {
			while(true){
				try {
					sendDown(outputBucket.take());
				} 
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	
	public class Gen implements Runnable{

		@Override
		public void run() {
			int value = 0;
			int c = 0;
			int fake = 0;
			while (true){
				DataTuple data = new DataTuple();
				if(c == 2){
					value = 1;
					c = 0;
				}
				data.setId(value);
				value = 0;
				c++;
				try {
					outputBucket.put(data);
				} 
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

}
