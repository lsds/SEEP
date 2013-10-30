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
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitNodeState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InvalidateState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateAck;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.MapSerializer;

/** 
* ControlHandlerWorker. This class is in charge of managing control messages.
*/

public class ControlHandlerWorker implements Runnable{

	final private Logger LOG = LoggerFactory.getLogger(ControlHandlerWorker.class);
	
	private Socket incomingSocket = null;
	private CoreRE owner = null;
	//In charge of control thread execution
	private boolean goOn;
	private Kryo k = null;

	public ControlHandlerWorker(Socket incomingSocket, CoreRE owner){
		this.incomingSocket = incomingSocket;
		this.owner = owner;
		this.goOn = true;
		this.k = initializeKryo();
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
		return k;
	}

	public void run(){
		InputStream is = null;
		OutputStream os = null;
		ControlTuple tuple = null;
		try{
			//Establish input stream, which receives serialised objects
			is = incomingSocket.getInputStream();
			os = incomingSocket.getOutputStream();
			Input i = new Input(is, 100000);
			//Read the connection to get the data
			while(goOn){
				tuple = k.readObject(i, ControlTuple.class);
				if(tuple != null){
					InetAddress ip = incomingSocket.getInetAddress();
					owner.processControlTuple(tuple, os, ip);
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
		catch(IOException io){
			LOG.error("-> ControlHandlerWorker. IO Error "+io.getMessage());
			io.printStackTrace();
		}
	}
}
