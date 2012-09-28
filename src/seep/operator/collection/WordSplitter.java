package seep.operator.collection;

import seep.buffer.Buffer;
import seep.comm.routing.Router;
import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.BackupState;
import seep.operator.CommunicationChannel;
import seep.operator.Operator;
import seep.operator.StateSplitI;
import seep.operator.StatelessOperator;
import seep.operator.OperatorContext.PlacedOperator;

@SuppressWarnings("serial")
public class WordSplitter extends Operator implements StatelessOperator, StateSplitI{

	//test purposes
	int counter = 0;
	long t_start = 0;
	
	
	boolean first = true;
	//finish test purposes

	public WordSplitter(int opID) {
		super(opID);
		subclassOperator = this;
		//setDispatchPolicy(DispatchPolicy.PARTITION);
		//setDispatchPolicy(DispatchPolicy.ANY);
		
		//ContentBasedFilter cbf = new ContentBasedFilter("getString");
//		cbf.routeValueToDownstream(RouteOperator.EQ, 0, 10);
//		cbf.routeValueToDownstream(RouteOperator.EQ, 2, 20);
		//setDispatchPolicy(DispatchPolicy.CONTENT_BASED, cbf);
		
		//Define dispatching filter for different downstreams
		/** THIS would not need to do this, necessary to refactor the high level API **/
		
	}

	
	public synchronized void processData(DataTuple dt) {
//		if(first){
//			first = false;
//			t_start = System.currentTimeMillis();
//		}
//		String sentence = dt.getString();
//		String[] tokens = sentence.split(" ");
//		for (String word : tokens) {
//			if(word.equals("") || word.equals(" ") || word.equals("  ") || word.equals("(") || word.equals(")")){
//				continue;
//			}
//			Seep.DataTuple.Builder b = Seep.DataTuple.newBuilder();
//			b.setTs(dt.getTs())
//			 .setString(word);
//			sendDown(b.build());
//			counter++;
//			//this should be sendDown(tuple, value) to work properly...
////			System.out.println("word: "+word);
//		}
//		if((System.currentTimeMillis() - t_start) > 1000){
//			System.out.println("# "+counter);
//			counter = 0;
//			t_start = System.currentTimeMillis();
//		}
	}

//	@Override
//	public seep.comm.tuples.Seep.BackupState.Builder[] parallelizeState(BackupState toSplit, int key) {
//		System.out.println("#######################");
//		System.out.println("PARALLEL KEY: "+key);
//		System.out.println("#######################");
//		BackupState.Builder splitted[] = new BackupState.Builder[2];
//		
//		Seep.WordCounterState original = toSplit.getWcState();
//		
//		Seep.BackupState.Builder oldBS = Seep.BackupState.newBuilder();
//		Seep.WordCounterState.Builder oldS = Seep.WordCounterState.newBuilder();
//		Seep.BackupState.Builder newBS = Seep.BackupState.newBuilder();
//		Seep.WordCounterState.Builder newS = Seep.WordCounterState.newBuilder();
//		
//		for(Seep.WordCounterState.Entry e : original.getEntryList()){
//			int aux = e.getWord().hashCode();
//			if(Router.customHash(aux) < key){
//				oldS.addEntry(e);
//			}
//			else{
//				newS.addEntry(e);
//			}
//		}
//		
//		oldBS.setWcState(oldS);
//		newBS.setWcState(newS);
//		
//		splitted[0] = oldBS;
//		splitted[1] = newBS;
//		
//		return splitted;
//	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public BackupState[] parallelizeState(BackupState toSplit, int key) {
		// TODO Auto-generated method stub
		return null;
	}
}
