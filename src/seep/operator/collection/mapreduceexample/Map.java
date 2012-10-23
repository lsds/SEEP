package seep.operator.collection.mapreduceexample;

import java.util.HashMap;
import java.util.LinkedList;

import seep.comm.routing.Router;
import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.StateI;
import seep.infrastructure.monitor.MetricsReader;
import seep.operator.Operator;
import seep.operator.StateSplitI;
import seep.operator.StatelessOperator;

public class Map extends Operator implements StatelessOperator, StateSplitI{
	
	private static final long serialVersionUID = 1L;
	
	private final boolean simpleTOPKPartitioning = true;
	
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
		if(first){
			hackRouter();
			first = false;
		}
//		System.out.println("RX: "+dt.getCountryCode()+" Visit article: "+dt.getArticle());
		//Emit <K,V> being <countryCode, 1>
		dt.setMRValue(1);
		sendDown(dt, dt.getCountryCode().hashCode());
//		hacked_send(dt);
		
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

	@Override
	public StateI[] parallelizeState(StateI toSplit, int key, String stateClass) {
		StateI []partitions = new StateI[2];
		if(!simpleTOPKPartitioning){
			if(stateClass.equals("class seep.operator.collection.mapreduceexample.TopKReduceState")){
				TopKReduceState oldPartition = new TopKReduceState();
				TopKReduceState newPartition = new TopKReduceState();
			
				oldPartition.top5Visits = ((TopKReduceState)toSplit).top5Visits;
				newPartition.top5Visits = ((TopKReduceState)toSplit).top5Visits;

				HashMap<String, Integer> oldCountryCode = new HashMap<String, Integer>();
				HashMap<String, Integer> newCountryCode = new HashMap<String, Integer>();
			
				HashMap<String, Integer> countryCodeToSplit = ((TopKReduceState)toSplit).countryCode;
				System.out.println("HAVE TO SPLIT entries: "+countryCodeToSplit.size());
				for(String code : countryCodeToSplit.keySet()){
					//old
					if(Router.customHash(code) < key){
						oldCountryCode.put(code, countryCodeToSplit.get(code));
					}
					//new
					else{
						newCountryCode.put(code, countryCodeToSplit.get(code));
					}
				}
				System.out.println("OLD split has: "+oldCountryCode.size());
				System.out.println("NEW split has: "+newCountryCode.size());
			
				oldPartition.countryCode = oldCountryCode;
				newPartition.countryCode = newCountryCode;
			
				LinkedList<TopPosition> oldTop = new LinkedList<TopPosition>();
				LinkedList<TopPosition> newTop = new LinkedList<TopPosition>();
			
				LinkedList<TopPosition> topToSplit = ((TopKReduceState)toSplit).top;
			
				for(int i = 0; i<topToSplit.size(); i++){
					TopPosition pos = topToSplit.get(i);
					String code = pos.countryCodeString;
					//old
					if(Router.customHash(code) < key){
						oldTop.add(pos);
					}
					//new
					else{
						newTop.add(pos);
					}
				}
				TopPosition aux = new TopPosition("trash", 0);
				int remainingOld = 4-oldTop.size();
				for(int k = 0; k <= remainingOld; k++){
//					int index = 5-(k + (5 - remaining));
//					oldTop.add(index, aux);
					oldTop.add(aux);
				}
				int remainingNew = 4-newTop.size();
				for(int k = 0; k <= remainingNew; k++){
//					int index = 5-(k + (5 - remaining));
//					oldTop.add(index, aux);
					newTop.add(aux);
				}
				System.out.println("LENGTH OLD: "+oldTop.size());
				oldPartition.top = oldTop;
				System.out.println("LENGTH NEW: "+newTop.size());
				newPartition.top = newTop;
			
				partitions[0] = oldPartition;
				partitions[1] = newPartition;
			}
			else{
				System.out.println("not matching classes ABORT");
			}
			return partitions;
		}
		else{
			TopKReduceState newS = new TopKReduceState();
			LinkedList<TopPosition> aux = new LinkedList<TopPosition>();
			for(int i = 0; i<5; i++){
				aux.add(new TopPosition("trash", 0));
			}
			newS.top = aux;
			partitions[0] = toSplit;
			partitions[1] = newS;
			return partitions;
		}
	}

}
