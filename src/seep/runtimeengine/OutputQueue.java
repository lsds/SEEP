package seep.runtimeengine;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import seep.Main;
import seep.P;
import seep.buffer.Buffer;
import seep.comm.serialization.BatchDataTuple;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.NodeManager;
import seep.operator.EndPoint;
import seep.operator.Operator;

public class OutputQueue {

	// replaySemaphore controls whether it is possible to send or not
	private AtomicInteger replaySemaphore = new AtomicInteger(0);
	private Kryo k = null;
	
	public OutputQueue(){
		this.k = initializeKryo();
	}
	
	private Kryo initializeKryo(){
		//optimize here kryo
		Kryo k = new Kryo();
		k.register(DataTuple.class);
		k.register(BatchDataTuple.class);
		return k;
	}
	
	//Start incoming data, one thread has finished replaying
	public synchronized void start(){
		/// \todo {this is a safe check that should not be done because we eventually will be sure that it works well}
		if(replaySemaphore.get() == 0){
			NodeManager.nLogger.warning("-> Dispatcher. replaySemaphore was 0, stays equals ");
			replaySemaphore.set(0);
			return;
		}
		NodeManager.nLogger.info("-> Dispatcher. replaySemaphore decrements: "+replaySemaphore.toString());
		replaySemaphore.decrementAndGet();
		synchronized(this){
			this.notify();
		}
	}
	
	public synchronized void stop() {
		//Stop incoming data, a new thread is replaying
		NodeManager.nLogger.info("-> Dispatcher. replaySemaphore increments: "+replaySemaphore.toString());
		
		/**
		 * hack done on july the third 2012 to get parallel recovery results.
		 *  we make sure that conn is only stop once
		 */
//if (replaySemaphore.get() > 0){
//	return;
//}
		replaySemaphore.incrementAndGet();
	}
	
	public void sendToDownstream(DataTuple tuple, EndPoint dest, boolean now, boolean beacon) {

		if(dest instanceof CommunicationChannel){
			CommunicationChannel channelRecord = (CommunicationChannel) dest;
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
						channelRecord.addDataToBatch(tuple);
					}
					//If it is mandated to send the tuple now (URGENT), then channelBatchSize is put to 0
					if(now) channelRecord.resetChannelBatchSize();
					long currentTime = System.currentTimeMillis();
					/// \todo{Add the following line for include the batch timing mechanism}
//					if(channelRecord.channelBatchSize == 0 || (currentTime - channelRecord.getTick) > ExecutionConfiguration.maxLatencyAllowed ){
					if(channelRecord.getChannelBatchSize() == 0){
						BatchDataTuple msg = channelRecord.getBatch();
	
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
		else if(dest instanceof Operator){
			Operator operatorObj = (Operator) dest;
			operatorObj.processData(tuple);
		}
	}
	
	public void replay(CommunicationChannel oi){
		long a = System.currentTimeMillis();
				while(oi.getSharedIterator().hasNext()){
					BatchDataTuple batch = oi.getSharedIterator().next();
					Output output = oi.getOutput();
					k.writeObject(output, batch);
					output.flush();
				}
		long b = System.currentTimeMillis() - a;
		System.out.println("Dis.replay: "+b);
	}
	
	public void replayTuples(CommunicationChannel cci) {
				Iterator<BatchDataTuple> sharedIterator = cci.getBuffer().iterator();
				Output output = cci.getOutput();
				int bufferSize = cci.getBuffer().size();
				int controlThreshold = (int)(bufferSize)/10;
				int replayed = 0;
				while(sharedIterator.hasNext()) {
					BatchDataTuple dt = sharedIterator.next();
					k.writeObject(output, dt);
					output.flush();
					replayed++;
					/// \test {test this functionality. is this necessary?}
					if((bufferSize-replayed) <= (controlThreshold+1)){
						break;
					}
				}
				//Restablish communication. Set variables and sharedIterator with the current iteration state.
				NodeManager.nLogger.info("-> Recovering connections");
				cci.getReplay().set(true);
				cci.getStop().set(false);
				cci.setSharedIterator(sharedIterator);
				start();
	}
	
}
