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
import java.util.BitSet;
import java.util.HashMap;
import java.util.zip.DeflaterOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
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
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.processingunit.PUContext;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import de.javakaffee.kryoserializers.BitSetSerializer;
import com.google.common.collect.RangeSet;

public class ControlDispatcher {
	
	final private Logger LOG = LoggerFactory.getLogger(ControlDispatcher.class);

	private final int BLIND_SOCKET;
	private final int CONTROL_SOCKET;
	
	private PUContext puCtx = null;
	private Kryo k = null;

	///\fixme{remove this variable asap. debugging for now}
	// FIXME: REMOVE THIS FROM HERE ASAP
	private Output largeOutput = new Output(10000000);
	
	public ControlDispatcher(PUContext puCtx){
		this.puCtx = puCtx;
		this.BLIND_SOCKET = new Integer(GLOBALS.valueFor("blindSocket"));
		this.CONTROL_SOCKET = new Integer(GLOBALS.valueFor("controlSocket"));
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
		//k.register(BitSet.class);
		k.register(BitSet.class, new BitSetSerializer());
		k.register(OpFailureCtrl.class);
		k.register(FailureCtrl.class);
		k.register(UpDownRCtrl.class);
		k.register(DownUpRCtrl.class);

		return k;
	}
	
	public void sendAllUpstreams(ControlTuple ct){
		for(int i = 0; i < puCtx.getUpstreamTypeConnection().size(); i++) {
			sendUpstream(ct, i);
		}
	}
	public void sendAllUpstreams(ControlTuple ct, boolean block){
		for(int i = 0; i < puCtx.getUpstreamTypeConnection().size(); i++) {
			sendUpstream(ct, i, block);
		}
	}

	public boolean sendUpstream(ControlTuple ct, int index)
	{
		return sendUpstream(ct, index, true);
	}

	public boolean sendDummyUpstream(ControlTuple ct, int index)
	{
		EndPoint obj = puCtx.getDummyUpstreamTypeConnection().elementAt(index);
		return sendUpstream(ct, (SynchronousCommunicationChannel)obj, false);
	}	

	public boolean sendUpstream(ControlTuple ct, int index, boolean block){
		EndPoint obj = puCtx.getUpstreamTypeConnection().elementAt(index);
		return sendUpstream(ct, (SynchronousCommunicationChannel)obj, block);
	}
	
	private boolean sendUpstream(ControlTuple ct, SynchronousCommunicationChannel channel, boolean block){
		//return send(ct, channel, block, "upstream");
		return channel.getControlDispatcherWorker().send(ct, block);	
	}

	private boolean send(ControlTuple ct, SynchronousCommunicationChannel channel, boolean block, String dir)
	{
		int numRetries = 0;
		boolean success = false;
		long sendStart = System.currentTimeMillis();
		while(!success)
		{
			Socket socket = block ? channel.getDownstreamControlSocket() : channel.tryGetDownstreamControlSocket();
			if (socket == null)
			{
				if (block) { throw new RuntimeException("Logic error."); }
				LOG.warn("Dropping control msg as control socket is null.");
				break;
			}
			
			Output output = null;
			try{
				//boolean compress = true;
				boolean compress = false;
				OutputStream outputStream = compress ? new DeflaterOutputStream(socket.getOutputStream()) : socket.getOutputStream();
				output = new Output(outputStream);
				long syncStart = System.currentTimeMillis();
				//synchronized(k){
				synchronized(channel.getControlDispatcherWorker().getCtrlKryo()){
					synchronized(socket){
						synchronized (output){
							long writeStart = System.currentTimeMillis();
							if (ct.getTsSend() > 0) { ct.setTsSend(writeStart); }
							channel.getControlDispatcherWorker().getCtrlKryo().writeObject(output, ct);
							output.flush();
							LOG.debug("Wrote "+dir+" control tuple "+ct.toString()+" to "+channel.getOperatorId()+",size="+output.total()+" in "+(System.currentTimeMillis()-writeStart)+" ms (+sync="+(System.currentTimeMillis() - syncStart)+" ms)");
						}
					}
				}
				success = true;
			}
			catch(IOException | KryoException e){
				if (numRetries < 1)
				{
					LOG.error("-> Dispatcher. While sending control msg "+e.getMessage());
					e.printStackTrace();
				}
				channel.reopenDownstreamControlSocketNonBlocking(socket);
				if (!block) { break; }
			}
		}
		if (!success) { LOG.warn("Sending control tuple "+dir+" to "+channel.getOperatorId()+" failed in "+(System.currentTimeMillis()-sendStart)+" ms"); }
		return success;
	}
	
	public void sendOpenSessionWaitACK(ControlTuple ct, int index){
		DisposableCommunicationChannel dcc = (DisposableCommunicationChannel) puCtx.getStarTopology().get(index);
		int targetOpId = dcc.getOperatorId();
		InetAddress ip_endpoint = dcc.getIp();
		
		Output output = null;
		BufferedReader in = null;
		try{
			Socket socket = new Socket(ip_endpoint, (CONTROL_SOCKET+targetOpId));
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
			Socket socket = new Socket(ip_endpoint, (CONTROL_SOCKET+targetOpId));
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
	
	public boolean sendDownstream(ControlTuple ct, int index) { return sendDownstream(ct, index, true); }
	
	public boolean sendDummyDownstream(ControlTuple ct, int index)
	{
		EndPoint obj = puCtx.getDummyDownstreamTypeConnection().elementAt(index);
		return sendDownstream(ct, (SynchronousCommunicationChannel)obj, false);
	}	

	public boolean sendDownstream(ControlTuple ct, int index, boolean block){
		EndPoint obj = puCtx.getDownstreamTypeConnection().elementAt(index);
		return sendDownstream(ct, (SynchronousCommunicationChannel)obj, block);
	}
	
	private boolean sendDownstream(ControlTuple ct, SynchronousCommunicationChannel channel, boolean block) 
	{
		//return send(ct, channel, block, "downstream");
		//TODO: the return value is kind of meaningless here.
		return channel.getControlDispatcherWorker().send(ct, block);	
	}

	public void ackControlMessage(ControlTuple genericAck, OutputStream os){
		Output output = new Output(os);
		synchronized(k){
			k.writeObject(output, genericAck);
		}
		output.flush();
		throw new RuntimeException("Deprecated: Sync for the os has now moved into the channel?");
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
