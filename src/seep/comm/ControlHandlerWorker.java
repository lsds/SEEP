package seep.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import seep.comm.serialization.ControlTuple;
import seep.infrastructure.NodeManager;
import seep.runtimeengine.CoreRE;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

/** 
* ControlHandlerWorker. This class is in charge of managing control messages.
*/

public class ControlHandlerWorker implements Runnable{

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
		Kryo k = new Kryo();
		k.setClassLoader(owner.getRuntimeClassLoader());
		k.register(ControlTuple.class);
		return k;
	}

	public void run(){
		InputStream is = null;
		OutputStream os = null;
		ControlTuple tuple = null;
//		Seep.ControlTuple.Builder ct = null;
		try{
			//Establish input stream, which receives serialized objects
			is = incomingSocket.getInputStream();
			os = incomingSocket.getOutputStream();
			Input i = new Input(is);
			//Read the connection to get the data
			while(goOn){
//				tuple = Seep.ControlTuple.parseDelimitedFrom(is);
				tuple = k.readObject(i, ControlTuple.class);
				System.out.println("RECEIVED");
/// \todo {what is the underlying problem that makes tuple potentially be null?}
				if(tuple != null){
					owner.processControlTuple(tuple, os);
				}
				else{
					NodeManager.nLogger.severe("-> ControlHandlerWorker. TUPLE IS NULL !");
					break;
				}
			}
			//Close streams and socket
			NodeManager.nLogger.severe("-> Closing connection");
			is.close();
			incomingSocket.close();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> ControlHandlerWorker. IO Error "+io.getMessage());
			io.printStackTrace();
		}
	}
}
