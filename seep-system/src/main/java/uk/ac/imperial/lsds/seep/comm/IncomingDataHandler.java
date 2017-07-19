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
package uk.ac.imperial.lsds.seep.comm;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.runtimeengine.DataStructureAdapter;
import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.OperatorContext;
import uk.ac.imperial.lsds.seep.runtimeengine.SynchronousCommunicationChannel;

/**
* IncomingDataHandler. This is in charge of managing incoming data connections and associate a thread to them
*/

public class IncomingDataHandler implements Runnable{

	final private Logger LOG = LoggerFactory.getLogger(IncomingDataHandler.class);
	
	//private Operator owner;
	private CoreRE owner;
	private int connPort;
	private boolean goOn;
	private Map<String, Integer> idxMapper;
	private DataStructureAdapter dsa;
	private final Map<Integer, BlockingQueue<ControlTuple>> ctrlQueues;

	public int getConnPort(){
		return connPort;
	}

	public void setConnPort(int connPort){
		this.connPort = connPort;
	}

	public IncomingDataHandler(CoreRE owner, int connPort, Map<String, Integer> idxMapper, DataStructureAdapter dsa, Map<Integer, BlockingQueue<ControlTuple>> ctrlQueues){
		this.owner = owner;
		this.connPort = connPort;
		//this.selector = initSelector();
		this.goOn = true;
		this.idxMapper = idxMapper;
		this.dsa = dsa;
		this.ctrlQueues = ctrlQueues;
		LOG.info(" -> ctrl queues = "+ctrlQueues);
	}

	public void run(){
		ServerSocket incDataServerSocket = null;
		int socketCount=0;
		try{
			//Establish listening port
			incDataServerSocket = new ServerSocket(connPort);
			incDataServerSocket.setReuseAddress(true);
			incDataServerSocket.setReceiveBufferSize(Integer.parseInt(GLOBALS.valueFor("socketBufferSize")));
			LOG.info("-> IncomingDataHandler listening in port: {}", connPort);
			LOG.info("idh socket receiver buffer size = "+incDataServerSocket.getReceiveBufferSize());
			//Upstream id
			while(goOn){
				Socket incomingConn = incDataServerSocket.accept();
				String threadName = incomingConn.getInetAddress().toString();
				Thread newConn = null;
				//int upstreamOpId = owner.getOpIdFromInetAddress(((InetSocketAddress)incomingConn.getRemoteSocketAddress()).getAddress());
				InetSocketAddress inSocketAddr = (InetSocketAddress)incomingConn.getRemoteSocketAddress();
				int upstreamOpId = owner.getOpIdFromInetAddressAndPort(inSocketAddr.getAddress(), inSocketAddr.getPort());
				LOG.info("-> Creating worker for upstream: "+upstreamOpId);
				if (ctrlQueues == null)
				{
					newConn = new Thread(new IncomingDataHandlerWorker(incomingConn, owner, idxMapper, dsa),  "idhw-"+threadName+"-T-"+socketCount++);
				}
				else
				{
					LOG.info("-> ctrl queue:"+ctrlQueues.get(upstreamOpId));
					OperatorContext opCtx = owner.getProcessingUnit().getOperator().getOpContext();
					int index = opCtx.getUpOpIndexFromOpId(upstreamOpId);
					LOG.info("-> index:"+index+", upOpIds="+opCtx.getUpstreamOpIdList()+",indexes="+opCtx.getListOfUpstreamIndexes());
					if (owner.getProcessingUnit().getPUContext().isConfigured())
					{
						Vector<EndPoint> upstreamConnections = owner.getProcessingUnit().getPUContext().getUpstreamTypeConnection();
						//TODO: Thread safety wrt PUContext here?
						EndPoint obj = upstreamConnections.elementAt(index);
						newConn = new Thread(new IncomingDataHandlerWorker(incomingConn, owner, idxMapper, dsa, ctrlQueues.get(upstreamOpId)),  "idhjw-"+threadName+"-T-"+socketCount++);
						SynchronousCommunicationChannel channel = ((SynchronousCommunicationChannel) obj);
						channel.updateDownstreamControlSocket(incomingConn); 
					}
					else
					{
						LOG.warn("Received incoming data conn but pu ctxt not created yet. Closing.");
						incomingConn.close();	
						continue;
					}
				}
				newConn.start();
			}
			incDataServerSocket.close();
		}
		catch(BindException be){
			LOG.error("-> BIND EXC IO Error "+be.getMessage());
			LOG.error("-> Was trying to connect to: "+connPort);
			be.printStackTrace();
		}
		catch(IOException io){
			LOG.error("-> IncomingDataHandler. While listening incoming conns "+io.getMessage());
			io.printStackTrace();
		}
	}
}
