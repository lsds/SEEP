package seep.operator;

import seep.buffer.Buffer;
import seep.infrastructure.*;

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

@SuppressWarnings("serial")
public class OperatorContext implements Serializable {

	private OperatorStaticInformation location;
	private ArrayList<Integer> listOfManagedStates = new ArrayList<Integer>();
	
	private ArrayList<Integer> connectionsD = new ArrayList<Integer>();
	private ArrayList<Integer> connectionsU = new ArrayList<Integer>();

	private ArrayList<OperatorStaticInformation> upstream = new ArrayList<OperatorStaticInformation>();
	private ArrayList<OperatorStaticInformation> downstream = new ArrayList<OperatorStaticInformation>();

	//map in charge of storing the buffers that this operator is using
	static public Map<Integer, Buffer> downstreamBuffers = new HashMap<Integer, Buffer>();
	/// \todo{the signature of this attribute must change to the one written below}
	//private HashMap<Integer, Buffer> downstreamBuffers = new HashMap<Integer, Buffer>();
	
	//These structures are Vector because they are potentially accessed from more than one point at a time
	/// \todo {refactor this to a synchronized map??}
	private Vector downstreamTypeConnection = null;
	private Vector upstreamTypeConnection = null;
	
	public Vector getDownstreamTypeConnection() {
		return downstreamTypeConnection;
	}

	public Vector getUpstreamTypeConnection() {
		return upstreamTypeConnection;
	}
	
	public OperatorContext(ArrayList<Integer> connectionsD, ArrayList<Integer> connectionsU, ArrayList<OperatorStaticInformation> upstream, ArrayList<OperatorStaticInformation> downstream){
		this.connectionsD = connectionsD;
		this.connectionsU = connectionsU;
		this.upstream = upstream;
		this.downstream = downstream;
	}

	public OperatorStaticInformation getOperatorStaticInformation(){
		return location;
	}

	public void setOperatorStaticInformation(OperatorStaticInformation location){
		this.location = location;
	}
	
	public void addDownstream(int opID) {
		connectionsD.add(opID);
	}
	
	public int getDownstreamSize() { 
		return downstream.size(); 
	}
	
	public void addUpstream(int opID) {
		connectionsU.add(opID);
	}
	
	public OperatorStaticInformation getDownstreamLocation(int opID) {
		return downstream.get(findOpIndex(opID, connectionsD));
	}
	
	public OperatorStaticInformation getUpstreamLocation(int opID) {
		return upstream.get(findOpIndex(opID, connectionsU));
	}
	
	public void setDownstreamOperatorStaticInformation(int opID, OperatorStaticInformation loc) {
		addOrReplace(opID, loc, connectionsD, downstream);
	}

	//Add if it is a new upstrea, replace if it is a previous upstream that failer or that have changed node for any other reason
	public void setUpstreamOperatorStaticInformation(int opID, OperatorStaticInformation loc) {
		addOrReplace(opID, loc, connectionsU, upstream);
	}

	private void addOrReplace(int opID, OperatorStaticInformation loc, ArrayList<Integer> IDList, ArrayList<OperatorStaticInformation> LocationList) {
		int opIndex = findOpIndex(opID, IDList);
		if (opIndex<LocationList.size())
			LocationList.set(opIndex, loc);
		else {
			for (int index = LocationList.size(); index < opIndex; index++) {
				LocationList.add(null);
			}
			LocationList.add(loc);
		}
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
	
	private int findOpIndex(int opID, ArrayList<Integer> IDList) {
		for(Integer id : IDList){
			if(id == opID){
				return IDList.indexOf(id);
			}
		}
		throw new IllegalArgumentException("opID "+ opID + " not found");
	}

	//TODO place opID in location instead of this?
	public static class PlacedOperator {
		int index;
		ArrayList<Integer> conns;
		ArrayList<OperatorStaticInformation> locs;
		
		public PlacedOperator(int index, ArrayList<Integer> conns, ArrayList<OperatorStaticInformation> locs) {
			this.index = index;
			this.conns = conns;
			this.locs = locs;
		}
		
		public OperatorStaticInformation location() { return locs.get(index); }
		public int opID() {  return conns.get(index); }
		public int index() { return index; }
	};

	
	public class DownIter implements Iterable<PlacedOperator>, Serializable {
		public Iterator<PlacedOperator> iterator() { return new Iter(connectionsD,downstream); }
		public int size() { return connectionsD.size(); }
	}
	public class UpIter implements Iterable<PlacedOperator>, Serializable {
		public Iterator<PlacedOperator> iterator() { return new Iter(connectionsU,upstream); }
		public int size() { return connectionsU.size(); }
	}
	
	public final DownIter downstreams = new DownIter();
	
	public final UpIter upstreams = new UpIter();
	
	static class Iter implements Iterator<PlacedOperator> {

		int index = -1;
		ArrayList<Integer> conns;
		ArrayList<OperatorStaticInformation> locs;

		private Iter(ArrayList<Integer> conns, ArrayList<OperatorStaticInformation> locs) { this.conns = conns; this.locs = locs; }
		public boolean hasNext() { return index<conns.size()-1; }

		public PlacedOperator next() { index++; return new PlacedOperator(index, conns, locs); }

		public void remove() { throw new UnsupportedOperationException(); }
	}

	public OperatorContext(){
	}
	
	@Override 
	public String toString() {
		StringBuffer buf = new StringBuffer("@"+ getOperatorStaticInformation());
		if (downstream.size()>0) {
			buf.append(" down: [");
			for (PlacedOperator op : downstreams) {
				buf.append("Id: "+op.opID() + op.location());
				System.out.println();
			}
			buf.append("]");
		}
		if (upstream.size()>0) {
			buf.append(" up: [");
			for (PlacedOperator op : upstreams) {
				buf.append("Id: "+op.opID() + op.location());
				System.out.println();
			}
			buf.append("]");
		}
		return buf.toString();
	}

	//this method and the following one do not support changing the node with
	//a new node with a different port, however this is not a problem for operators,
	//that do not need to contact the secondary but just need ports for operators.
	//the slight problem is that if the new node has a different port this would not
	//be reflected in the node object, which might be confusing for debugging.
	public void changeLocation(InetAddress oldIp, InetAddress newIp){
		for(int i = 0; i < upstream.size(); i++){
			if(upstream.get(i).getMyNode().getIp().equals(oldIp)){
				Node newNode = upstream.get(i).getMyNode().setIp(newIp);
				OperatorStaticInformation newLoc = upstream.get(i).setNode(newNode);
				upstream.set(i, newLoc);
			}
		}
		for(int i = 0; i < downstream.size(); i++){
			if(downstream.get(i).getMyNode().getIp().equals(oldIp)){
				Node newNode = downstream.get(i).getMyNode().setIp(newIp);
				OperatorStaticInformation newLoc = downstream.get(i).setNode(newNode);
				downstream.set(i, newLoc);
			}
		}
	}

	public void changeLocation(int opId, InetAddress newIp){
		for(int i = 0; i < upstream.size(); i++){
			if((upstream.get(i).getInD() - 40000) == opId){
				Node newNode = upstream.get(i).getMyNode().setIp(newIp);
				OperatorStaticInformation newLoc = upstream.get(i).setNode(newNode);
				upstream.set(i, newLoc);
			}
		}
		for(int i = 0; i < downstream.size(); i++){
			if((downstream.get(i).getInD() - 40000) == opId){
				Node newNode = downstream.get(i).getMyNode().setIp(newIp);
				OperatorStaticInformation newLoc = downstream.get(i).setNode(newNode);
				downstream.set(i, newLoc);
			}
		}
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
	
	public void configureCommunication() {
		
		downstreamTypeConnection = new Vector();
		upstreamTypeConnection = new Vector();
		//Gather nature of downstream operators, i.e. local or remote
		for(PlacedOperator down: downstreams){
			configureNewDownstreamCommunication(down.opID(),down.location());
		}
		for(PlacedOperator up: upstreams){
			configureNewUpstreamCommunication(up.opID(),up.location());
		}
	}
	
	public void configureNewUpstreamCommunication(int opID, OperatorStaticInformation loc) {
		InetAddress localIp = location.getMyNode().getIp();
		if(loc.getMyNode().getIp().equals(localIp)){
			if(NodeManager.mapOP_ID.containsKey(opID)){
				//Store reference in upstreamTypeConnection, store operator(local) or socket(remote)
				upstreamTypeConnection.add(NodeManager.mapOP_ID.get(opID));
				NodeManager.nLogger.info("-> OperatorContext. New local upstream conn to OP-"+opID);
			}
		}
		//remote
		else if (!(loc.getMyNode().getIp().equals(localIp))){
			createRemoteCommunication(loc.getMyNode().getIp(), 0, loc.getInC(), "up");
			NodeManager.nLogger.info("-> OperatorContext. New remote upstream conn to OP-"+opID);
		}
	}

	public void configureNewDownstreamCommunication(int opID, OperatorStaticInformation loc) {
		InetAddress localIp = location.getMyNode().getIp();
		//Check if downstream node is remote or local, and check that it is not a Sink
		if(loc.getMyNode().getIp().equals(localIp)){
			//Access downstream reference in map with op_id
			if (NodeManager.mapOP_ID.containsKey(opID)) {
				//Store reference in downstreamTypeConnection, store operator(local) or socket(remote)
				downstreamTypeConnection.add(NodeManager.mapOP_ID.get(opID));
				NodeManager.nLogger.info("-> OperatorContext. New local downstream conn to OP-"+opID);
			}
		}
		else if(!(loc.getMyNode().getIp().equals(localIp))){
			//If remote, create communication with other point
			createRemoteCommunication(loc.getMyNode().getIp(), loc.getInD(), loc.getInC(), "down");
			NodeManager.nLogger.info("-> OperatorContext. New remote downstream conn to OP-"+opID);
		}
	}
	
	private void createRemoteCommunication(InetAddress ip, int portD, int portC, String type){
		Socket socketD = null;
		Socket socketC = null;
		try{
			if(type.equals("down")){
				socketD = new Socket(ip, portD);
				if(portC != 0){
					socketC = new Socket(ip, portC);
				}
				Buffer buffer = new Buffer();
				downstreamTypeConnection.add(new CommunicationChannel(socketD, socketC, buffer));
/// \todo{here a 40000 is used, change this line to make it properly}
				downstreamBuffers.put((portD-40000), buffer);
			}
			else if(type.equals("up")){
				socketC = new Socket(ip, portC);
				upstreamTypeConnection.add(new CommunicationChannel(null, socketC, null));
			}
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> OperatorContext. While establishing remote connection "+io.getMessage());
			io.printStackTrace();
		}
	}
	
	public CommunicationChannel getCCIfromOpId(int opId, String type){
		if(type.equals("d")){
			for(PlacedOperator down: downstreams){
				if(down.opID() == opId){
					if( downstreamTypeConnection.elementAt(down.index()) instanceof CommunicationChannel){
						return (CommunicationChannel)downstreamTypeConnection.elementAt(down.index());
					}
				}
			}
		}
		else if(type.equals("u")){
			for(PlacedOperator up: downstreams){
				if(up.opID() == opId){
					if(upstreamTypeConnection.elementAt(up.index()) instanceof CommunicationChannel){
						return (CommunicationChannel)upstreamTypeConnection.elementAt(up.index());
					}	
				}
			}
		}
		return null;
	}

	public Buffer getBuffer(int opId) {
		return downstreamBuffers.get(opId);
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
