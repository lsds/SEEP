package seep.comm;

import java.io.IOException;
import java.net.ServerSocket;

import seep.infrastructure.NodeManager;
import seep.infrastructure.monitor.MetricsReader;
import seep.runtimeengine.CoreRE;

/**
* IncomingDataHandler. This is in charge of managing incoming data connections and associate a thread to them
*/

public class IncomingDataHandler implements Runnable{

	//private Operator owner;
	private CoreRE owner;
	private int connPort;
	private boolean goOn;

	public int getConnPort(){
		return connPort;
	}

	public void setConnPort(int connPort){
		this.connPort = connPort;
	}

	public IncomingDataHandler(CoreRE owner, int connPort){
		this.owner = owner;
		this.connPort = connPort;
		//this.selector = initSelector();
		this.goOn = true;
	}

	public void run(){
		ServerSocket incDataServerSocket = null;
		try{
			//Establish listening port
			incDataServerSocket = new ServerSocket(connPort);
			incDataServerSocket.setReuseAddress(true);
			NodeManager.nLogger.info("-> IncomingDataHandler listening in port: "+connPort);
			//Upstream id
			int uid = 0;
			while(goOn){
				Thread newConn = new Thread(new IncomingDataHandlerWorker(uid, incDataServerSocket.accept(), owner));
				newConn.start();
				MetricsReader.numberIncomingDataHandlerWorkers.inc();
				uid++;
			}
			incDataServerSocket.close();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> IncomingDataHandler. While listening incoming conns "+io.getMessage());
			io.printStackTrace();
		}
	}
}