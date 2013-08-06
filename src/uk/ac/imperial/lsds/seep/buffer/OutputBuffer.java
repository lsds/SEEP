package uk.ac.imperial.lsds.seep.buffer;

import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;

public class OutputBuffer {

	public BatchTuplePayload batch;
	public int channelOpId; // the opId in the other side
	
	public OutputBuffer(BatchTuplePayload batch, int channelOpId){
		this.batch = batch;
		this.channelOpId = channelOpId;
	}
}
