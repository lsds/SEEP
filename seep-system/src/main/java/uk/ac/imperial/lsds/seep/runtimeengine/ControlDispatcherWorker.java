package uk.ac.imperial.lsds.seep.runtimeengine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.zip.DeflaterOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;

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
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import de.javakaffee.kryoserializers.BitSetSerializer;

public class ControlDispatcherWorker implements Runnable
{
	final private Logger logger = LoggerFactory.getLogger(ControlDispatcherWorker.class);

	
	//N.B. Unbounded, or at least very large, since if a thread calling send blocks it
	//could starve other channels of control tuples. Possibly would have been better to
	//require callers to use a separate thread for each channel but this approach requires
	//fewer code changes.
	private final BlockingQueue<ControlTuple> queue = new LinkedBlockingQueue<>();	
	private final SynchronousCommunicationChannel channel;
	private final Kryo ctrlKryo;

	public ControlDispatcherWorker(SynchronousCommunicationChannel channel)
	{
		this.channel = channel;
		this.ctrlKryo = initializeCtrlKryo();
	}

	public Kryo getCtrlKryo()
	{
		return ctrlKryo;
	}

	private Kryo initializeCtrlKryo(){
		Kryo k = new Kryo();
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

	public void run()
	{	
		while(true)
		{
			ControlTuple ct = null;
		
			try {	ct = queue.take(); } catch(InterruptedException e)  { continue; }

			//Now try to send it. Perhaps might want to filter/squash if there is a big backlog.
			int numRetries = 0;
			boolean success = false;
			long sendStart = System.currentTimeMillis();
			boolean block = false; //todo
			while(!success)
			{
				Socket socket = block ? channel.getDownstreamControlSocket() : channel.tryGetDownstreamControlSocket();
				if (socket == null)
				{
					if (block) { throw new RuntimeException("Logic error."); }
					logger.warn("Dropping control msg as control socket is null.");
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
					synchronized(ctrlKryo){
						synchronized(socket){
							synchronized (output){
								long writeStart = System.currentTimeMillis();
								if (ct.getTsSend() > 0) { ct.setTsSend(writeStart); }
								ctrlKryo.writeObject(output, ct);
								output.flush();
								logger.debug("Wrote control tuple "+ct.toString()+" to "+channel.getOperatorId()+",size="+output.total()+" in "+(System.currentTimeMillis()-writeStart)+" ms (+sync="+(System.currentTimeMillis() - syncStart)+" ms)");
							}
						}
					}
					success = true;
				}
				catch(IOException | KryoException e){
					if (numRetries < 1)
					{
						logger.error("-> Dispatcher. While sending control msg "+e.getMessage());
						e.printStackTrace();
					}
					channel.reopenDownstreamControlSocketNonBlocking(socket);
					if (!block) { break; }
				}
			}

			if (!success) { logger.warn("Sending control tuple to "+channel.getOperatorId()+" failed in "+(System.currentTimeMillis()-sendStart)+" ms"); }
		}
	}

	public boolean send(ControlTuple ct, boolean block)
	{
		if (block) { throw new RuntimeException("TODO."); }

		//add/offer/put
		return queue.offer(ct);
		//TODO: What about block and dir?
	}
}
