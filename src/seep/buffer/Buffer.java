package seep.buffer;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

import seep.comm.serialization.controlhelpers.BackupOperatorState;
import seep.comm.serialization.messages.BatchTuplePayload;

/**
* Buffer class models the buffers for the connections between operators in our system
*/

public class Buffer implements Serializable{

	private static final long serialVersionUID = 1L;

//	private Deque<BatchDataTuple> buff = new LinkedBlockingDeque<BatchDataTuple>();
	private Deque<BatchTuplePayload> buff = new LinkedBlockingDeque<BatchTuplePayload>();
	
	private BackupOperatorState bs = null;

//	public Iterator<BatchDataTuple> iterator() { return buff.iterator(); }
	public Iterator<BatchTuplePayload> iterator() { return buff.iterator(); }

	public Buffer(){
		//state cannot be null. before backuping it would be null and this provokes bugs
//\bug The constructor in Buffer is operator dependant, this must be fixed by means of interfaces that make it independent.
		BackupOperatorState initState = new BackupOperatorState();
		bs = initState;
	}
	
	public int size(){
		return buff.size();
	}

	public BackupOperatorState getBackupState(){
		return bs;
	}

	public void saveStateAndTrim(BackupOperatorState bs){
		//Save state
		this.bs = bs;
		long ts_e = bs.getState().getData_ts();
		
		//Trim buffer, eliminating those tuples that are represented by this state
		trim(ts_e);
	}
	
	public void replaceBackupOperatorState(BackupOperatorState bs) {
//		if(this.bs.getBackupOperatorState() != null && bs.getBackupOperatorState() != null){
//		System.out.println("% Buffer. replacing old state: "+this.bs.getBackupOperatorState()[0].getOpId()+" with "+bs.getBackupOperatorState()[0].getOpId());
//		}
		this.bs = bs;
	}

	public void save(BatchTuplePayload batch){
		buff.add(batch);
	}
	
//	public void save(BatchDataTuple batch){
//		buff.add(batch);
//	}
	
/// \test trim() should be tested
	public void trim(long ts){
//System.out.println("TO TRIM");
//		Iterator<BatchDataTuple> iter = buff.iterator();
		Iterator<BatchTuplePayload> iter = buff.iterator();
		int numOfTuplesPerBatch = 0;
		while (iter.hasNext()) {
//			BatchDataTuple next = iter.next();
			BatchTuplePayload next = iter.next();
			long timeStamp = 0;
			numOfTuplesPerBatch = next.batchSize;
			//Accessing last index cause that is the newest tuple in the batch
//			timeStamp = next.getTuple(numOfTuplesPerBatch-1).getTimestamp();
			timeStamp = next.getTuple(numOfTuplesPerBatch-1).timestamp;
//System.out.println("#events: "+numOfTuplesPerBatch+" timeStamp: "+timeStamp+" ts: "+ts);
			if (timeStamp <= ts) iter.remove();
			else break;
		}
	}
}
