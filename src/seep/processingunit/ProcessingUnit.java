package seep.processingunit;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import seep.comm.routing.Router;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.NodeManager;
import seep.operator.EndPoint;
import seep.operator.Operator;
import seep.operator.OperatorContext;
import seep.operator.OperatorStaticInformation;
import seep.runtimeengine.CommunicationChannel;
import seep.runtimeengine.CoreRE;
import seep.runtimeengine.OutputQueue;


public class ProcessingUnit {

	private CoreRE owner = null;
	private PUContext ctx = null;
	//Operators managed by this processing unit [ opId<Integer> - op<Operator> ]
	static public Map<Integer, Operator> mapOP_ID = new HashMap<Integer, Operator>();
	//Map between operator id and dispatcher [opId<Integer> - Dispatcher]
	private Operator mostUpstream = null;
	
	private OutputQueue outputQueue = null;
	
	private ArrayList<Integer> listOfManagedStates = new ArrayList<Integer>();
	
	public ProcessingUnit(CoreRE owner){
		this.owner = owner;
		ctx = new PUContext(owner.getNodeDescr());
	}
	
	public Operator getMostUpstream(){
		return mostUpstream;
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
		return ctx;
	}
	
	/** Runtime methods **/
	
	public void processData(DataTuple data){
		mostUpstream.processData(data);
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
