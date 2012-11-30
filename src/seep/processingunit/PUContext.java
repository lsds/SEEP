package seep.processingunit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import seep.buffer.Buffer;
import seep.infrastructure.NodeManager;
import seep.infrastructure.WorkerNodeDescription;
import seep.operator.EndPoint;
import seep.operator.Operator;
import seep.operator.OperatorStaticInformation;
import seep.operator.OperatorContext.PlacedOperator;
import seep.runtimeengine.CommunicationChannel;


public class PUContext {

	private WorkerNodeDescription nodeDescr = null;
	
	private ArrayList<EndPoint> remoteUpstream = new ArrayList<EndPoint>();
	private ArrayList<EndPoint> remoteDownstream = new ArrayList<EndPoint>();
	//These structures are Vector because they are potentially accessed from more than one point at a time
	/// \todo {refactor this to a synchronized map??}
	private Vector<EndPoint> downstreamTypeConnection = null;
	private Vector<EndPoint> upstreamTypeConnection = null;
	
	//map in charge of storing the buffers that this operator is using
	/// \todo{the signature of this attribute must change to the one written below}
	//private HashMap<Integer, Buffer> downstreamBuffers = new HashMap<Integer, Buffer>();
	static public Map<Integer, Buffer> downstreamBuffers = new HashMap<Integer, Buffer>();
	
	
	public PUContext(WorkerNodeDescription nodeDescr){
		this.nodeDescr = nodeDescr;
	}
	
	public Vector<EndPoint> getDownstreamTypeConnection() {
		return downstreamTypeConnection;
	}
	
	public Vector<EndPoint> getUpstreamTypeConnection() {
		return upstreamTypeConnection;
	}
	
	public void configureDownstreamAndUpstreamConnections(Operator op){
		//Gather nature of downstream operators, i.e. local or remote
		for(PlacedOperator down: op.getOpContext().downstreams){
			configureNewDownstreamCommunication(down.opID(),down.location());
		}
		for(PlacedOperator up: op.getOpContext().upstreams){
			configureNewUpstreamCommunication(up.opID(),up.location());
		}
	}
	
	public void configureOperatorConnections(ArrayList<Operator> operatorSet) {
		
		downstreamTypeConnection = new Vector<EndPoint>();
		upstreamTypeConnection = new Vector<EndPoint>();
		
		for(Operator op : operatorSet){
			configureDownstreamAndUpstreamConnections(op);
		}	
	}
	
	public void configureNewUpstreamCommunication(int opID, OperatorStaticInformation loc) {
		InetAddress localIp = nodeDescr.getIp();
		if(loc.getMyNode().getIp().equals(localIp)){
			if(ProcessingUnit.mapOP_ID.containsKey(opID)){
				//Store reference in upstreamTypeConnection, store operator(local) or socket(remote)
				upstreamTypeConnection.add(ProcessingUnit.mapOP_ID.get(opID));
				NodeManager.nLogger.info("-> OperatorContext. New local upstream conn to OP-"+opID);
			}
		}
		//remote
		else if (!(loc.getMyNode().getIp().equals(localIp))){
			createRemoteCommunication(opID, loc.getMyNode().getIp(), 0, loc.getInC(), "up");
			NodeManager.nLogger.info("-> OperatorContext. New remote upstream conn to OP-"+opID);
		}
	}

	public void configureNewDownstreamCommunication(int opID, OperatorStaticInformation loc) {
		InetAddress localIp = nodeDescr.getIp();
		//Check if downstream node is remote or local, and check that it is not a Sink
		if(loc.getMyNode().getIp().equals(localIp)){
			//Access downstream reference in map with op_id
			if (ProcessingUnit.mapOP_ID.containsKey(opID)) {
				//Store reference in downstreamTypeConnection, store operator(local) or socket(remote)
				downstreamTypeConnection.add(ProcessingUnit.mapOP_ID.get(opID));
				NodeManager.nLogger.info("-> OperatorContext. New local downstream conn to OP-"+opID);
			}
		}
		else if(!(loc.getMyNode().getIp().equals(localIp))){
			//If remote, create communication with other point
			createRemoteCommunication(opID, loc.getMyNode().getIp(), loc.getInD(), loc.getInC(), "down");
			NodeManager.nLogger.info("-> OperatorContext. New remote downstream conn to OP-"+opID);
		}
	}
	
	private void createRemoteCommunication(int opID, InetAddress ip, int portD, int portC, String type){
		Socket socketD = null;
		Socket socketC = null;
		try{
			if(type.equals("down")){
				socketD = new Socket(ip, portD);
				if(portC != 0){
					socketC = new Socket(ip, portC);
				}
				Buffer buffer = new Buffer();
				CommunicationChannel con = new CommunicationChannel(opID, socketD, socketC, buffer);
				downstreamTypeConnection.add(con);
				remoteDownstream.add(con);
/// \todo{here a 40000 is used, change this line to make it properly}
				downstreamBuffers.put((portD-40000), buffer);
			}
			else if(type.equals("up")){
				socketC = new Socket(ip, portC);
				CommunicationChannel con = new CommunicationChannel(opID, null, socketC, null);
				upstreamTypeConnection.add(con);
				remoteUpstream.add(con);
			}
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> OperatorContext. While establishing remote connection "+io.getMessage());
			io.printStackTrace();
		}
	}
	
//	public CommunicationChannel getCCIfromOpId(int opId, String type){
//		if(type.equals("d")){
//			for(PlacedOperator down: downstreams){
//				if(down.opID() == opId){
//					if(downstreamTypeConnection.elementAt(down.index()) instanceof CommunicationChannel){
//						return (CommunicationChannel)downstreamTypeConnection.elementAt(down.index());
//					}
//				}
//			}
//		}
//		else if(type.equals("u")){
//			for(PlacedOperator up: downstreams){
//				if(up.opID() == opId){
//					if(upstreamTypeConnection.elementAt(up.index()) instanceof CommunicationChannel){
//						return (CommunicationChannel)upstreamTypeConnection.elementAt(up.index());
//					}	
//				}
//			}
//		}
//		return null;
//	}
	
	public CommunicationChannel getCCIfromOpId(int opId, String type){
		if(type.equals('d')){
			for(EndPoint ep : downstreamTypeConnection){
				if(ep.getOperatorId() == opId){
					return (CommunicationChannel)ep;
				}
			}
		}
		else if(type.equals('u')){
			for(EndPoint ep : upstreamTypeConnection){
				if(ep.getOperatorId() == opId){
					return (CommunicationChannel)ep;
				}
			}
		}
		return null;
	}
	
	public Buffer getBuffer(int opId) {
		return downstreamBuffers.get(opId);
	}
}
