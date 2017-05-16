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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DownUpRCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.FailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InvalidateState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.OpFailureCtrl;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateAck;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.UpDownRCtrl;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.GLOBALS;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import de.javakaffee.kryoserializers.BitSetSerializer;
import com.google.common.collect.RangeSet;

/** 
* ControlHandlerWorker. This class is in charge of managing control messages.
*/

public class ControlHandlerWorker implements Runnable{

	private final Logger LOG = LoggerFactory.getLogger(ControlHandlerWorker.class);

	private Socket incomingSocket = null;
	private CoreRE owner = null;
	//In charge of control thread execution
	private boolean goOn;
	private Kryo k = null;
	private java.util.Random rand = new java.util.Random();
	private final BlockingQueue<ControlTuple> ctrlChannel;
	private final boolean dummy;

	public ControlHandlerWorker(Socket incomingSocket, CoreRE owner){
		this(incomingSocket, owner, false);
	}

	public ControlHandlerWorker(Socket incomingSocket, CoreRE owner, boolean dummy){
		this.incomingSocket = incomingSocket;
		this.owner = owner;
		this.goOn = true;
		this.k = initializeKryo();
		this.ctrlChannel = null;
		this.dummy = dummy;
	}

	public ControlHandlerWorker(BlockingQueue<ControlTuple> ctrlChannel, CoreRE owner){
		this.incomingSocket = null;
		this.owner = owner;
		this.goOn = true;
		this.k = null;
		this.ctrlChannel = ctrlChannel; 
		this.dummy = false;
	}
	
	private Kryo initializeKryo(){
		//optimize here kryo
		k = new Kryo();
		k.register(ControlTuple.class);
		k.register(MemoryChunk.class);
		k.register(StateChunk.class);
		k.register(HashMap.class, new MapSerializer());
		k.register(BackupOperatorState.class);
		k.register(byte[].class);
		k.register(RawData.class);
		k.register(Ack.class);
		k.register(BackupNodeState.class);
		k.register(Resume.class);
		k.register(ScaleOutInfo.class);
		k.register(StateAck.class);
		k.register(ArrayList.class);
		k.register(BackupRI.class);
		k.register(InitNodeState.class);
		k.register(InitOperatorState.class);
		k.register(InitRI.class);
		k.register(InvalidateState.class);
		k.register(ReconfigureConnection.class);
		//k.register(BitSet.class);
		k.register(BitSet.class, new BitSetSerializer());
		k.register(OpFailureCtrl.class);
		k.register(FailureCtrl.class);
		k.register(UpDownRCtrl.class);
		k.register(DownUpRCtrl.class);
		
		return k;
	}

	public void run(){
		if (ctrlChannel != null)
		{
			channelRead();
		}
		else
		{
			socketRead();
		}
	}

	private void channelRead()
	{
		InputStream is = null;
		OutputStream os = null;
		ControlTuple tuple = null;
		long txnStart = System.currentTimeMillis();
		while(goOn){
			long readStart = System.currentTimeMillis();

			try { tuple = ctrlChannel.take(); } 
			catch(InterruptedException e) { LOG.error("Unexpected interrupt: "+ e); System.exit(1); }

			long readEnd = System.currentTimeMillis();
			long netDelay = tuple.getTsSend() > 0 ? readEnd - tuple.getTsSend() : -1;
			LOG.debug("Read control tuple in "+ (readEnd-readStart) + " ms"+ (netDelay > 0 ? ", netDelay="+netDelay : "")+",txnDelay="+(readStart-txnStart));
			txnStart = readEnd;
			//simulateNetDelay();
			if(tuple != null){
				//N.B. If os is no longer null, need to change locking around upstream since icdhw will pass
				//the same socket for sending upstream control messages.
				owner.processControlTuple(tuple, null, null);
				LOG.debug("Processed control tuple in "+(System.currentTimeMillis()-readEnd) + " ms");
			}
			else{
				LOG.error("-> ControlHandlerWorker. TUPLE IS NULL !");
				System.exit(1);
			}
		}
		LOG.error("-> ControlHandlerWorker. Exiting loop !");
	}

	private void socketRead()
	{
		InputStream is = null;
		OutputStream os = null;
		ControlTuple tuple = null;
		try{
			if (!Boolean.parseBoolean(GLOBALS.valueFor("piggybackControlTraffic")))
			{ setSocketBufSize(incomingSocket); }
			//Establish input stream, which receives serialised objects
			is = incomingSocket.getInputStream();
			if (!Boolean.parseBoolean(GLOBALS.valueFor("piggybackControlTraffic")))
			{ os = incomingSocket.getOutputStream(); } //This must be an incoing conn created by ouptut queue worker.
			Input i = new Input(is, 100000);
			//Read the connection to get the data
			long txnStart = System.currentTimeMillis();
			while(goOn){
				long readStart = System.currentTimeMillis();
				tuple = k.readObject(i, ControlTuple.class);
				long readEnd = System.currentTimeMillis();
				long netDelay = tuple.getTsSend() > 0 ? readEnd - tuple.getTsSend() : -1;
				LOG.debug("Read control tuple in "+ (readEnd-readStart) + " ms"+ (netDelay > 0 ? ", netDelay="+netDelay : "")+",txnDelay="+(readStart-txnStart));
				txnStart = readEnd;
				//simulateNetDelay();
				if(tuple != null){
					InetAddress ip = incomingSocket.getInetAddress();
					if (!dummy) 
					{
						owner.processControlTuple(tuple, os, ip);
						LOG.debug("Processed control tuple in "+(System.currentTimeMillis()-readEnd) + " ms");
					}
				}
				else{
					LOG.error("-> ControlHandlerWorker. TUPLE IS NULL !");
					break;
				}
			}
			//Close streams and socket
			LOG.error("-> Closing connection");
			is.close();
			incomingSocket.close();
		}
		catch(Exception io){
			LOG.error("-> ControlHandlerWorker. IO Error "+io.getMessage());
			io.printStackTrace();
			try { 
				incomingSocket.close();
			} catch (IOException e) {}
		}
	}

	private void setSocketBufSize(Socket socket) throws SocketException
	{
		int bufSize = Integer.parseInt(GLOBALS.valueFor("ctrlSocketBufSize"));
		socket.setSendBufferSize(bufSize);
		socket.setReceiveBufferSize(bufSize);
		if (bufSize != socket.getSendBufferSize() || bufSize != socket.getReceiveBufferSize()) 
		{ 
			LOG.error("Set socket buf size failed, requested="+bufSize+",send="+socket.getSendBufferSize()+",receive="+socket.getReceiveBufferSize());
		}
	}	

	private void simulateNetDelay()
	{

		long minDelay = 10;
		long maxDelay = 500;
		
		long nextDelay = minDelay;

		if (rand.nextDouble() < 0.005) { nextDelay += (long)((maxDelay - minDelay) * rand.nextDouble()); }

		try
		{
			Thread.sleep(nextDelay);


		} catch(Exception e) { throw new RuntimeException(e); }


	}

}
