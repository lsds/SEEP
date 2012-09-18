package seep.buffer;

import java.io.IOException;
import java.util.Iterator;

import seep.comm.Dispatcher;
import seep.comm.tuples.Seep;
import seep.operator.CommunicationChannel;

/**
* TupleReplayer. This is the runnable object in charge of replaying the buffer of a connection.
*/

public class TupleReplayer implements Runnable {

	private Iterator<Seep.EventBatch> sharedIterator;
	private int controlThreshold;
	private int bufferSize;
	private CommunicationChannel connection;
	private Dispatcher dispatcher;
	
	public TupleReplayer(CommunicationChannel oi, Dispatcher dispatcher) {
		this.connection = oi;
		oi.sharedIterator = oi.getBuffer().iterator();
		this.sharedIterator = oi.sharedIterator;
		bufferSize = oi.getBuffer().size();
		controlThreshold = (int)(bufferSize)/10;
		this.dispatcher = dispatcher;
	}
	
	@Override 
	public void run() {
		
		int replayed = 0;
		while(sharedIterator.hasNext()) {
			try{
				Seep.EventBatch dt = sharedIterator.next();
/// \todo{THIS PIECE OF SYNC WAS REMOVED ON 6-july-2012 to get ft results, test if is still consistent}
				synchronized(connection.getDownstreamDataSocket()){
					dt.writeDelimitedTo(connection.getDownstreamDataSocket().getOutputStream());
				}
				replayed++;
				//Criteria for knowing how to transfer control back to incomingdatahandler
				/// \test {test this functionality. is this necessary?}
				if((bufferSize-replayed) <= (controlThreshold+1)){
					break;
				}
			}
			catch(IOException io){
				System.out.println("ERROR in replayer when replaying info: "+io.getMessage());
				io.printStackTrace();
			}
		}
		//Restablish communication
		connection.getReplay().set(true);
		connection.getStop().set(false);
		dispatcher.startIncomingData();
	}
}
