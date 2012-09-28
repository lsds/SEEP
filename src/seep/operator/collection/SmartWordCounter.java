package seep.operator.collection;

import seep.Main;
import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.InitState;
import seep.operator.Operator;
import seep.operator.StatefullOperator;
import seep.operator.StateSplitI;
import seep.operator.workers.StateBackupWorker;
import seep.comm.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections.*;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class SmartWordCounter extends Operator implements StatefullOperator{
	
	//test purposes
	int counter = 0;
	long t_start = 0;
	long period = 30000;
	String chosenWord = "wwwwwwww";
	int backupTime = 5000;
	
	
	//latency instrumentation
	private StringBuilder sb = null;
	private int numTuples = 0;
	private long tinit = 0;
	
	
	HashMap<String, Integer> countMap = new HashMap<String, Integer>();
	
	
	//finish test purposes
	public void save(){
		try {
			BufferedWriter bos = new BufferedWriter(new FileWriter(new File("latency.dat")));
			String data = null;
			synchronized(sb){
				data = sb.toString();
			}
//			System.out.println("DATA: "+data);
			bos.write(data);
			bos.close();
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("SAVED!!!!!");
	}
	

	int c = 0;
//	private StateBackupWorker stateBackupWorker;
	private boolean first = true;

	public int getCounter(){
		return c;
	}

	public void setCounter(int c){
		this.c = c;
	}

	public SmartWordCounter(int opID) {
		super(opID);
		
		subclassOperator = this;
		//There is only one so...
		int mode = 2;
		switch(mode){
		case 0:
			break;
		case 1:
			Random r = new Random(52);
			for(int i = 0; i<10000; i++){
				int random = r.nextInt();
				String s = Integer.toString(random);
				countMap.put(s, new Integer(random));
			}
			break;
		case 2:
			Random r2 = new Random(52);
			for(int i = 0; i<100000; i++){
				int random = r2.nextInt();
				String s = Integer.toString(random);
				countMap.put(s, new Integer(random));
			}
			break;
		}
	}

	public HashMap<String, Integer> getCountMap(){
		return countMap;
	}
	
	public synchronized void processData(DataTuple dt) {
////	System.out.println(".");
//		if(first){
//			//new Thread(stateBackupWorker).start();
//			first = false;
//			StateBackupWorker stw = new StateBackupWorker(this);
//			t_start = System.currentTimeMillis();
//			tinit = t_start;
//			new Thread(stw).start();
//			sb = new StringBuilder();
//		}
//		
//		long instantTime = System.currentTimeMillis();
//		//current elapsed time
//		int currentTime = (int)(instantTime - t_start);
//		
//		numTuples++;
//		int t = (int) ((int)(instantTime - tinit)/1000);
//		if(numTuples == 10){
//			//Print time and latency for this tuple
//			String out = (t+" "+(instantTime - dt.getTs())+"\n");
//			sb.append(out);
////			System.out.println("# "+t+" "+(instantTime - dt.getTs()));
//			numTuples = 0;
//		}
//		
//		String word = dt.getString();
//		int count = 0;
//		if(countMap.containsKey(word)){
//			count = countMap.get(word);
//			countMap.put(word, ++count);
//		}
//		else{
//			countMap.put(word, 1);
//		}
//		//countMap.put(word, ++count);
//		
////		System.out.println(currentTime+" compare "+period);
//		
//		if(currentTime >= period){
//			if((System.currentTimeMillis()-t_start) >= period){
////System.out.println("Period.");
//				if(countMap.containsKey(chosenWord)){
//System.out.println("word: "+chosenWord);
//					count = countMap.get(chosenWord);
//					Seep.DataTuple.Builder b = Seep.DataTuple.newBuilder();
//					b.setTs(dt.getTs())
//						.setString(chosenWord)
//						.setInt(count);
//					c++;
//					sendDown(b.build());
//					System.out.println("Sending counter for: "+chosenWord+ ": "+count);
////				//every 1000 tuples backup state.
////				if(c == 1000){
////					synchronized(this){
////						generateBackupState();
////					}
////				}
//				}
//			}
//			t_start = System.currentTimeMillis();
//		}
	}

//	public void installState(Seep.InitState is){
//System.out.println("INSTALLING STATE: ###############################");
//		countMap = new HashMap<String, Integer>();
//		counter = 0;
//
//		//Extract the specific state to this operator
//		Seep.WordCounterState wcS = is.getWcState();
//		Seep.WordCounterState.Entry e = null;
//System.out.println("Number of entries of this state: "+wcS.getEntryCount());
//		List<Seep.WordCounterState.Entry> entries = wcS.getEntryList();
//		for(Seep.WordCounterState.Entry entry : entries){
//			countMap.put(entry.getWord(), entry.getCounter());
//		}
////		for(int i = 0; i<wcS.getEntryCount(); i++){
////			e = wcS.getEntry(i);
////			countMap.put(e.getWord(), e.getCounter());
////		}
//System.out.println("Op: "+getOperatorId()+" has restored state");
//System.out.println("###############################");
//	}

//	@Override
//	public void generateBackupState() {
//		
//			HashMap countMap = getCountMap();
////long a = System.currentTimeMillis();
//			Seep.WordCounterState.Builder wcS = Seep.WordCounterState.newBuilder();
//			List<String> words = null;
//			synchronized(countMap){
//				words = new ArrayList<String>(countMap.keySet());
//			}
//			//List<Integer> words = new ArrayList<Integer>(countMap.keySet());
//			String wordsA[] = words.toArray(new String [0]);
////System.out.println("OP: "+getOperatorId()+" my state has "+wordsA.length+" entries");
//			//Integer wordsA[] = words.toArray(new Integer [0]);
//			List<Integer> counters = null;
//			synchronized(countMap){
//				counters = new ArrayList<Integer>(countMap.values());
//			}
//			Integer countersA[] = counters.toArray(new Integer[0]);
//			Seep.WordCounterState.Entry.Builder eB = Seep.WordCounterState.Entry.newBuilder();
//			for(int i = 0; i<wordsA.length; i++){
//				eB.setWord(wordsA[i]);
//				//eB.setWordH(wordsA[i]);
//				eB.setCounter(countersA[i]);
//				wcS.addEntry(eB.build());
//			}
//			Seep.BackupState.Builder bsB = Seep.BackupState.newBuilder();
//			//Developer needs to save just the state
//			bsB.setWcState(wcS.build());
//			//System.out.println("WORDCOUNTER: Checkpointing state");
//			//backupState(bsB, this);
//			/*This method is called each time the developer decides to checkpoint the state. A more powerful API should be offered, for instance able to support timers, and configured initially or whatever... For now, this explicitly does a backup of the state, and in this specific example, the developer chooses to checkpoint each time data is processed in this operator*/
//			backupState(bsB);
//			setCounter(0);
//		
//		
//	}

	@Override
	public long getBackupTime() {
		return backupTime;
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void installState(InitState is) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateBackupState() {
		// TODO Auto-generated method stub
		
	}
}
