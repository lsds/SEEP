package seep.buffer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

import seep.Main;
import seep.comm.tuples.Seep;
import seep.utils.CommunicationChannel;
import seep.utils.ExecutionConfiguration;

/**
* Buffer class models the buffers for the connections between operators in our system
*/

@SuppressWarnings("serial")
public class Buffer implements Serializable{

	private Deque<Seep.EventBatch> buff = new LinkedBlockingDeque<Seep.EventBatch>();
	
	private Seep.BackupState bs = null;

	public Iterator<Seep.EventBatch> iterator() { return buff.iterator(); }

	public Buffer(){
//\todo This block of code is conditional on ft model. This must be done in a different way.
		if(Main.valueFor("ftmodel").equals("newModel")){
			//state cannot be null. before backuping it would be null and this provokes bugs
//\bug The constructor in Buffer is operator dependant, this must be fixed by means of interfaces that make it independent.
			Seep.WordCounterState.Builder wcs = Seep.WordCounterState.newBuilder();
			Seep.WordCounterState.Entry.Builder eB = Seep.WordCounterState.Entry.newBuilder();
			eB.setCounter(0);
			wcs.addEntry(eB.build());
			Seep.BackupState.Builder initState = Seep.BackupState.newBuilder();
			initState.setOpId(0);
			initState.setTsE(0);
			initState.setWcState(wcs.build());
			bs = initState.build();
		}
	}
	
	public int size(){
		return buff.size();
	}

	public Seep.BackupState getBackupState(){
		return bs;
	}

	public void saveStateAndTrim(Seep.BackupState bs){
		//Save state
		this.bs = bs;
		//Trim buffer, eliminating those tuples that are represented by this state
		trim(bs.getTsE());
	}
	
	public void replaceBackupState(Seep.BackupState bs) {
		this.bs = bs;
	}

	public void save(Seep.EventBatch batch){
		buff.add(batch);
	}
	
/// \test trim() should be tested
	public void trim(long ts){
//System.out.println("TO TRIM");
		Iterator<Seep.EventBatch> iter = buff.iterator();
		int numOfTuplesPerBatch = 0;
		while (iter.hasNext()) {
			Seep.EventBatch next = iter.next();
			long timeStamp = 0;
			numOfTuplesPerBatch = next.getEventCount();
			//Accessing last index cause that is the newest tuple in the batch
			timeStamp = next.getEvent(numOfTuplesPerBatch-1).getTs();
//System.out.println("#events: "+numOfTuplesPerBatch+" timeStamp: "+timeStamp+" ts: "+ts);
			if (timeStamp <= ts) iter.remove();
			else break;
		}
	}
	
	public void replay(CommunicationChannel oi){
long a = System.currentTimeMillis();
		while(oi.sharedIterator.hasNext()){
			Seep.EventBatch batch = oi.sharedIterator.next();
			try{
				batch.writeDelimitedTo(oi.getDownstreamDataSocket().getOutputStream());
			}
			catch(IOException io){
				System.out.println("While replaying: "+io.getMessage());
			}
		}
long b = System.currentTimeMillis() - a;
System.out.println("Dis.replay: "+b);
	}
}
