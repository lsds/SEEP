package seep.operator;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import seep.Main;
import seep.buffer.Buffer;
import seep.comm.tuples.Seep;
import seep.infrastructure.NodeManager;

public class OutputQueue {

	// replaySemaphore controls whether it is possible to send or not
	private AtomicInteger replaySemaphore = new AtomicInteger(0);
	
	public OutputQueue(){
		
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
	
	public void sendToDownstream(Seep.DataTuple tuple, Object dest, boolean now, boolean beacon) {

		if(dest instanceof CommunicationChannel){
			CommunicationChannel channelRecord = (CommunicationChannel) dest;
			Socket sock = channelRecord.getDownstreamDataSocket();
			Buffer buffer = channelRecord.getBuffer();
			AtomicBoolean replay = channelRecord.getReplay();
			AtomicBoolean stop = channelRecord.getStop();
			try{
				//To send tuple
				if(replay.compareAndSet(true, false)){
					buffer.replay(channelRecord);
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
						Seep.EventBatch msg = channelRecord.buildBatch();
	
						channelRecord.setTick(currentTime);
						msg.writeDelimitedTo(sock.getOutputStream());
						channelRecord.cleanBatch();
						if(Main.valueFor("eftMechanismEnabled").equals("true")){
							buffer.save(msg);
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
			catch(IOException io){
				NodeManager.nLogger.severe("-> Dispatcher. While sending: "+io.getMessage());
				io.printStackTrace();
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
	
}
