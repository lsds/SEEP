package seep.processingunit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Output;

import seep.buffer.Buffer;
import seep.comm.serialization.messages.BatchTuplePayload;
import seep.comm.serialization.messages.Payload;
import seep.comm.serialization.messages.TuplePayload;
import seep.comm.serialization.serializers.ArrayListSerializer;
import seep.infrastructure.NodeManager;
import seep.infrastructure.WorkerNodeDescription;
import seep.operator.EndPoint;
import seep.operator.Operator;
import seep.operator.OperatorStaticInformation;
import seep.operator.OperatorContext.PlacedOperator;
import seep.runtimeengine.AsynchronousCommunicationChannel;
import seep.runtimeengine.SynchronousCommunicationChannel;


public class PUContext {

	private WorkerNodeDescription nodeDescr = null;
	
	private ArrayList<EndPoint> remoteUpstream = new ArrayList<EndPoint>();
	private ArrayList<EndPoint> remoteDownstream = new ArrayList<EndPoint>();
	//These structures are Vector because they are potentially accessed from more than one point at a time
	/// \todo {refactor this to a synchronized map??}
	private Vector<EndPoint> downstreamTypeConnection = null;
	private Vector<EndPoint> upstreamTypeConnection = null;
	
	// Selector for asynchrony in downstream connections
	private Selector selector;
	
	//map in charge of storing the buffers that this operator is using
	/// \todo{the signature of this attribute must change to the one written below}
	//private HashMap<Integer, Buffer> downstreamBuffers = new HashMap<Integer, Buffer>();
	static public Map<Integer, Buffer> downstreamBuffers = new HashMap<Integer, Buffer>();
	
	public Kryo k;
	
	public PUContext(WorkerNodeDescription nodeDescr){
		this.k = initializeKryo();
		this.nodeDescr = nodeDescr;
		try {
			this.selector = SelectorProvider.provider().openSelector();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Kryo initializeKryo(){
		//optimize here kryo
		Kryo k = new Kryo();
		k.register(ArrayList.class, new ArrayListSerializer());
		k.register(Payload.class);
		k.register(TuplePayload.class);
		k.register(BatchTuplePayload.class);
		return k;
	}
	
	public Vector<EndPoint> getDownstreamTypeConnection() {
		return downstreamTypeConnection;
	}
	
	public Vector<EndPoint> getUpstreamTypeConnection() {
		return upstreamTypeConnection;
	}
	
	public Selector getConfiguredSelector(){
		return selector;
	}
	
	private void configureDownstreamAndUpstreamConnections(Operator op){
		//Gather nature of downstream operators, i.e. local or remote
		for(PlacedOperator down: op.getOpContext().downstreams){
			configureNewDownstreamCommunication(down.opID(),down.location());
		}
		for(PlacedOperator up: op.getOpContext().upstreams){
			configureNewUpstreamCommunication(up.opID(),up.location());
		}
	}
	
	public void configureOperatorConnections(Collection<Operator> operatorSet) {
		
		downstreamTypeConnection = new Vector<EndPoint>();
		upstreamTypeConnection = new Vector<EndPoint>();
		
//		System.out.println("operator set is: "+operatorSet.size());
//		for(Operator op : operatorSet){
//			System.out.println("Op: "+op.getId());
//		}
		
		
		for(Operator op : operatorSet){
			configureDownstreamAndUpstreamConnections(op);
		}	
	}
	
	/**
	 * This function creates a (always) synchronous communication channel with the specified upstream operator
	 * @param opID
	 * @param loc
	 */
	public void configureNewUpstreamCommunication(int opID, OperatorStaticInformation loc) {
		InetAddress localIp = nodeDescr.getIp();
		if(loc.getMyNode().getIp().equals(localIp)){
			if(ProcessingUnit.mapOP_ID.containsKey(opID)){
				//Store reference in upstreamTypeConnection, store operator(local) or socket(remote)
				upstreamTypeConnection.add(ProcessingUnit.mapOP_ID.get(opID));
				NodeManager.nLogger.info("-> PUContext. New local upstream conn to OP-"+opID);
			}
		}
		//remote
		else if (!(loc.getMyNode().getIp().equals(localIp))){
			createRemoteSynchronousCommunication(opID, loc.getMyNode().getIp(), 0, loc.getInC(), "up");
			NodeManager.nLogger.info("-> PUContext. New remote upstream (sync) conn to OP-"+opID);
		}
	}

	/**
	 * This function creates an asynchronous communication channel with the specified downstream operator
	 * @param opID
	 * @param loc
	 */
	public void configureNewDownstreamCommunication(int opID, OperatorStaticInformation loc) {
		InetAddress localIp = nodeDescr.getIp();
		//Check if downstream node is remote or local, and check that it is not a Sink
		if(loc.getMyNode().getIp().equals(localIp)){
			//Access downstream reference in map with op_id
			if (ProcessingUnit.mapOP_ID.containsKey(opID)) {
				//Store reference in downstreamTypeConnection, store operator(local) or socket(remote)
				downstreamTypeConnection.add(ProcessingUnit.mapOP_ID.get(opID));
				NodeManager.nLogger.info("-> PUContext. New local downstream conn to OP-"+opID);
			}
		}
		else if(!(loc.getMyNode().getIp().equals(localIp))){
			//If remote, create communication with other point
			createRemoteAsynchronousCommunication(opID, loc.getMyNode().getIp(), loc.getInD());
//			createRemoteSynchronousCommunication(opID, loc.getMyNode().getIp(), loc.getInD(), loc.getInC(), "down");
			NodeManager.nLogger.info("-> PUContext. New remote downstream (async) conn to OP-"+opID);
		}
	}
	
	
	private void createRemoteAsynchronousCommunication(int opId, InetAddress ip, int port){
		NodeManager.nLogger.info("-> Trying remote downstream conn to: "+ip.toString()+"/"+port);
		try {
			// Create a non-blocking socket channel
			SocketChannel socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			// establish connection
			socketChannel.connect(new InetSocketAddress(ip, port));
			// We create an output where to write serialized data (kryo stuff), and we associate a native byte buffer in a bytebufferoutputstream
			
			ByteBuffer nativeBuffer = ByteBuffer.allocate(16);
			nativeBuffer.clear();
			ByteBufferOutputStream bbos = new ByteBufferOutputStream(nativeBuffer);
			
			Output o = new Output(bbos, 16);
			// finally we register this socket to the selector for the async behaviour, and we link nativeBuffer, for the selector to access it directly
			SelectionKey key = socketChannel.register(selector, SelectionKey.OP_WRITE, nativeBuffer);
			boolean connSuccess = socketChannel.finishConnect();
			if(!connSuccess){
				NodeManager.nLogger.severe("Failed connection to: "+key.toString());
				System.exit(0);
			}
			//Finally create the metadata structure associated to this connection
			Buffer buf = new Buffer();
			AsynchronousCommunicationChannel acc = new AsynchronousCommunicationChannel(opId, buf, o, k);
			acc.setSelector(selector);
			downstreamTypeConnection.add(acc);
			remoteDownstream.add(acc);
			// Set the buffer
			downstreamBuffers.put((port-40000), buf);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void createRemoteSynchronousCommunication(int opID, InetAddress ip, int portD, int portC, String type){
		Socket socketD = null;
		Socket socketC = null;
		try{
			if(type.equals("down")){
				NodeManager.nLogger.info("-> Trying remote downstream conn to: "+ip.toString()+"/"+portD);
				socketD = new Socket(ip, portD);
				if(portC != 0){
					socketC = new Socket(ip, portC);
				}
				Buffer buffer = new Buffer();
				SynchronousCommunicationChannel con = new SynchronousCommunicationChannel(opID, socketD, socketC, buffer);
				downstreamTypeConnection.add(con);
				remoteDownstream.add(con);
/// \todo{here a 40000 is used, change this line to make it properly}
				downstreamBuffers.put((portD-40000), buffer);
			}
			else if(type.equals("up")){
				NodeManager.nLogger.info("-> Trying remote upstream conn to: "+ip.toString()+"/"+portC);
				socketC = new Socket(ip, portC);
				SynchronousCommunicationChannel con = new SynchronousCommunicationChannel(opID, null, socketC, null);
				upstreamTypeConnection.add(con);
				remoteUpstream.add(con);
			}
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> PUContext. While establishing remote connection "+io.getMessage());
			if(socketD != null){
				NodeManager.nLogger.severe("-> Data Conn to: "+socketD.toString());
			}
			else if(socketC != null){
				NodeManager.nLogger.severe("-> Control Conn to: "+socketC.toString());
			}
			else{
				NodeManager.nLogger.severe("-> Socket objects are BOTH NULL");

			}
			io.printStackTrace();
		}
	}
	
	
	public SynchronousCommunicationChannel getCCIfromOpId(int opId, String type){
		if(type.equals("d")){
			for(EndPoint ep : downstreamTypeConnection){
				if(ep.getOperatorId() == opId){
					return (SynchronousCommunicationChannel)ep;
				}
			}
		}
		else if(type.equals("u")){
			for(EndPoint ep : upstreamTypeConnection){
				if(ep.getOperatorId() == opId){
					return (SynchronousCommunicationChannel)ep;
				}
			}
		}
		return null;
	}
	
	public Buffer getBuffer(int opId) {
		return downstreamBuffers.get(opId);
	}
	
	/** Dynamic Reconfiguration **/
	
	public void updateConnection(int opId, InetAddress newIp){
		InetAddress localIp = nodeDescr.getIp();
		Operator opToReconfigure = ProcessingUnit.mapOP_ID.get(opId);
		int dataPort = opToReconfigure.getOpContext().getOperatorStaticInformation().getInD();
		int controlPort = opToReconfigure.getOpContext().getOperatorStaticInformation().getInC();
	
		for(EndPoint ep : downstreamTypeConnection){
			if(ep.getOperatorId() == opId){
				try{
					Socket dataS = new Socket(newIp, dataPort);
					Socket controlS = new Socket(newIp, controlPort);
					Buffer buf = downstreamBuffers.get(opId);
					int index = opToReconfigure.getOpContext().getDownOpIndexFromOpId(opId);
					SynchronousCommunicationChannel cci = new SynchronousCommunicationChannel(opId, dataS, controlS, buf);
					downstreamTypeConnection.set(index, cci);
				}
				catch(IOException io){
					System.out.println("While re-creating socket: "+io.getMessage());
				}
			}
		}
		for(EndPoint ep : upstreamTypeConnection){
			if(ep.getOperatorId() == opId){
				try{
					Socket controlS = new Socket(newIp, controlPort);
					int index = opToReconfigure.getOpContext().getUpOpIndexFromOpId(opId);
					SynchronousCommunicationChannel cci = new SynchronousCommunicationChannel(opId, null, controlS, null);
					upstreamTypeConnection.set(index, cci);
				}
				catch(IOException io){
					System.out.println("While re-creating socket: "+io.getMessage());
				}
			}
		}
		NodeManager.nLogger.info("-> PUContext. Conns of OP-"+opId+" updated");
	}
}