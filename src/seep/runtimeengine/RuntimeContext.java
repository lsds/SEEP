package seep.runtimeengine;

import seep.buffer.Buffer;
import seep.infrastructure.*;
import seep.operator.OperatorStaticInformation;
import seep.processingunit.ProcessingUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;

/**
* OperatorContext. This object is in charge of model information associated to a given operator, as its connections or its location.
*/

public class RuntimeContext implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private OperatorStaticInformation location;
	private ArrayList<Integer> listOfManagedStates = new ArrayList<Integer>();
	
	
	
	
	
	
	
	public RuntimeContext(ArrayList<Integer> connectionsD, ArrayList<Integer> connectionsU, ArrayList<OperatorStaticInformation> upstream, ArrayList<OperatorStaticInformation> downstream){
		this.connectionsD = connectionsD;
		this.connectionsU = connectionsU;
		this.upstream = upstream;
		this.downstream = downstream;
	}
	
	public int getDownstreamSize() { 
		return downstream.size(); 
	}
	
	public OperatorStaticInformation getDownstreamLocation(int opID) {
		return downstream.get(findOpIndex(opID, connectionsD));
	}
	
	public OperatorStaticInformation getUpstreamLocation(int opID) {
		return upstream.get(findOpIndex(opID, connectionsU));
	}
	
	public PlacedOperator findUpstream(int opId){
		for (PlacedOperator op : upstreams) {
			if (op.opID() == opId) return op;
		}
		return null;
	}
	
	public PlacedOperator findDownstream(int opID) {
		for (PlacedOperator op : downstreams) {
			if (op.opID() == opID) return op;
		}
		return null;
	}

	public RuntimeContext(){
	}

	
	
	public PlacedOperator minimumUpstream() {
		PlacedOperator min = null;
		for (PlacedOperator op : upstreams) {
			if (min==null) { min = op; }
			else if (min.opID() > op.opID()) { min = op; }
		}
		return min;
	}

	public void updateConnection(int opId, InetAddress newIp){
		InetAddress localIp = location.getMyNode().getIp();
	
		for(PlacedOperator down: downstreams){
			if(down.opID() == opId){
				if(down.location().getMyNode().getIp().equals(localIp)){
					System.out.println("PROBLEM HERE!!!!!!");
/// \todo {when introducing local operators there is a whole new bunch of possibilities to handle}
					//This means that a previous remote operator is now local, so, previous operator buffers should be replayed
					//destroyed, and this new operator should create the new buffer for their downstream connection.
				}
				else{
					try{
						Socket dataS = new Socket(newIp, down.location().getInD());
						Socket controlS = new Socket(newIp, down.location().getInC());
						Buffer buf = downstreamBuffers.get(opId);

						CommunicationChannel cci = new CommunicationChannel(dataS, controlS, buf);
						downstreamTypeConnection.set(down.index(), cci);
					}
					catch(IOException io){
						System.out.println("While re-creating socket: "+io.getMessage());
					}
				}
			}
		}
		for(PlacedOperator up: upstreams){
			if(up.opID() == opId){
				if(up.location().getMyNode().getIp().equals(localIp)){
					System.out.println("PROBLEM HERE!!!!!!");
					//This means that a previous remote operator is now local, so, previous operator buffers should be replayed
					//destroyed, and this new operator should create the new buffer for their downstream connection. 
/// \todo {when introducing local operators there is a whole new bunch of possibilities to handle}
				}
				else{
					try{
						Socket controlS = new Socket(newIp, up.location().getInC());
						CommunicationChannel cci = new CommunicationChannel(null, controlS, null);
						upstreamTypeConnection.set(up.index(), cci);
					}
					catch(IOException io){
						System.out.println("While re-creating socket: "+io.getMessage());
					}
				}
				
			}
			//FIXME this should dissapear...
/// \test {Work without this -2}
//			else if (up.opID() == -2){
//				//Sources are always remote
//				createRemoteCommunication(up.location().getMyNode().getIp(), 0, up.location().getInC(), "up");
//			}
		}
		NodeManager.nLogger.info("-> OperatorContext. Conns of OP-"+opId+" updated");
	}
	
	
	
	

	
	
	public boolean isManagingStateOf(int opId) {
//		if(downstreamBuffers.get(opId) != null) return true;
		if(listOfManagedStates.contains(opId)) return true;
		return false;
	}
	
	public synchronized void registerManagedState(int opId) {
		//If the state does not figure as being managed, we include it
		if(!listOfManagedStates.contains(opId)){
			NodeManager.nLogger.info("-> New STATE registered for OP: "+opId);
			listOfManagedStates.add(opId);
		}
	}
	
	public synchronized void invalidateState(int opId) {
		//If the states figures as being managed we removed it
		int index = 0;
		if((index = listOfManagedStates.indexOf(opId)) != -1) listOfManagedStates.remove(index);
		// and then we clean both the buffer and the mapping in downstreamBuffers.
		if(downstreamBuffers.get(opId) != null){
			//First of all, we empty the buffer
			downstreamBuffers.get(opId).replaceBackupState(null);
		}
	}

	public int getDownOpIdFromIndex(int index){
		int opId = 0;
		for(PlacedOperator down : downstreams){
			if(down.index() == index){
				return down.opID();
			}
		}
		return opId;
	}
	
	public int getUpOpIdFromIndex(int index){
		int opId = 0;
		for(PlacedOperator up : upstreams){
			if(up.index() == index){
				return up.opID();
			}
		}
		return opId;
	}
	
	public ArrayList<Integer> getListOfDownstreamIndexes() {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for(PlacedOperator down : downstreams){
			indexes.add(down.index());
		}
		return indexes;
	}

	//Check if the given opId is statefull
	public boolean isDownstreamOperatorStateful(int opId) {
		for(PlacedOperator op : downstreams){
			if(op.opID() == opId){
				if(op.location().isStatefull()){
					return true;
				}
				else{
					return false;
				}
			}
		}
		return false;
	}
	
	//Check if all downstreamms are stateful
	public boolean isDownstreamStateful() {
		for(PlacedOperator op : downstreams){
			if(!(op.location().isStatefull())){
				return false;
			}
		}
		return true;
	}
}
