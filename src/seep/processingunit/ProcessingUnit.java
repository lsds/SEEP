package seep.processingunit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import seep.comm.routing.Router;
import seep.comm.serialization.ControlTuple;
import seep.comm.serialization.DataTuple;
import seep.comm.serialization.controlhelpers.BackupState;
import seep.infrastructure.NodeManager;
import seep.operator.EndPoint;
import seep.operator.Operator;
import seep.operator.StatefulOperator;
import seep.operator.OperatorContext;
import seep.operator.OperatorStaticInformation;
import seep.operator.State;
import seep.runtimeengine.CommunicationChannel;
import seep.runtimeengine.CoreRE;
import seep.runtimeengine.OutputQueue;

public class ProcessingUnit {

	private CoreRE owner = null;
	private PUContext ctx = null;
	private SystemStatus systemStatus = SystemStatus.NORMAL;

	//Operators managed by this processing unit [ opId<Integer> - op<Operator> ]
	static public Map<Integer, Operator> mapOP_ID = new HashMap<Integer, Operator>();
	//Map between operator id and state [opId<Integer> - State]
	private Map<Integer, State> mapOP_S = new HashMap<Integer, State>();
	// lockState arbiters the access to the state between operators and processingUnit. 0-> free, 1->operator, 2->pu
	private AtomicInteger lockState = new AtomicInteger(0);
	private Operator mostUpstream = null;
	
	private OutputQueue outputQueue = null;
	
	private StateBackupWorker sbw;
	private Thread stateWorker;
	private ArrayList<Integer> listOfManagedStates = new ArrayList<Integer>();
	
	public ProcessingUnit(CoreRE owner){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr());
	}
	
	public Operator getMostUpstream(){
		return mostUpstream;
	}
	
	public SystemStatus getSystemStatus(){
		return systemStatus;
	}
	
	public void setSystemStatus(SystemStatus systemStatus){
		this.systemStatus = systemStatus;
	}
	
	//This enum is for aiding in the implementation of the protocols
	public enum SystemStatus {
		NORMAL, WAITING_FOR_STATE_ACK, INITIALISING_STATE//, REPLAYING_BUFFER//, RECONFIGURING_COMM
	}
	
	/** SETUP methods **/
	
	public void setOutputQueue(OutputQueue outputQueue){
		this.outputQueue = outputQueue;
	}
	
	public void newOperatorInstantiation(Operator o) {
		//Detect the first submitted operator
		if(mapOP_ID.isEmpty()){
			mostUpstream = o;
		}
		o.setProcessingUnit(this);
		mapOP_ID.put(o.getOperatorId(), o);
		//If the operator is stateful, we extract its state and store it in the provisioned map
		if(o instanceof StatefulOperator){
			mapOP_S.put(o.getOperatorId(), o.getState());
		}
	}

	public void setOpReady(int opId) {
		mapOP_ID.get(opId).setReady(true);
	}
	
	public PUContext setUpProcessingUnit(){
		//Create connections between operators
		ArrayList<Operator> operatorSet = (ArrayList<Operator>) mapOP_ID.values();
		ctx.configureOperatorConnections(operatorSet);
		//Create and configure routers
		for(Operator op : operatorSet){
			// Initialize and set the routing information
			String queryFunction = op.getOpContext().getQueryFunction();
			HashMap<Integer, ArrayList<Integer>> routeInfo = op.getOpContext().getRouteInfo();
			Router r = new Router(queryFunction, routeInfo);
			// Configure routing implementations of the operator
			r.configureRoutingImpl(op.getOpContext());
			op.setRouter(r);
		}
		//Create and run state backup worker in case this is a stateful machine
		// FIXME pick the checkpoint interval properly
		int checkpointInterval = 0;
		sbw = new StateBackupWorker(this, mapOP_S, checkpointInterval);
		stateWorker = new Thread(sbw);
		stateWorker.start();
		return ctx;
	} 
	
	/** Runtime methods **/
	
	public void processData(DataTuple data){
		// Try to acquire the lock for processing data
		while(!lockState.compareAndSet(0,1)){
			// If not successfull wait till is available
			synchronized(lockState){
				try{
					lockState.wait();
				}
				catch(InterruptedException ie){
					ie.printStackTrace();
				}
			}
		}
		// If successful process the data
		// TODO: Adjust timestamp of state
		mostUpstream.processData(data);
		//Set the lock free again
		lockState.set(0);
		lockState.notify();
	}

	public void sendData(DataTuple dt, ArrayList<Integer> targets){
		for(Integer target : targets){
			try{
//			System.out.println("TARGET: "+target.toString());
				EndPoint dest = ctx.getDownstreamTypeConnection().elementAt(target);
				// REMOTE
				if(dest instanceof CommunicationChannel){
					///\fixme{do some proper thing with var now}
					boolean now = false;
					outputQueue.sendToDownstream(dt, dest, now, false);
				}
				// LOCAL
				else if(dest instanceof Operator){
					Operator operatorObj = (Operator) dest;
					operatorObj.processData(dt);
				}
			}
			catch(ArrayIndexOutOfBoundsException aioobe){
				System.out.println("Targets size: "+targets.size()+" Target-Index: "+target+" downstreamSize: "+ctx.getDownstreamTypeConnection().size());
				aioobe.printStackTrace();
			}
		}
	}
	
//	public void sendData(DataTuple dt, int value, boolean now) {
//		ArrayList<Integer> targets = router.forward(dt, value, now);
//		for(Integer target : targets){
//			try{
////			System.out.println("TARGET: "+target.toString());
//				EndPoint dest = puCtx.getDownstreamTypeConnection().elementAt(target);
//				outputQueue.sendToDownstream(dt, dest, now, false);
//			}
//			catch(ArrayIndexOutOfBoundsException aioobe){
//				System.out.println("Targets size: "+targets.size()+" Target-Index: "+target+" downstreamSize: "+puCtx.getDownstreamTypeConnection().size());
//				aioobe.printStackTrace();
//			}
//		}
//		
//		
//	}
	
	/** Operator information management **/
	
	public void reconfigureOperatorLocation(int opId, InetAddress ip){
		mapOP_ID.get(opId).getOpContext().changeLocation(opId, ip);
	}
	
	public void reconfigureOperatorConnection(int opId, InetAddress ip){
		ctx.updateConnection(opId, ip);
	}
	
	/** State Management Stuff **/
	
		/** State operations **/
	public void checkpointAndBackupState(){
		// LOCK MANAGEMENT TO ENSURE NO CONCURRENT ACCESS WITH OPERATOR CODE
	}
	
	public void backupState(State state){
		BackupState bs = new BackupState();
		bs.setOpId(state.getOwnerId());
		bs.setState(state);
		bs.setStateClass(state.getStateTag());
		//Build the ControlTuple msg
		ControlTuple ctB = new ControlTuple().makeBackupState(bs);
		//Finally send the backup state
//System.out.println("Sending BACKUP to : "+backupUpstreamIndex+" OPID: "+opContext.getUpOpIdFromIndex(backupUpstreamIndex));
		owner.sendBackupState(ctB);
//		controlDispatcher.sendUpstream(ctB, backupUpstreamIndex);
//		ack(currentTsData);
	}
	
	
		/** Who manages which state? **/
	
	public synchronized void invalidateState(int opId) {
		//If the states figures as being managed we removed it
		int index = 0;
		if((index = listOfManagedStates.indexOf(opId)) != -1) listOfManagedStates.remove(index);
		// and then we clean both the buffer and the mapping in downstreamBuffers.
		if(PUContext.downstreamBuffers.get(opId) != null){
			//First of all, we empty the buffer
			PUContext.downstreamBuffers.get(opId).replaceBackupState(null);
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
	
		/** Dynamic change of operator information **/
	
	public void addDownstream(int opId, OperatorStaticInformation location){
		OperatorContext opContext = mapOP_ID.get(opId).getOpContext();
		opContext.addDownstream(opId);
		opContext.setDownstreamOperatorStaticInformation(opId, location);
		ctx.configureNewDownstreamCommunication(opId, location);
	}
	
	public void addUpstream(int opId, OperatorStaticInformation location){
		OperatorContext opContext = mapOP_ID.get(opId).getOpContext();
		opContext.addUpstream(opId);
		opContext.setUpstreamOperatorStaticInformation(opId, location);
		ctx.configureNewUpstreamCommunication(opId, location);
	}
}
