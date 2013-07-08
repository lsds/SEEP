package uk.co.imperial.lsds.seep.comm.serialization;

public class MetricsTuple {

	private int opId;
	
	private long inputQueueEvents;
	private long numberIncomingDataHandlerWorkers;
	
	public MetricsTuple(){
	}

	public int getOpId(){
		return opId;
	}
	
	public long getInputQueueEvents(){
		return inputQueueEvents;
	}
	
	public long getNumberIncomingDataHandlerWorkers(){
		return numberIncomingDataHandlerWorkers;
	}
	
	public void setOpId(int opId){
		this.opId = opId;
	}
	
	public void setInputQueueEvents(long inputQueueEvents) {
		this.inputQueueEvents = inputQueueEvents;		
	}
	
	public void setNumberIncomingDataHandlerWorkers(long numberIncomingdataHandlerWorkers2){
		this.numberIncomingDataHandlerWorkers = numberIncomingdataHandlerWorkers2; 
	}
}
