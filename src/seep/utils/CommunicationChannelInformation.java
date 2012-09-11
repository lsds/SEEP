package seep.utils;

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
public class CommunicationChannelInformation{

	public Socket downstreamSocketD;
	public Socket downstreamSocketC;
	public Buffer buffer;
	//Set atomic variables to their initial value
	public AtomicBoolean stop = new AtomicBoolean(false);
	public AtomicBoolean replay = new AtomicBoolean(false);
	
	public long reconf_ts;
	public long last_ts;
	public Iterator<Seep.EventBatch> sharedIterator;
	
	//Batch information for this channel
	public Seep.EventBatch.Builder batch = Seep.EventBatch.newBuilder();
	public int channelBatchSize = Integer.parseInt(Main.valueFor("batchLimit"));
	public long tick = 0;

	public CommunicationChannelInformation(Socket downstreamSocketD, Socket downstreamSocketC, Buffer buffer){
		this.downstreamSocketD = downstreamSocketD;
		this.downstreamSocketC = downstreamSocketC;
		this.buffer = buffer;
	}
}
