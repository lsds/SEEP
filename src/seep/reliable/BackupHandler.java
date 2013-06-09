package seep.reliable;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import seep.infrastructure.NodeManager;
import seep.runtimeengine.CoreRE;

public class BackupHandler implements Runnable{
	
	//The core that owns this control handler
	private CoreRE owner;
	//The connection port that this controlhandler must use
	private int connPort;
	//This variable controls if this Runnable should keep running or not
	private boolean goOn;
	//Variable to control is a session is running or not
	private AtomicBoolean isSessionClosed = new AtomicBoolean(true);
	
	// Session related variables
	private String sessionName = null;
	private int transNumber = -1;
	private String lastSessionName = null;
	
	//Variables to keep the backup handler
	private ArrayList<MappedByteBuffer> lastBackupHandlers = new ArrayList<MappedByteBuffer>();
	private ArrayList<MappedByteBuffer> backupLastBackupHandlers = new ArrayList<MappedByteBuffer>();
	
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
	
	public String getLastBackupSessionNane(){
		return lastSessionName;
	}
	
	
	public BackupHandler(CoreRE owner, int port) {
		this.owner = owner;
		this.connPort = port;
		this.goOn = true;
	}
	
	long s_sessiontime = 0;
	
	public void openSession(){
		s_sessiontime = System.currentTimeMillis();
		// We let this to open connections
		isSessionClosed.set(false);
		// We name the new session
		sessionName = new Long(System.currentTimeMillis()).toString();
		transNumber = -1;
		
		// We keep a backup of the last backup file handlers
		backupLastBackupHandlers = new ArrayList<MappedByteBuffer>(lastBackupHandlers);
		lastBackupHandlers.clear();
		synchronized(this){
			this.notify();
		}
	}
	
	public void closeSession(){
		// We reset the transNumber for the next session
//		transNumber = 0;
		// We configure the last session name as the one just finished
		lastSessionName = sessionName;
		isSessionClosed.set(true);
		System.out.println("TOTAL SESSION TIME: "+(System.currentTimeMillis() - s_sessiontime));
	}

	public void addBackupHandler(MappedByteBuffer mbb){
		this.lastBackupHandlers.add(mbb);
	}
	
	public ArrayList<MappedByteBuffer> getBackupHandler(){
		if(isSessionClosed.get()){
			return lastBackupHandlers;
		}
		else{
			//this option or better yet: wait for current session to finish
			return backupLastBackupHandlers;
		}
	}
	
	@Override
	public void run() {
		ServerSocket backupServerSocket = null;
		try{
			//Establish listening port
    		backupServerSocket = new ServerSocket(connPort);
			NodeManager.nLogger.info("-> BackupHandler listening in port: "+connPort);
			//while goOn is active
			while(goOn){
				NodeManager.nLogger.info("-> BackupHandler is waiting for opening session");
				// We check if a session is closed, and wait, or open, and receive stuff
				if(isSessionClosed.get()){
					// Closed session, we reset the transmission number and wait
//					transNumber = -1;
					synchronized(this){
						try {
							this.wait();
						}
						catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else{
					NodeManager.nLogger.info("New Backup session opened");
					transNumber++;
					// With an opened session we wait for connections and pass the sessionName and the transmission number
					BackupHandlerWorker bhw = new BackupHandlerWorker(backupServerSocket.accept(), this, sessionName, transNumber);
					Thread newConn = new Thread(bhw);
					newConn.start();
				}
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
