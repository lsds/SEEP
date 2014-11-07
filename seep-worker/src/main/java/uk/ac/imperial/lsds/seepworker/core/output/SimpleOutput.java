package uk.ac.imperial.lsds.seepworker.core.output;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.api.DownstreamConnection;
import uk.ac.imperial.lsds.seep.api.data.OTuple;
import uk.ac.imperial.lsds.seepworker.core.output.routing.Router;


public class SimpleOutput implements OutputAdapter {

	private int streamId;
	private List<DownstreamConnection> cons;
	private Router router;
	private Map<Integer, OutputQueue> outputQueues;
	
	public SimpleOutput(List<DownstreamConnection> cons){
		this.cons = cons;
		this.router = Router.buildRouterFor(cons);
		this.streamId = cons.get(0).getStreamId();
		this.outputQueues = createOutputQueuesFor(cons);
	}

	@Override
	public int getStreamId() {
		return streamId;
	}

	@Override
	public void send(OTuple o) {
		outputQueues.get(0).dispatch(o.getData());
		
	}

	@Override
	public void sendAll(OTuple o) {
		// TODO Auto-generated method stub
		// send (write) to all queues
	}

	@Override
	public void sendKey(OTuple o, int key) {
		// TODO Auto-generated method stub
		// send to the queue that hash
	}

	@Override
	public void sendKey(OTuple o, String key) {
		// TODO Auto-generated method stub
		// same
	}

	@Override
	public void sendStreamid(int streamId, OTuple o) {
		// TODO Auto-generated method stub
		// non defined
	}

	@Override
	public void sendStreamidAll(int streamId, OTuple o) {
		// TODO Auto-generated method stub
		// non defined
	}

	@Override
	public void sendStreamidKey(int streamId, OTuple o, int key) {
		// TODO Auto-generated method stub
		// non defined
	}

	@Override
	public void sendStreamidKey(int streamId, OTuple o, String key) {
		// TODO Auto-generated method stub
		// non defined
	}

	@Override
	public void send_index(int index, OTuple o) {
		// TODO Auto-generated method stub
		// careful i guess
	}

	@Override
	public void send_opid(int opId, OTuple o) {
		// TODO Auto-generated method stub
		// careful again
	}
	
	private Map<Integer, OutputQueue> createOutputQueuesFor(List<DownstreamConnection> cons){
		Map<Integer, OutputQueue> outputs = new HashMap<>();
		
		return outputs;
	}
	
	class Sender implements Runnable {

		@Override
		public void run() {
			
			// check the output buffers, whatever that is and send downstream to the configured socket
			// this would require a wait-notify mechanism, otherwise this guy will be working like crazy
			
		}	
	}
	
}
