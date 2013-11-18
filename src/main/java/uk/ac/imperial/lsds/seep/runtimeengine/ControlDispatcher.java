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
package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.Main;
import uk.ac.imperial.lsds.seep.P;
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
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.processingunit.PUContext;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

public class ControlDispatcher {
	
	final private Logger LOG = LoggerFactory.getLogger(ControlDispatcher.class);

	private final int BLIND_SOCKET;
	
	private PUContext puCtx = null;
	private Kryo k = null;
	
	///\fixme{remove this variable asap. debugging for now}
	// FIXME: REMOVE THIS FROM HERE ASAP
	private Output largeOutput = new Output(10000000);
	
	public ControlDispatcher(PUContext puCtx){
		this.puCtx = puCtx;
		this.BLIND_SOCKET = new Integer(P.valueFor("blindSocket"));
		this.k = initializeKryo();
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
	
	public void sendAllUpstreams(ControlTuple ct){
		for(int i = 0; i < puCtx.getUpstreamTypeConnection().size(); i++) {
			sendUpstream(ct, i);
		}
	}

	public void sendUpstream(ControlTuple ct, int index){
		EndPoint obj = puCtx.getUpstreamTypeConnection().elementAt(index);
		Socket socket = ((SynchronousCommunicationChannel) obj).getDownstreamControlSocket();
		Output output = null;
		try{
			output = new Output(socket.getOutputStream());
			synchronized(k){
				synchronized(socket){
					synchronized (output){
						k.writeObject(output, ct);
						output.flush();
					}
				}
			}
		}
		catch(IOException io){
			LOG.error("-> Dispatcher. While sending control msg "+io.getMessage());
			io.printStackTrace();
		}
	}
	
	public void sendOpenSessionWaitACK(ControlTuple ct, int index){
		DisposableCommunicationChannel dcc = (DisposableCommunicationChannel) puCtx.getStarTopology().get(index);
		int targetOpId = dcc.getOperatorId();
		InetAddress ip_endpoint = dcc.getIp();
		
		Output output = null;
		BufferedReader in = null;
		try{
			Socket socket = new Socket(ip_endpoint, (Main.CONTROL_SOCKET+targetOpId));
			output = new Output(socket.getOutputStream());
			synchronized(k){
				synchronized(socket){
					synchronized (output){
						k.writeObject(output, ct);
						output.flush();
						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String reply = null;
						System.out.println("waiting to read answer/reply");
						reply = in.readLine();
						System.out.println("READ");
						in.close();
						output.close();
					}
				}
			}
		}
		catch(IOException io){
			LOG.error("-> Dispatcher. While sending control msg "+io.getMessage());
			io.printStackTrace();
		}
	}
	
	public void sendCloseSession(ControlTuple ct, int index){
		DisposableCommunicationChannel dcc = (DisposableCommunicationChannel) puCtx.getStarTopology().get(index);
		int targetOpId = dcc.getOperatorId();
		InetAddress ip_endpoint = dcc.getIp();
		Output output = null;
		try{
			Socket socket = new Socket(ip_endpoint, (Main.CONTROL_SOCKET+targetOpId));
			output = new Output(socket.getOutputStream());
			synchronized(k){
				synchronized(socket){
					synchronized (output){
						k.writeObject(output, ct);
						output.flush();
//						output.close();
					}
				}
			}
		}
		catch(IOException io){
			LOG.error("-> Dispatcher. While sending control msg "+io.getMessage());
			io.printStackTrace();
		}
	}
	
	public void sendUpstream_blind(ControlTuple ct, int index){
		long startSend = System.currentTimeMillis();
//		EndPoint obj = puCtx.getUpstreamTypeConnection().elementAt(index);
		InetAddress ip_endpoint = ((DisposableCommunicationChannel)puCtx.getStarTopology().get(index)).getIp();
		//Reopen socket before sending... only if closed
//		Socket socket = ((SynchronousCommunicationChannel) obj).reOpenBlindSocket();
		try{
			Socket socket = new Socket(ip_endpoint, BLIND_SOCKET);
		
			largeOutput.setOutputStream(socket.getOutputStream());
//			output = new Output(socket.getOutputStream());
//			System.out.println("WRITING TO: "+socket.toString());
			synchronized(k){
				synchronized(socket){
					synchronized (largeOutput){
						long startWrite = System.currentTimeMillis();
						System.out.println("Send chunk to: "+socket.toString());
						k.writeObject(largeOutput, ct);
//						System.out.println("%*% SER SIZE: "+largeOutput.toBytes().length+" bytes");
						largeOutput.flush();
						largeOutput.close();
						long stopWrite = System.currentTimeMillis();
//						System.out.println("% Write socket: "+(stopWrite-startWrite));
					}
				}
			}
		}
		catch(IOException io){
			LOG.error("-> Dispatcher. While sending control msg "+io.getMessage());
			io.printStackTrace();
		}
		long stopSend = System.currentTimeMillis();
//		System.out.println("% Send : "+(stopSend-startSend));
	}
	
	public void sendUpstream_blind_metadata(int data, int index){
		EndPoint obj = puCtx.getUpstreamTypeConnection().elementAt(index);
		Socket socket = ((SynchronousCommunicationChannel) obj).reOpenBlindSocket();
		try{
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(data);
			dos.flush();
			dos.close();
		}
		catch(IOException io){
			LOG.error("-> Dispatcher. While sending control msg "+io.getMessage());
			io.printStackTrace();
		}
	}
	
	public void sendDownstream(ControlTuple ct, int index){
		EndPoint obj = puCtx.getDownstreamTypeConnection().elementAt(index);
		if (obj instanceof SynchronousCommunicationChannel){
			Socket socket = ((SynchronousCommunicationChannel) obj).getDownstreamControlSocket();
			Output output = null;
			try{
				output = new Output(socket.getOutputStream());
				synchronized(k){
					synchronized (socket){
						k.writeObject(output, ct);
						output.flush();
					}
				}
			}
			catch(IOException io){
				LOG.error("-> Dispatcher. While sending control msg "+io.getMessage());
				io.printStackTrace();
			}
		}
	}
	
	public void ackControlMessage(ControlTuple genericAck, OutputStream os){
		Output output = new Output(os);
		synchronized(k){
			k.writeObject(output, genericAck);
		}
		output.flush();
	}
	
	public void initStateMessage(ControlTuple initStateMsg, OutputStream os){
		Output output = new Output(os);
		synchronized(k){
			k.writeObject(output, initStateMsg);
		}
		output.flush();
	}
	
	public Object deepCopy(Object toCopy){
		long s = System.currentTimeMillis();
		System.out.println("CLASS: "+toCopy.getClass().toString());
		synchronized(k){
			k.register(toCopy.getClass());
//			Object o = k.copy(toCopy);
			long e = System.currentTimeMillis();
			System.out.println("TOTAL-Kryo-SER: "+(e-s));
			return k.copy(toCopy);
		}
	}
}