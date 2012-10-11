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
	private int alpha = 10;
	
	/** TIME CONTROL**/
	int counter = 0;
	long t_start = 0;
	long i_time = 0;
	boolean first = true;
	
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
//			try{
//				Thread.sleep(1000);
//			}
//			catch(Exception e){
//				e.printStackTrace();
//			}
			i_time = System.currentTimeMillis();
			long currentTime = i_time - t_start;
			counter += alpha;
			if(currentTime >= 1000){
				System.out.println("E/S: "+counter);
//				System.out.println("INPUTQ-counter: "+MetricsReader.eventsInputQueue.getCount());
				t_start = System.currentTimeMillis();
				counter = 0;
			}
		}
		
	}
	
	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
