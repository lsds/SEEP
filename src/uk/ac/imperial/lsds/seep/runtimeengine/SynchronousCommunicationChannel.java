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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.buffer.Buffer;
import uk.ac.imperial.lsds.seep.buffer.OutputLogEntry;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.BatchTuplePayload;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.EndPoint;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;

/**
* OutputInformation. This class models the information associated to a downstream or upstream connection
*/
public class SynchronousCommunicationChannel implements EndPoint{

	private int targetOperatorId;
	private Socket downstreamDataSocket;
	private Socket downstreamControlSocket;
	private Buffer buffer;

	private final Object controlSocketLock = new Object(){};

	private Output output = null;
	private OutputStream bos = null;

	//Set atomic variables to their initial value
	private AtomicBoolean stop = new AtomicBoolean(false);
	private AtomicBoolean replay = new AtomicBoolean(false);

	private TimestampTracker reconf_ts;
	private long last_ts;
	private Iterator<OutputLogEntry> sharedIterator;

	//Batch information for this channel
	private BatchTuplePayload batch = new BatchTuplePayload();
	private int channelBatchSize = Integer.parseInt(GLOBALS.valueFor("batchLimit"));
	private long tick = 0;

	public SynchronousCommunicationChannel(int opId, Socket downstreamSocketD, Socket downstreamSocketC, Buffer buffer){
		this.targetOperatorId = opId;
		this.downstreamDataSocket = downstreamSocketD;
		this.downstreamControlSocket = downstreamSocketC;
		this.buffer = buffer;
		try {
			/// \fixme{this must be fixed, different CONSTRUCTORS, please...}
			if(downstreamDataSocket != null){
				//Create buffered output stream
				//Create common outputstream and let kryo to manage buffers
				bos = downstreamSocketD.getOutputStream();
				//Create the kryo output for this socket
				output = new Output(bos);
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int getOperatorId(){
		return targetOperatorId;
	}

	public Socket getDownstreamControlSocket() {
		synchronized(controlSocketLock)
		{
			while (downstreamControlSocket == null)
			{
				try
				{
					controlSocketLock.wait(30*1000);
				}
				catch(InterruptedException e) { e.printStackTrace(); /*Urgh*/}
			}
			return downstreamControlSocket;
		}
	}

	public Socket tryGetDownstreamControlSocket()
	{
		synchronized(controlSocketLock)
		{
			return downstreamControlSocket;
		}
	}

	/** ThreadSafety: This method, getOutput(), and getDownstreamDataSocket()
	 * should only ever be called with the corresponding OutputQueue lock held
	 * to avoid concurrent data consumer/processing threads trying to reopen a failed socket.
	 * dokeeffe TODO: Currently this will just block indefinitely until the create socket call
	 * succeeds. Probably want to add a way to interrupt the waiting (e.g. if we need
	 * to scale-in/reconfigure/shutdown).
	 * @return the new socket.
	 */
	public Socket reopenDownstreamDataSocket()
	{
		if (downstreamDataSocket == null) { throw new RuntimeException("No data socket on this channel."); }

		InetAddress ip = downstreamDataSocket.getInetAddress();
		int port = downstreamDataSocket.getPort();

		//Try to close the current downstream data socket and output stream
		try { output.close(); }
		catch (KryoException e) { e.printStackTrace(); }
		try { downstreamDataSocket.close(); }
		catch (IOException e) {e.printStackTrace();}
		output = null;
		downstreamDataSocket = null;

		boolean success = false;
		while(!success)
		{
			Socket tmpSocket = null;
			OutputStream tmpOutput = null;
			try
			{
				tmpSocket = new Socket(ip, port);
				tmpOutput = tmpSocket.getOutputStream();
				downstreamDataSocket = tmpSocket;
				output = new Output(tmpOutput);
				success = true;
			}
			catch(IOException e)
			{
				if (tmpSocket != null)
				{
					try { tmpSocket.close(); }
					catch (IOException e1) {}
				}
				e.printStackTrace();
			}
		}
		return downstreamDataSocket;
	}

	/**
	 * dokeeffe TODO: Should probably allow this to be
	 * extended to support cancellation. At the moment it will
	 * try to reconnect for ever.
	 */
	public void reopenDownstreamControlSocketNonBlocking(Socket prevSocketToClose)
	{
		if (prevSocketToClose == null)
		{
			//Temp sanity check, should probably remove this restriction.
			throw new RuntimeException("Previous socket should never be null.");
		}

		final InetAddress ip;
		final int port;

		synchronized(controlSocketLock)
		{
			if (downstreamControlSocket != null && prevSocketToClose == downstreamControlSocket)
			{
				ip = downstreamControlSocket.getInetAddress();
				port = downstreamControlSocket.getPort();
				downstreamControlSocket = null;
			}
			else
			{
				try { prevSocketToClose.close(); }
				catch(IOException e) { e.printStackTrace(); /*Urgh*/ }

				//Another caller is already reopening the socket.
				return;
			}
		}

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(true)
				{
					Socket tmpSocket = null;
					try
					{
						tmpSocket = new Socket(ip, port);
						synchronized(controlSocketLock)
						{
							downstreamControlSocket = tmpSocket;
							controlSocketLock.notifyAll();
							return;
							//return downstreamControlSocket;
						}
					}
					catch(IOException e)
					{
						e.printStackTrace(); // Urgh
					}
					//dokeeffe TODO: N.B. If some other exception causes this
					//thread to fail then this connection will be stuck forever
					//as there is no way for another thread to restart the connection
				}
			}
		}).start();

	}



	public void setSharedIterator(Iterator<OutputLogEntry> i){
		this.sharedIterator = i;
	}

	public Iterator<OutputLogEntry> getSharedIterator(){
		return sharedIterator;
	}

	public Output getOutput() {
		return output;
	}

	public void setTick(long tick){
		this.tick = tick;
	}

	public Socket getDownstreamDataSocket(){
		return downstreamDataSocket;
	}

	public Buffer getBuffer(){
		return buffer;
	}

	public AtomicBoolean getReplay(){
		return replay;
	}

	//dokeeffe TODO: Hmm, might want to keep this internal
	// and add an extra setter to the interface if this class
	// is going to handle reconnects.
	public AtomicBoolean getStop(){
		return stop;
	}

	public synchronized BatchTuplePayload getBatch(){
		return batch;
	}

	public synchronized void addDataToBatch(TuplePayload payload){
		batch.addTuple(payload);
		channelBatchSize--;
		last_ts = payload.timestamp;
	}

	public int getChannelBatchSize(){
		return channelBatchSize;
	}

	public void resetChannelBatchSize(){
		channelBatchSize = 0;
	}

	public void cleanBatch(){
		batch.clear();
		int limit = Integer.parseInt(GLOBALS.valueFor("batchLimit"));
		channelBatchSize = limit;
	}

	public void cleanBatch2(){
		batch = new BatchTuplePayload();
	}

	public long getLast_ts(){
		return last_ts;
	}

	public TimestampTracker getReconf_ts(){
		return reconf_ts;
	}

	public void setReconf_ts(TimestampTracker ts){
		this.reconf_ts = ts;
	}
}
