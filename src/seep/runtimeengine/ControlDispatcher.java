package seep.runtimeengine;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import seep.comm.serialization.ControlTuple;
import seep.infrastructure.NodeManager;
import seep.operator.EndPoint;
import seep.processingunit.PUContext;

public class ControlDispatcher {

	private PUContext puCtx = null;
	private Kryo k = null;
	
	public ControlDispatcher(PUContext puCtx){
		this.puCtx = puCtx;
		this.k = initializeKryo();
	}
	
	private Kryo initializeKryo(){
		k = new Kryo();
		k.register(ControlTuple.class);
		return k;
	}
	
	public void sendAllUpstreams(ControlTuple ct){
		for(int i = 0; i < puCtx.getUpstreamTypeConnection().size(); i++) {
			sendUpstream(ct, i);
		}		
	}
	
	public void sendUpstream(ControlTuple ct, int index){
		EndPoint obj = puCtx.getUpstreamTypeConnection().elementAt(index);
		if(obj instanceof CoreRE){
			CoreRE operatorObj = (CoreRE) obj;
			operatorObj.processControlTuple(ct, null);
		}
		else if (obj instanceof CommunicationChannel){
			Socket socket = ((CommunicationChannel) obj).getDownstreamControlSocket();
			Output output = null;
			try{
				output = new Output(socket.getOutputStream());
				synchronized (output){
					k.writeObject(output, ct);
					output.flush();
				}
			}
			catch(IOException io){
				NodeManager.nLogger.severe("-> Dispatcher. While sending control msg "+io.getMessage());
				io.printStackTrace();
			}
		}
	}
	
	public void sendDownstream(ControlTuple ct, int index){
		EndPoint obj = puCtx.getDownstreamTypeConnection().elementAt(index);
		if(obj instanceof CoreRE){
			CoreRE operatorObj = (CoreRE) obj;
			operatorObj.processControlTuple(ct, null);
		}
		else if (obj instanceof CommunicationChannel){
			Socket socket = ((CommunicationChannel) obj).getDownstreamControlSocket();
			Output output = null;
			try{
				output = new Output(socket.getOutputStream());
				synchronized (socket){
//					tuple.writeDelimitedTo(socket.getOutputStream());
					k.writeObject(output, ct);
					output.flush();
				}
			}
			catch(IOException io){
				NodeManager.nLogger.severe("-> Dispatcher. While sending control msg "+io.getMessage());
				io.printStackTrace();
			}
		}
	}
	
	public void ackControlMessage(ControlTuple genericAck, OutputStream os){
		Output output = new Output(os);
		k.writeObject(output, genericAck);
		output.flush();
	}
	
	public void initStateMessage(ControlTuple initStateMsg, OutputStream os){
		Output output = new Output(os);
		k.writeObject(output, initStateMsg);
		output.flush();
	}
	
}
