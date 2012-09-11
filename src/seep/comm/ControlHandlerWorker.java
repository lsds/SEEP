package seep.comm;

import seep.comm.tuples.*;
import seep.comm.tuples.Seep.ControlTuple;
import seep.infrastructure.NodeManager;
import seep.operator.*;
import java.io.*;
import java.net.*;

/** 
* ControlHandlerWorker. This class is in charge of managing control messages.
*/

public class ControlHandlerWorker implements Runnable{

	private Socket incomingSocket = null;
	private Operator owner = null;
	//In charge of control thread execution
	private boolean goOn;

	public ControlHandlerWorker(Socket incomingSocket, Operator owner){
		this.incomingSocket = incomingSocket;
		this.owner = owner;
		this.goOn = true;
	}

	public void run(){
		InputStream is = null;
		OutputStream os = null;
		String infoS = incomingSocket.getRemoteSocketAddress().toString();
		ControlTuple tuple = null;
		Seep.ControlTuple.Builder ct = null;
		try{
			//Establish input stream, which receives serialized objects
			is = incomingSocket.getInputStream();
			os = incomingSocket.getOutputStream();
			//Read the connection to get the data
			while(goOn){
				tuple = Seep.ControlTuple.parseDelimitedFrom(is);
/// \todo {what is the underlying problem that makes tuple potentially be null?}
				if(tuple != null){
					ct = tuple.toBuilder();
					owner.processControlTuple(ct, os);
				}
				else break;
			}
			//Close streams and socket
			is.close();
			incomingSocket.close();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> ControlHandlerWorker. IO Error "+io.getMessage());
			io.printStackTrace();
		}
	}
}
