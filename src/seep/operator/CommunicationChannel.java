package seep.operator;

import java.net.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import seep.Main;
import seep.buffer.Buffer;
//import seep.comm.Replayer;
import seep.comm.tuples.Seep;

/**
* OutputInformation. This class models the information associated to a downstream or upstream connection
*/
/// \todo {This class mixes information that could be separated into a more consistent scheme}
public class CommunicationChannel{

	private Socket downstreamDataSocket;
	public Socket downstreamControlSocket;
	private Buffer buffer;
	//Set atomic variables to their initial value
	private AtomicBoolean stop = new AtomicBoolean(false);
	private AtomicBoolean replay = new AtomicBoolean(false);
	
	public long reconf_ts;
	private long last_ts;
	public Iterator<Seep.EventBatch> sharedIterator;
	
	//Batch information for this channel
	private Seep.EventBatch.Builder batch = Seep.EventBatch.newBuilder();
	private int channelBatchSize = Integer.parseInt(Main.valueFor("batchLimit"));
	private long tick = 0;

	public CommunicationChannel(Socket downstreamSocketD, Socket downstreamSocketC, Buffer buffer){
		this.downstreamDataSocket = downstreamSocketD;
		this.downstreamControlSocket = downstreamSocketC;
		this.buffer = buffer;
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
	
	public Seep.EventBatch.Builder getBatch(){
		return batch;
	}
	
	public void addDataToBatch(Seep.DataTuple dt){
		batch.addEvent(dt);
		channelBatchSize--;
		last_ts = dt.getTs();
	}
	
	public int getChannelBatchSize(){
		return channelBatchSize;
	}
	
	public void resetChannelBatchSize(){
		channelBatchSize = 0;
	}
	
	public Seep.EventBatch buildBatch(){
		Seep.EventBatch msg = batch.build();
		return msg;
	}
	
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
