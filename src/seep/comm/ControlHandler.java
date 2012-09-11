package seep.comm;

import seep.infrastructure.Infrastructure;
import seep.infrastructure.NodeManager;
import seep.operator.*;

import java.io.*;
import java.net.*;

/** 
* ControlHandler. This class is in charge of managing control connections and attach them to a given thread that is in charge of serving them.
*/

public class ControlHandler implements Runnable{

	//The operator that owns this control handler
	private Operator owner;
	//The connection port that this controlhandler must use
	private int connPort;
	//This variable controls if this Runnable should keep running or not
	private boolean goOn;

	public Connectable getOwner(){
		return owner;
	}
	
	public void setOwner(Operator owner){
		this.owner = owner;
	}

	public int getConnPort(){
		return connPort;
	}

	public void setConnPort(int connPort){
		this.connPort = connPort;
	}

	public boolean getGoOn(){
		return goOn;
	}

	public void setGoOn(boolean goOn){
		this.goOn = goOn;
	}

	public ControlHandler(Operator owner, int connPort){
		this.owner = owner;
		this.connPort = connPort;
		this.goOn = true;
	}

	public void run(){
		ServerSocket controlServerSocket = null;
		try{
			//Establish listening port
    		controlServerSocket = new ServerSocket(connPort);
			controlServerSocket.setReuseAddress(true);
			//while goOn is active
			while(goOn){
				//Place new connections in a new thread. We have a thread per upstream connection
				Thread newConn = new Thread(new ControlHandlerWorker(controlServerSocket.accept(), owner));
				newConn.start();
			}
			controlServerSocket.close();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> ControlHandler. While listening incoming conns "+io.getMessage());
			io.printStackTrace();
		}
	}
	
}	
