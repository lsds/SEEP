package seep.processingunit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import seep.comm.serialization.DataTuple;
import seep.infrastructure.NodeManager;
import seep.operator.Operator;
import seep.runtimeengine.CoreRE;


public class ProcessingUnit {

	private CoreRE owner = null;
	private PUContext ctx = null;
	//Operators managed by this processing unit [ opId<Integer> - op<Operator> ]
	static public Map<Integer, Operator> mapOP_ID = new HashMap<Integer, Operator>();
	private Operator mostUpstream = null;
	
	private ArrayList<Integer> listOfManagedStates = new ArrayList<Integer>();
	
	public ProcessingUnit(CoreRE owner){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr());
	}
	
	public void newOperatorInstantiation(Operator o) {
		//Detect the first submitted operator
		if(mapOP_ID.isEmpty()){
			mostUpstream = o;
		}
		o.setProcessingUnit(this);
		mapOP_ID.put(o.getOperatorId(), o);
	}

	public void setOpReady(int opId) {
		mapOP_ID.get(opId).setReady(true);
	}
	
	public PUContext setUpProcessingUnit(){
		ArrayList<Operator> operatorSet = (ArrayList<Operator>) mapOP_ID.values();
		ctx.configureOperatorConnections(operatorSet);
		return ctx;
	}
	
	public void processData(DataTuple data){
		mostUpstream.processData(data);
	}

	public void sendData(DataTuple dt, int minValue, boolean b) {
		// TODO Auto-generated method stub
		
		
	}
	
	/** Operator information management **/
	
	public void reconfigureOperatorLocation(int opId, InetAddress ip){
		mapOP_ID.get(opId).getOpContext().changeLocation(opId, ip);
	}
	
	public void reconfigureOperatorConnection(int opId, InetAddress ip){
		ctx.updateConnection(opId, ip);
	}
	
	/** State Management Stuff **/
	
	public synchronized void invalidateState(int opId) {
		//If the states figures as being managed we removed it
		int index = 0;
		if((index = listOfManagedStates.indexOf(opId)) != -1) listOfManagedStates.remove(index);
		// and then we clean both the buffer and the mapping in downstreamBuffers.
		if(ctx.downstreamBuffers.get(opId) != null){
			//First of all, we empty the buffer
			ctx.downstreamBuffers.get(opId).replaceBackupState(null);
		}
	}
	
	public synchronized void registerManagedState(int opId) {
		//If the state does not figure as being managed, we include it
		if(!listOfManagedStates.contains(opId)){
			NodeManager.nLogger.info("-> New STATE registered for OP: "+opId);
			listOfManagedStates.add(opId);
		}
	}
	
	public boolean isManagingStateOf(int opId) {
//		if(downstreamBuffers.get(opId) != null) return true;
		if(listOfManagedStates.contains(opId)) return true;
		return false;
	}
}
