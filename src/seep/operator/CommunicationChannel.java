package seep.operator;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.esotericsoftware.kryo.io.Output;

import seep.Main;
import seep.buffer.Buffer;
//import seep.comm.Replayer;
import seep.comm.serialization.BatchDataTuple;
import seep.comm.serialization.DataTuple;
import seep.comm.tuples.Seep;

/**
* OutputInformation. This class models the information associated to a downstream or upstream connection
*/
public class CommunicationChannel{

	private Socket downstreamDataSocket;
	private Socket downstreamControlSocket;
	private Buffer buffer;
	
	private Output output = null;
	private BufferedOutputStream bos = null;
	
	//Set atomic variables to their initial value
	private AtomicBoolean stop = new AtomicBoolean(false);
	private AtomicBoolean replay = new AtomicBoolean(false);
	
	public long reconf_ts;
	private long last_ts;
	private Iterator<BatchDataTuple> sharedIterator;
	
	//Batch information for this channel
	private BatchDataTuple batch = new BatchDataTuple();
	private int channelBatchSize = Integer.parseInt(Main.valueFor("batchLimit"));
	private long tick = 0;

	public CommunicationChannel(Socket downstreamSocketD, Socket downstreamSocketC, Buffer buffer){
		this.downstreamDataSocket = downstreamSocketD;
		this.downstreamControlSocket = downstreamSocketC;
		this.buffer = buffer;
		try {
			/// \fixme{this must be fixed, different CONSTRUCTORS, please...}
			if(downstreamDataSocket != null){
				//Create buffered output stream
				bos = new BufferedOutputStream(downstreamSocketD.getOutputStream());
				//Create the kryo output for this socket
				output = new Output(bos);
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Socket getDownstreamControlSocket(){
		return downstreamControlSocket;
	}
	
	public void setSharedIterator(Iterator<BatchDataTuple> i){
		this.sharedIterator = i;
	}
	
	public Iterator<BatchDataTuple> getSharedIterator(){
		return sharedIterator;
	}
	
	public Output getOutput() {
		// TODO Auto-generated method stub
		return null;
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
	
	public BatchDataTuple getBatch(){
		return batch;
	}
	
	/// \fixme{batching is broken after changing serialization mechanism, check this}
	public void addDataToBatch(DataTuple dt){
		batch.addTuple(dt);
		channelBatchSize--;
		last_ts = dt.getTs();
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
		int limit = Integer.parseInt(Main.valueFor("batchLimit"));
		channelBatchSize = limit;
	}
	
	public long getLast_ts(){
		return last_ts;
	}
	
	public void setReconf_ts(long ts){
		this.reconf_ts = ts;
	}
}
