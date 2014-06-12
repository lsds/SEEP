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
package uk.ac.imperial.lsds.seep.reliable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
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
import uk.ac.imperial.lsds.seep.runtimeengine.JobBean;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

public class SerialiserWorker implements Runnable{
	
	final private Logger LOG = LoggerFactory.getLogger(SerialiserWorker.class);

	private Kryo k;
	private final int BLIND_SOCKET;
	private ArrayBlockingQueue<JobBean> jobQueue;
	private boolean goOn = true;
	
	public SerialiserWorker(ArrayBlockingQueue<JobBean> jobQueue){
		this.k = initializeKryo();
		this.BLIND_SOCKET = new Integer(GLOBALS.valueFor("blindSocket"));
		this.jobQueue = jobQueue;
	}
	
	public void killThread(){
		this.goOn = false;
	}

	private Kryo initializeKryo(){
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
	
	@Override
	public void run() {
		while(goOn){
			JobBean jb = null;
			try {
				jb = jobQueue.take();
			
				if(jb.msg != null){
					serialiseAndSend(jb.msg, jb.ip);
				}
				else{
					jobQueue.put(jb);
					goOn = false;
				}
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private Output largeOutput = new Output(1000000);
	public void serialiseAndSend(ControlTuple ct, InetAddress ip_endpoint){
		long startSend = System.currentTimeMillis();
		try{
			Socket socket = new Socket(ip_endpoint, BLIND_SOCKET);
			largeOutput.setOutputStream(socket.getOutputStream());
			synchronized(k){
				synchronized(socket){
					synchronized (largeOutput){
						long startWrite = System.currentTimeMillis();
						System.out.println(Thread.currentThread().getName()+": Send chunk to: "+socket.toString());
						k.writeObject(largeOutput, ct);
//						System.out.println("%*% SER SIZE: "+largeOutput.toBytes().length+" bytes");
						largeOutput.flush();
						largeOutput.close();
						long stopWrite = System.currentTimeMillis();
					}
				}
			}
		}
		catch(IOException io){
			LOG.error("-> Dispatcher. While sending control msg "+io.getMessage());
			io.printStackTrace();
		}
		long stopSend = System.currentTimeMillis();
	}
	
}
