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
package uk.ac.imperial.lsds.seep.processingunit;

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
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.buffer.Buffer;
import uk.ac.imperial.lsds.seep.buffer.OutputBuffer;
import uk.ac.imperial.lsds.seep.infrastructure.WorkerNodeDescription;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.operator.OperatorContext.PlacedOperator;
import uk.ac.imperial.lsds.seep.runtimeengine.AsynchronousCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.DisposableCommunicationChannel;
import uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;

import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Output;

public class PUContext {

	final private Logger LOG = LoggerFactory.getLogger(PUContext.class);

	//	private WorkerNodeDescription nodeDescr = null;

	private final int CONTROL_SOCKET;

	private ArrayList<EndPoint> remoteUpstream = new ArrayList<EndPoint>();
	private ArrayList<EndPoint> remoteDownstream = new ArrayList<EndPoint>();
	//These structures are Vector because they are potentially accessed from more than one point at a time
	/// \todo {refactor this to a synchronized map??}
	private Vector<EndPoint> downstreamTypeConnection = null;
	private Vector<EndPoint> upstreamTypeConnection = null;

	//The structure just stores the ip adresses of those nodes in the topology ready to receive state chunks
	private ArrayList<EndPoint> starTopology = null;

	// Selector for asynchrony in downstream connections
	private Selector selector;

	//map in charge of storing the buffers that this operator is using
	private HashMap<Integer, Buffer> downstreamBuffers = new HashMap<Integer, Buffer>();

	public PUContext(WorkerNodeDescription nodeDescr, ArrayList<EndPoint> starTopology){
		this.CONTROL_SOCKET = new Integer(GLOBALS.valueFor("controlSocket")); 
		//		this.nodeDescr = nodeDescr;
		this.starTopology = starTopology;
		try {
			this.selector = SelectorProvider.provider().openSelector();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isScalingOpDirectDownstream(int opId){
		for(EndPoint ep : downstreamTypeConnection){
			if(ep.getOperatorId() == opId){
				return true;
			}
		}
		return false;
	}

	public int getStarTopologySize(){
		return starTopology.size();
	}

	public void filterStarTopology(int opId){
		for(int i = 0; i < starTopology.size(); i++){
			EndPoint ep = starTopology.get(i);
			if(ep.getOperatorId() == opId){
				starTopology.remove(i);
			}
		}
	}

	public void updateStarTopology(ArrayList<EndPoint> starTopology){
		this.starTopology = starTopology;
	}

	public DisposableCommunicationChannel getDCCfromOpIdInStarTopology(int opId){
		for(EndPoint dcc : starTopology){
			if(dcc.getOperatorId() == opId){
				return (DisposableCommunicationChannel)dcc;
			}
		}
		return null;
	}

	public ArrayList<EndPoint> getStarTopology(){
		return starTopology;
	}

	public ArrayList<OutputBuffer> getOutputBuffers(){
		ArrayList<OutputBuffer> outputBuffers = new ArrayList<OutputBuffer>();
		for(EndPoint ep : this.getDownstreamTypeConnection()){
			if(ep instanceof SynchronousCommunicationChannel){
				outputBuffers.add(new OutputBuffer(((SynchronousCommunicationChannel) ep).getBatch(), ep.getOperatorId()));
			}
		}
		return outputBuffers;
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
			LOG.info("-> configuring downstream of {}", down.opID());
			configureNewDownstreamCommunication(down.opID(), down.location());
		}
		for(PlacedOperator up: op.getOpContext().upstreams){
			LOG.info("-> configuring upstream of {}", up.opID());
			configureNewUpstreamCommunication(up.opID(), up.location());
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
		createRemoteSynchronousCommunication(opID, loc.getMyNode().getIp(), 0, loc.getInC(), "up");
		LOG.info("-> PUContext. New remote upstream (sync) conn to OP: {}", opID);
	}

	/**
	 * This function creates a synchronous communication channel with the specified downstream operator
	 * @param opID
	 * @param loc
	 */
	public void configureNewDownstreamCommunication(int opID, OperatorStaticInformation loc) {
		//If remote, create communication with other point			
		if (GLOBALS.valueFor("synchronousOutput").equals("true")){
			createRemoteSynchronousCommunication(opID, loc.getMyNode().getIp(), loc.getInD(), loc.getInC(), "down");
			LOG.info("-> New remote downstream (SYNC) conn to OP: {}", opID);
		}
		else{
			createRemoteAsynchronousCommunication(opID, loc.getMyNode().getIp(), loc.getInD());
			LOG.debug("-> New remote downstream (ASYNC) conn to OP: ", opID);
		}
	}

	private void createRemoteAsynchronousCommunication(int opId, InetAddress ip, int port){
		LOG.debug("-> Trying remote downstream conn to: {}/{}", ip.toString(), port);	
		DoAfterConnection doAfterConnection = new DoAfterConnection(opId, ip, port);
		doAfterConnection.run();
	}

	private class DoAfterConnection implements Runnable {
		int opId = 0;
		InetAddress ip = null;
		int port = 0;
		
		public DoAfterConnection(int opId, InetAddress ip, int port){
			this.opId = opId;
			this.ip = ip;
			this.port = port;
		}

		@Override
		public void run() {
			try {
				// Create a non-blocking socket channel
				SocketChannel socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(false);
				// establish connection
				socketChannel.connect(new InetSocketAddress(ip, port));
				// We create an output where to write serialized data (kryo stuff), and we associate a native byte buffer in a bytebufferoutputstream

				ByteBuffer nativeBuffer = ByteBuffer.allocateDirect(20000);
				ByteBufferOutputStream bbos = new ByteBufferOutputStream(nativeBuffer);

				Output o = new Output(bbos);
				// finally we register this socket to the selector for the async behaviour, and we link nativeBuffer, for the selector to access it directly
				//				SelectionKey key = socketChannel.register(selector, SelectionKey.OP_WRITE, nativeBuffer);

				//Finally create the metadata structure associated to this connection
				Buffer buf = new Buffer();
				AsynchronousCommunicationChannel acc = new AsynchronousCommunicationChannel(opId, buf, o, nativeBuffer);
				acc.setSelector(selector);

				SelectionKey key = socketChannel.register(selector, SelectionKey.OP_WRITE, acc);
				// To make sure conn is established...

				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				boolean connSuccess = socketChannel.finishConnect();
				if(!connSuccess){
					///\fixme{fix this}
					LOG.error("Failed connection to: "+key.toString());
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
	}

	private void createRemoteSynchronousCommunication(int opID, InetAddress ip, int portD, int portC, String type){
		Socket socketD = null;
		Socket socketC = null;

		try{
			if(type.equals("down")){
				LOG.info("-> Trying remote downstream conn to: {}/{}", ip.toString(), portD);
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
				LOG.info("-> Trying remote upstream conn to: {}/{}", ip.toString(), portC);
				socketC = new Socket(ip, portC);
				//				socketBlind = new Socket(ip, blindPort);
				SynchronousCommunicationChannel con = new SynchronousCommunicationChannel(opID, null, socketC,null);
				upstreamTypeConnection.add(con);
				remoteUpstream.add(con);
			}
		}
		catch(IOException io){
			LOG.error("-> PUContext. While establishing remote connection "+io.getMessage());
			if(socketD != null){
				LOG.error("-> Data Conn to: "+socketD.toString());
			}
			else if(socketC != null){
				LOG.error("-> Control Conn to: "+socketC.toString());
			}
			else{
				LOG.error("-> Socket objects are BOTH NULL");

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

	public void updateConnection(int opRecId, Operator opToReconfigure, InetAddress newIp){
		int opId = opRecId;
		int dataPort = 0;
		int controlPort = 0;

		if(opToReconfigure.getOpContext().downstreams.size() > 0){
			dataPort = opToReconfigure.getOpContext().findDownstream(opId).location().getInD();
			controlPort = opToReconfigure.getOpContext().findDownstream(opId).location().getInC();
		}

		for(EndPoint ep : downstreamTypeConnection){
			if(ep.getOperatorId() == opId){
				try{
					Socket dataS = new Socket(newIp, dataPort);
					Socket controlS = new Socket(newIp, controlPort);
					Socket blindS = null;
					Buffer buf = downstreamBuffers.get(opId);
					int index = opToReconfigure.getOpContext().getDownOpIndexFromOpId(opId);
					SynchronousCommunicationChannel cci = new SynchronousCommunicationChannel(opId, dataS, controlS, buf);
					downstreamTypeConnection.set(index, cci);
				}
				catch(IOException io){
					System.out.println("While re-creating DOWNSTREAM socket: "+io.getMessage());
				}
			}
		}
		for(EndPoint ep : upstreamTypeConnection){
			if(ep.getOperatorId() == opId){
				int upControlPort = CONTROL_SOCKET + ep.getOperatorId();
				try{
					Socket controlS = new Socket(newIp, upControlPort);
					int index = opToReconfigure.getOpContext().getUpOpIndexFromOpId(opId);
					SynchronousCommunicationChannel cci = new SynchronousCommunicationChannel(opId, null, controlS, null);
					upstreamTypeConnection.set(index, cci);
				}
				catch(IOException io){
					System.out.println("While re-creating UPSTREAM socket: "+io.getMessage());
				}
			}
		}
		LOG.debug("-> PUContext. Conns of OP: {} updated", opId);
	}
}
