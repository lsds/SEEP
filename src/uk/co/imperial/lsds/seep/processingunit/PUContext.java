/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.co.imperial.lsds.seep.processingunit;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import uk.co.imperial.lsds.seep.P;
import uk.co.imperial.lsds.seep.buffer.Buffer;
import uk.co.imperial.lsds.seep.infrastructure.NodeManager;
import uk.co.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.co.imperial.lsds.seep.operator.EndPoint;
import uk.co.imperial.lsds.seep.operator.Operator;
import uk.co.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.co.imperial.lsds.seep.operator.OperatorContext.PlacedOperator;
import uk.co.imperial.lsds.seep.runtimeengine.AsynchronousCommunicationChannel;
import uk.co.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;

import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Output;


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
	
	public PUContext(WorkerNodeDescription nodeDescr){
		this.nodeDescr = nodeDescr;
		try {
			this.selector = SelectorProvider.provider().openSelector();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public void configureOperatorConnections(Operator op) {
		
		downstreamTypeConnection = new Vector<EndPoint>();
		upstreamTypeConnection = new Vector<EndPoint>();
		configureDownstreamAndUpstreamConnections(op);	
	}
	
	/**
	 * This function creates a (always) synchronous communication channel with the specified upstream operator
	 * @param opID
	 * @param loc
	 */
	public void configureNewUpstreamCommunication(int opID, OperatorStaticInformation loc) {
		InetAddress localIp = nodeDescr.getIp();
//		if(loc.getMyNode().getIp().equals(localIp)){
//			if(StatefulProcessingUnit.mapOP_ID.containsKey(opID)){
//				//Store reference in upstreamTypeConnection, store operator(local) or socket(remote)
//				upstreamTypeConnection.add(StatefulProcessingUnit.mapOP_ID.get(opID));
//				NodeManager.nLogger.info("-> PUContext. New local upstream conn to OP-"+opID);
//			}
//		}
		//remote
//		else 
		if (!(loc.getMyNode().getIp().equals(localIp))){
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
//		//Check if downstream node is remote or local, and check that it is not a Sink
//		if(loc.getMyNode().getIp().equals(localIp)){
//			//Access downstream reference in map with op_id
//			if (StatefulProcessingUnit.mapOP_ID.containsKey(opID)) {
//				//Store reference in downstreamTypeConnection, store operator(local) or socket(remote)
//				downstreamTypeConnection.add(StatefulProcessingUnit.mapOP_ID.get(opID));
//				NodeManager.nLogger.info("-> PUContext. New local downstream conn to OP-"+opID);
//			}
//		}
//		else 
		if(!(loc.getMyNode().getIp().equals(localIp))){
			//If remote, create communication with other point			
			if (P.valueFor("synchronousOutput").equals("true")){
				
				createRemoteSynchronousCommunication(opID, loc.getMyNode().getIp(), loc.getInD(), loc.getInC(), "down");
				NodeManager.nLogger.info("-> PUContext. New remote downstream (SYNC) conn to OP-"+opID);
			}
			else{
				createRemoteAsynchronousCommunication(opID, loc.getMyNode().getIp(), loc.getInD());
				NodeManager.nLogger.info("-> PUContext. New remote downstream (ASYNC) conn to OP-"+opID);
			}
			
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
			
			ByteBuffer nativeBuffer = ByteBuffer.allocateDirect(20000);
			ByteBufferOutputStream bbos = new ByteBufferOutputStream(nativeBuffer);
//			ByteBufferOutputStream bbos = new ByteBufferOutputStream(16);
			
			Output o = new Output(bbos);
			// finally we register this socket to the selector for the async behaviour, and we link nativeBuffer, for the selector to access it directly
//			SelectionKey key = socketChannel.register(selector, SelectionKey.OP_WRITE, nativeBuffer);
			
			//Finally create the metadata structure associated to this connection
			Buffer buf = new Buffer();
			AsynchronousCommunicationChannel acc = new AsynchronousCommunicationChannel(opId, buf, o, nativeBuffer);
			acc.setSelector(selector);
			
			SelectionKey key = socketChannel.register(selector, SelectionKey.OP_WRITE, acc);
			// To make sure conn is established...
			try {
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			boolean connSuccess = socketChannel.finishConnect();
			if(!connSuccess){
				NodeManager.nLogger.severe("Failed connection to: "+key.toString());
				System.exit(0);
			}
			
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
		Socket socketBlind = null;
		int blindPort = new Integer(P.valueFor("blindSocket"));
		
		try{
			if(type.equals("down")){
				NodeManager.nLogger.info("-> Trying remote downstream conn to: "+ip.toString()+"/"+portD);
				socketD = new Socket(ip, portD);
				if(portC != 0){
					socketC = new Socket(ip, portC);
				}
				
				Buffer buffer = new Buffer();
				
				SynchronousCommunicationChannel con = new SynchronousCommunicationChannel(opID, socketD, socketC, socketBlind, buffer);
				downstreamTypeConnection.add(con);
				remoteDownstream.add(con);
/// \todo{here a 40000 is used, change this line to make it properly}
				downstreamBuffers.put((portD-40000), buffer);
			}
			else if(type.equals("up")){
				NodeManager.nLogger.info("-> Trying remote upstream conn to: "+ip.toString()+"/"+portC);
				socketC = new Socket(ip, portC);
				socketBlind = new Socket(ip, blindPort);
				SynchronousCommunicationChannel con = new SynchronousCommunicationChannel(opID, null, socketC, socketBlind, null);
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
	
//	public void printDownstreamTypeConnection(){
//		for(EndPoint ep : downstreamTypeConnection){
//			System.out.println("OP: "+ep.getOperatorId());
//		}
//	}
	
	public Buffer getBuffer(int opId) {
		return downstreamBuffers.get(opId);
	}
	
	/** Dynamic Reconfiguration **/
	
	public void updateConnection(int opRecId, Operator opToReconfigure, InetAddress newIp){
		int opId = opRecId;
		int dataPort = opToReconfigure.getOpContext().findDownstream(opId).location().getInD();
		int controlPort = opToReconfigure.getOpContext().findDownstream(opId).location().getInC();

		int blindPort = new Integer(P.valueFor("blindSocket"));
	
		for(EndPoint ep : downstreamTypeConnection){
			if(ep.getOperatorId() == opId){
				try{
					Socket dataS = new Socket(newIp, dataPort);
					Socket controlS = new Socket(newIp, controlPort);
					Socket blindS = null;
					Buffer buf = downstreamBuffers.get(opId);
					int index = opToReconfigure.getOpContext().getDownOpIndexFromOpId(opId);
					SynchronousCommunicationChannel cci = new SynchronousCommunicationChannel(opId, dataS, controlS, blindS, buf);
					downstreamTypeConnection.set(index, cci);
				}
				catch(IOException io){
					System.out.println("While re-creating DOWNSTREAM socket: "+io.getMessage());
				}
			}
		}
		for(EndPoint ep : upstreamTypeConnection){
			if(ep.getOperatorId() == opId){
				try{
					Socket controlS = new Socket(newIp, controlPort);
					Socket blindS = new Socket(newIp, blindPort);
					int index = opToReconfigure.getOpContext().getUpOpIndexFromOpId(opId);
					SynchronousCommunicationChannel cci = new SynchronousCommunicationChannel(opId, null, controlS, blindS, null);
					upstreamTypeConnection.set(index, cci);
				}
				catch(IOException io){
					System.out.println("While re-creating UPSTREAM socket: "+io.getMessage());
				}
			}
		}
		NodeManager.nLogger.info("-> PUContext. Conns of OP-"+opId+" updated");
	}
}
