package seep.operator.collection.mapreduceexample;

import seep.comm.serialization.DataTuple;
import seep.infrastructure.monitor.MetricsReader;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Map extends Operator implements StatelessOperator{
	
	private static final long serialVersionUID = 1L;

	/** TIME CONTROL**/
	int counter = 0;
	long t_start = 0;
	long i_time = 0;
	boolean first = true;
	
	
	public Map(int opId){
		super(opId);
		subclassOperator = this;
	}
	
	@Override
	public void processData(DataTuple dt) {
//		System.out.println("RX: "+dt.getCountryCode()+" Visit article: "+dt.getArticle());
		//Emit <K,V> being <countryCode, 1>
		dt.setMRValue(1);
		sendDown(dt);
		
		/**TIME CONTROL**/
		i_time = System.currentTimeMillis();
		long currentTime = i_time - t_start;
		if(currentTime >= 1000){
			System.out.println("INPUTQ-counter: "+MetricsReader.eventsInputQueue.getCount());
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
