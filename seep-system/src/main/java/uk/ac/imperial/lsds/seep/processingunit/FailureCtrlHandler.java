package uk.ac.imperial.lsds.seep.processingunit;

public class FailureCtrlHandler {
	
	private final IProcessingUnit owner;

	public FailureCtrlHandler(IProcessingUnit owner)
	{
		this.owner = owner;
	}
	
	public void handleFailureCtrl(IFailureCtrl fctrl, int downstreamOpId)
	{
		//nodeFctrl.update(fctrl);
		
		//boolean nodeOutChanged = purgeNodeOut();
		
		//refreshNodeOutTimers(fctr.alives());
		
		//boolean senderOutsChanged = purgeSenderOutputQueues();
		
		//batchAckBuffer.purge(nodeFctrl);
		
		//boolean inputQueuesChanged = purgeLogicalInputQueues();
		
		//notifyChannels()
	}
	

}
