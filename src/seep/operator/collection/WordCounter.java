package seep.operator.collection;

import seep.Main;
import seep.comm.serialization.DataTuple;
import seep.comm.tuples.Seep;
import seep.comm.tuples.Seep.DataTuple.Builder;
import seep.operator.Operator;
import seep.operator.StatefullOperator;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class WordCounter extends Operator implements StatefullOperator{

//test purposes
int counter = 0;
long t_start = 0;
long period = 5000;
String chosenWord = "Unix";
	//finish test purposes


	int c = 0;
	//private StateBackupWorker stateBackupWorker;
	private boolean first = true;

	public int getCounter(){
		return c;
	}

	public void setCounter(int c){
		this.c = c;
	}

	public WordCounter(int opID) {
		super(opID);
		subclassOperator = this;
		//stateBackupWorker = new StateBackupWorker(this);
		//There is only one so...
	}

	HashMap<String, Integer> countMap = new HashMap<String, Integer>();

	public HashMap<String, Integer> getCountMap(){
		return countMap;
	}
	
	public synchronized void processData(DataTuple dt) {
//
//		if(first){
//			//new Thread(stateBackupWorker).start();
//			first = false;
//			t_start = System.currentTimeMillis();
//		}
////		String word = dt.getString();
////		int count = 0;
////		if(countMap.get(word) != null){
////			count = countMap.get(word);
////		}
////		else{
////			countMap.put(word, 0);
////		}
////		countMap.put(word, ++count);
////		Seep.DataTuple.Builder b = Seep.DataTuple.newBuilder();
////		b.setTs(dt.getTs())
////			.setString(word)
////			.setInt(count);
////		c++;
////		//System.out.println("WC: word-> "+word+" counter-> "+count);
////		sendDown(b.build());
////		//every 1000 tuples backup state.
////		if(c == 1000){
////			generateBackupState();
////		}
//		t_start = System.currentTimeMillis();
	}

	public void installState(Seep.InitState is){
		countMap = new HashMap<String, Integer>();
		counter = 0;
		Seep.WordCounterState wcS = is.getWcState();
		Seep.WordCounterState.Entry e = null;
		for(int i = 0; i<wcS.getEntryCount(); i++){
			e = wcS.getEntry(i);
			countMap.put(e.getWord(), e.getCounter());
		}
		System.out.println("OP"+getOperatorId()+" -> has restored state");
	}

	@Override
	public void generateBackupState() {
		
			HashMap<String, Integer> countMap = getCountMap();
			Seep.WordCounterState.Builder wcS = Seep.WordCounterState.newBuilder();
			List<String> words = null;
			synchronized(countMap){
				words = new ArrayList<String>(countMap.keySet());
			}
			String wordsA[] = words.toArray(new String [0]);
			List<Integer> counters = null;
			synchronized(countMap){
				counters = new ArrayList<Integer>(countMap.values());
			}
			Integer countersA[] = counters.toArray(new Integer[0]);
			Seep.WordCounterState.Entry.Builder eB = Seep.WordCounterState.Entry.newBuilder();
			for(int i = 0; i<wordsA.length; i++){
				eB.setWord(wordsA[i]);
				eB.setCounter(countersA[i]);
				wcS.addEntry(eB.build());
			}
			Seep.BackupState.Builder bsB = Seep.BackupState.newBuilder();
			//Developer needs to save just the state
			bsB.setWcState(wcS.build());
			backupState(bsB);
			setCounter(0);
		
	}

	@Override
	public long getBackupTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
