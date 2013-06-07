package seep.reliable;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import seep.infrastructure.NodeManager;
import seep.runtimeengine.CoreRE;

public class BackupHandler implements Runnable{
	
	//The core that owns this control handler
	private CoreRE owner;
	//The connection port that this controlhandler must use
	private int connPort;
	//This variable controls if this Runnable should keep running or not
	private boolean goOn;
	// Keeps a relation between node port and file name
	private Map<String, String> mapNode = new HashMap<String, String>();

	public CoreRE getOwner(){
		return owner;
	}
	
	public void setOwner(CoreRE owner){
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
	
	
	public BackupHandler(CoreRE owner, int port) {
		this.owner = owner;
		this.connPort = port;
		this.goOn = true;
	}

	@Override
	public void run() {
		ServerSocket backupServerSocket = null;
		try{
			//Establish listening port
    		backupServerSocket = new ServerSocket(connPort);
//			backupServerSocket.setReuseAddress(true);
			NodeManager.nLogger.info("-> BackupHandler listening in port: "+connPort);
			//while goOn is active
			while(goOn){
				//Place new connections in a new thread. We have a thread per upstream connection
//				String machine = backupServerSocket.getInetAddress().getHostAddress();
				String file = null;
//				if(mapNode.containsKey(machine)){
//					file = mapNode.get(machine);
//				}
				Thread newConn = new Thread(new BackupHandlerWorker(backupServerSocket.accept(), owner, file));
				
				newConn.start();
			}
			backupServerSocket.close();
		}
		catch(BindException be){
			NodeManager.nLogger.severe("-> BIND EXC IO Error "+be.getMessage());
			NodeManager.nLogger.severe("-> backupServerSocket.toString: "+backupServerSocket.toString());
			be.printStackTrace();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("-> BackupHandler. While listening incoming conns "+io.getMessage());
			io.printStackTrace();
		}
	}
}
