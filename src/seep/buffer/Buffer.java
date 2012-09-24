package seep.buffer;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

import seep.comm.serialization.BatchDataTuple;
import seep.comm.tuples.Seep;

/**
* Buffer class models the buffers for the connections between operators in our system
*/

public class Buffer implements Serializable{

	private static final long serialVersionUID = 1L;

	private Deque<BatchDataTuple> buff = new LinkedBlockingDeque<BatchDataTuple>();
	
	private Seep.BackupState bs = null;

	public Iterator<BatchDataTuple> iterator() { return buff.iterator(); }

	public Buffer(){
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

	public void save(BatchDataTuple batch){
		buff.add(batch);
	}
	
/// \test trim() should be tested
	public void trim(long ts){
//System.out.println("TO TRIM");
		Iterator<BatchDataTuple> iter = buff.iterator();
		int numOfTuplesPerBatch = 0;
		while (iter.hasNext()) {
			BatchDataTuple next = iter.next();
			long timeStamp = 0;
			numOfTuplesPerBatch = next.getBatchSize();
			//Accessing last index cause that is the newest tuple in the batch
			timeStamp = next.getTuple(numOfTuplesPerBatch-1).getTs();
//System.out.println("#events: "+numOfTuplesPerBatch+" timeStamp: "+timeStamp+" ts: "+ts);
			if (timeStamp <= ts) iter.remove();
			else break;
		}
	}
}
