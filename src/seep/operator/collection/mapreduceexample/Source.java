package seep.operator.collection.mapreduceexample;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Source extends Operator implements StatelessOperator{
	
	private static final long serialVersionUID = 1L;

	private String wikiData = "workload";
	
	private int records = 0;
	
	/** INPUT RATE xVAR **/
	private int alpha = 50;
	
	/** TIME CONTROL**/
	int counter = 0;
	long t_start = 0;
	long i_time = 0;
	boolean first = true;
	int avgCounter = 0;
	double avg = 0;
	int sec = 0;
	
	public Source(int opId){
		super(opId);
		subclassOperator = this;
	}

	@Override
	public void processData(DataTuple dt) {
		if(first){
			t_start = System.currentTimeMillis();
			first = false;
//			new Thread(ackWorker).start();
		}
		Kryo kryo = new Kryo();
		kryo.register(DataTuple.class);

		InputStream is = null;
		try {
			is = new FileInputStream(wikiData);
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Input i = new Input(is);
		while(true){
			records++;
			DataTuple tuple = kryo.readObjectOrNull(i, DataTuple.class);
			if(tuple == null){
				System.out.println("Replayed: "+records+". FINISHED");
				System.exit(0);
			}
			for(int j = 0; j<alpha; j++){
				sendDown(tuple);
			}
			try{
				Thread.sleep(1);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			i_time = System.currentTimeMillis();
			long currentTime = i_time - t_start;
			counter += alpha;
			if(currentTime >= 1000){
				sec++;
//				System.out.println("E/S: "+counter);
//				System.out.println("INPUTQ-counter: "+MetricsReader.eventsInputQueue.getCount());
				t_start = System.currentTimeMillis();
				avg += counter;
				counter = 0;
				avgCounter++;
				if(avgCounter == 10){
					avg = avg/10;
					System.out.println("# AVG INPUT RATE(10s): "+avg+" SEC: "+sec);
					avgCounter =0;
					avg = 0;
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
