package seep.runtimeengine;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.esotericsoftware.kryo.io.Output;

import seep.P;
import seep.buffer.Buffer;
import seep.comm.serialization.BatchDataTuple;
import seep.comm.serialization.DataTuple;
import seep.comm.serialization.messages.BatchTuplePayload;
import seep.comm.serialization.messages.TuplePayload;
import seep.operator.EndPoint;

/**
* OutputInformation. This class models the information associated to a downstream or upstream connection
*/
public class CommunicationChannel implements EndPoint{

	private int targetOperatorId;
	private Socket downstreamDataSocket;
	private Socket downstreamControlSocket;
	private Buffer buffer;
	
	private Output output = null;
	private OutputStream bos = null;
	
	//Set atomic variables to their initial value
	private AtomicBoolean stop = new AtomicBoolean(false);
	private AtomicBoolean replay = new AtomicBoolean(false);
	
	public long reconf_ts;
	private long last_ts;
//	private Iterator<BatchDataTuple> sharedIterator;
	private Iterator<BatchTuplePayload> sharedIterator;
	
	//Batch information for this channel
//	private BatchDataTuple batch = new BatchDataTuple();
	private BatchTuplePayload batch = new BatchTuplePayload();
	private int channelBatchSize = Integer.parseInt(P.valueFor("batchLimit"));
	private long tick = 0;

	public CommunicationChannel(int opId, Socket downstreamSocketD, Socket downstreamSocketC, Buffer buffer){
		this.targetOperatorId = opId;
		this.downstreamDataSocket = downstreamSocketD;
		this.downstreamControlSocket = downstreamSocketC;
		this.buffer = buffer;
		try {
			/// \fixme{this must be fixed, different CONSTRUCTORS, please...}
			if(downstreamDataSocket != null){
				//Create buffered output stream
//				bos = new BufferedOutputStream(downstreamSocketD.getOutputStream());
				//Create common outputstream and let kryo to manage buffers
				bos = downstreamSocketD.getOutputStream();
				//Create the kryo output for this socket
				output = new Output(bos);
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getOperatorId(){
		return targetOperatorId;
	}
	
	public Socket getDownstreamControlSocket(){
		return downstreamControlSocket;
	}
	
//	public void setSharedIterator(Iterator<BatchDataTuple> i){
//		this.sharedIterator = i;
//	}
	
	public void setSharedIterator(Iterator<BatchTuplePayload> i){
		this.sharedIterator = i;
	}
	
//	public Iterator<BatchDataTuple> getSharedIterator(){
//		return sharedIterator;
//	}
	
	public Iterator<BatchTuplePayload> getSharedIterator(){
		return sharedIterator;
	}
	
	public Output getOutput() {
		return output;
	}
	
	public void setTick(long tick){
		this.tick = tick;
	}
	
	public Socket getDownstreamDataSocket(){
		return downstreamDataSocket;
	}
	
	public Buffer getBuffer(){
		return buffer;
	}
	
	public AtomicBoolean getReplay(){
		return replay;
	}
	
	public AtomicBoolean getStop(){
		return stop;
	}
	
//	public BatchDataTuple getBatch(){
//		return batch;
//	}
	
	public BatchTuplePayload getBatch(){
		return batch;
	}
	
	/// \fixme{batching is broken after changing serialization mechanism, check this}
//	public void addDataToBatch(DataTuple dt){
//		batch.addTuple(dt);
//		channelBatchSize--;
//		last_ts = dt.getTimestamp();
//	}
	
	public void addDataToBatch(TuplePayload payload){
//		System.out.println("TX: "+payload.attrValues.size());
		batch.addTuple(payload);
		channelBatchSize--;
		last_ts = payload.timestamp;
	}
	
	public int getChannelBatchSize(){
		return channelBatchSize;
	}
	
	public void resetChannelBatchSize(){
		channelBatchSize = 0;
	}
	
//	public Seep.EventBatch buildBatch(){
//		Seep.EventBatch msg = batch.build();
//		return msg;
//	}
	
	public void cleanBatch(){
		batch.clear();
		int limit = Integer.parseInt(P.valueFor("batchLimit"));
		channelBatchSize = limit;
	}
	
	public long getLast_ts(){
		return last_ts;
	}
	
	public void setReconf_ts(long ts){
		this.reconf_ts = ts;
	}
}
