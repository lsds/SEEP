package seep.runtimeengine;

import java.util.ArrayList;

import seep.buffer.Buffer;
import seep.comm.serialization.DataTuple;
import seep.comm.serialization.messages.BatchTuplePayload;
import seep.comm.serialization.messages.Payload;
import seep.comm.serialization.messages.TuplePayload;
import seep.comm.serialization.serializers.ArrayListSerializer;
import seep.operator.EndPoint;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class AsynchronousCommunicationChannel implements EndPoint{

	private int opId;
	private Buffer buf;
	
	
	//Serialization tools
	private Kryo k;
	private Output o;
	
	public AsynchronousCommunicationChannel(int opId, Buffer buf, Output o){
		this.opId = opId;
		this.buf = buf;
		this.o = o;
		this.k = initializeKryo();
	}
	
	private Kryo initializeKryo(){
		//optimize here kryo
		Kryo k = new Kryo();
		k.register(ArrayList.class, new ArrayListSerializer());
		k.register(Payload.class);
		k.register(TuplePayload.class);
		k.register(BatchTuplePayload.class);
		return k;
	}
	
	@Override
	public int getOperatorId() {
		return opId;
	}
	
	public Output getOutput(){
		return o;
	}
	
	public void writeData(DataTuple dt){
		k.writeObject(o, dt.getPayload());
	}

}

/**
 * 
 public void sendToDownstream(DataTuple tuple, EndPoint dest, boolean now, boolean beacon) {

		SynchronousCommunicationChannel channelRecord = (SynchronousCommunicationChannel) dest;
		Buffer buffer = channelRecord.getBuffer();
		AtomicBoolean replay = channelRecord.getReplay();
		AtomicBoolean stop = channelRecord.getStop();
		//Output for this socket
		Output output = channelRecord.getOutput();
		try{
			//To send tuple
			if(replay.compareAndSet(true, false)){
				replay(channelRecord);
				replay.set(false);
				stop.set(false);
				//At this point, this operator has finished replaying the tuples
				NodeManager.setSystemStable();
			}
			if(!stop.get()){
				if(!beacon){
					channelRecord.addDataToBatch(tuple.getPayload());
				}
				//If it is mandated to send the tuple now (URGENT), then channelBatchSize is put to 0
				if(now) channelRecord.resetChannelBatchSize();
				long currentTime = System.currentTimeMillis();
				/// \todo{Add the following line for include the batch timing mechanism}
//				if(channelRecord.channelBatchSize == 0 || (currentTime - channelRecord.getTick) > ExecutionConfiguration.maxLatencyAllowed ){
				if(channelRecord.getChannelBatchSize() == 0){
//					BatchDataTuple msg = channelRecord.getBatch();
					BatchTuplePayload msg = channelRecord.getBatch();
					channelRecord.setTick(currentTime);
					
					k.writeObject(output, msg);
					//Flush the buffer to the stream
					output.flush();
					channelRecord.cleanBatch();
					
					if(P.valueFor("eftMechanismEnabled").equals("true")){
//							buffer.save(msg);
					}
				}
			}
			else if (!beacon){
				//Is there any thread replaying?
				while(replaySemaphore.get() >= 1){
					//If so, wait.
					synchronized(this){
						this.wait();
					}
				}
			}
		}
		catch(InterruptedException ie){
			NodeManager.nLogger.severe("-> Dispatcher. While trying to do wait() "+ie.getMessage());
			ie.printStackTrace();
		}
	}
 */ 
