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
package uk.ac.imperial.lsds.seep.reliable;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;

public class BackupHandler implements Runnable{
	
	//The core that owns this control handler
	private CoreRE owner;
	//The connection port that this backupHandler must use
	private int connPort;
	//This variable controls if this Runnable should keep running or not
	private boolean goOn;
	//Variable to control if a session is running or not
	private AtomicBoolean isSessionClosed = new AtomicBoolean(true);
	
	// Session related variables
	private String sessionName = null;
	private int transNumber = -1;
	private String lastSessionName = null;
	
	private long s_sessiontime = 0;

	private HashMap<InetAddress, BackupSessionInfo> openSessions = new HashMap<InetAddress, BackupSessionInfo>();
	private HashMap<Integer, ArrayList<FileChannel>> sessionHandlers = new HashMap<Integer, ArrayList<FileChannel>>();
	// To access rw files with backups
	private HashMap<Integer, ArrayList<FileChannel>> backupSessionHandlers = new HashMap<Integer, ArrayList<FileChannel>>();
	// To access the same files as above, with the possibility of removing them from the OS file system
	private HashMap<Integer, ArrayList<File>> sessionHandlersGCFiles = new HashMap<Integer, ArrayList<File>>();
	private HashMap<Integer, ArrayList<File>> backupSessionHandlersGCFiles = new HashMap<Integer, ArrayList<File>>();
	private HashMap<Integer, String> lastSessionNames = new HashMap<Integer, String>();
	
//	//Variables to keep the backup handler
//	private ArrayList<MappedByteBuffer> lastBackupHandlers = new ArrayList<MappedByteBuffer>();
//	private ArrayList<MappedByteBuffer> backupLastBackupHandlers = new ArrayList<MappedByteBuffer>();
	
	public CoreRE getOwner(){
		return owner;
	}
	
	public void setOwner(CoreRE owner){
		this.owner = owner;
	}

	public boolean getGoOn(){
		return goOn;
	}

	public void setGoOn(boolean goOn){
		this.goOn = goOn;
	}
	
	public String getLastBackupSessionName(int opId){
		return lastSessionNames.get(opId);
	}
	
	public BackupHandler(CoreRE owner, int port) {
		this.owner = owner;
		this.connPort = port;
		this.goOn = true;
		File newFile = new File("backup/");
		newFile.mkdirs();
	}
	
	public void openSession(int opId, InetAddress remoteAddress){
		NodeManager.nLogger.info("New Backup session opened for OP: "+opId);
		s_sessiontime = System.currentTimeMillis();
		// We name the new session
		sessionName = new Long(System.currentTimeMillis()).toString();
		transNumber = -1;
		
		ArrayList<FileChannel> lastBackupHandlers = new ArrayList<FileChannel>();
		ArrayList<File> lastBackupHandlersGCFiles = new ArrayList<File>();
		// We backup the previous handlers if there are any, keeping association with the operator establishing the connection
		if(sessionHandlers.containsKey(opId)){
			backupSessionHandlers.put(opId, sessionHandlers.get(opId));
			backupSessionHandlersGCFiles.put(opId, sessionHandlersGCFiles.get(opId));
		}
		// And we put the new ones here
		sessionHandlers.put(opId, lastBackupHandlers);
		sessionHandlersGCFiles.put(opId, lastBackupHandlersGCFiles);
		BackupSessionInfo bsi = new BackupSessionInfo(opId, lastBackupHandlers, this, sessionName, transNumber);
		// We log the open session, identifying it with the IP
		openSessions.put(remoteAddress, bsi);
		System.out.println("NEW SESSION: "+remoteAddress.toString());
	}
	
	public void closeSession(int opId, InetAddress remoteAddress){
		lastSessionNames.put(opId, openSessions.get(remoteAddress).getSessionName());
		openSessions.remove(remoteAddress);
		System.out.println("TOTAL SESSION TIME: "+(System.currentTimeMillis() - s_sessiontime));
		
		// If the session went well, then we get rid of the old files
		if(backupSessionHandlers.containsKey(opId)){
			ArrayList<FileChannel> oldFiles = backupSessionHandlers.get(opId);
			for(FileChannel f : oldFiles){
//				try {
//					f.close();
//				} 
//				catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
			ArrayList<File> toRemove = backupSessionHandlersGCFiles.get(opId);
			for(File f : toRemove){
//				f.delete();
			}
		}
	}

	public void addBackupHandler(int opId, FileChannel fc, File f){
		sessionHandlers.get(opId).add(fc);
		sessionHandlersGCFiles.get(opId).add(f);
	}

	@Override
	public void run() {
		ServerSocket backupServerSocket = null;
		try{
			//Establish listening port
    		backupServerSocket = new ServerSocket(connPort);
			NodeManager.nLogger.info("-> BackupHandler listening in port: "+connPort);
			NodeManager.nLogger.info("-> BackupHandler is waiting for opening session");
			//while goOn is active
			while(goOn){
				Socket incomingConn = backupServerSocket.accept();
				InetAddress incomingAddr = incomingConn.getInetAddress();
				if(openSessions.containsKey(incomingAddr)){
					BackupSessionInfo bsi = openSessions.get(incomingAddr);
					bsi.incrementTransNumber();
					// With an opened session we wait for connections and pass the sessionName and the transmission number
					BackupHandlerWorker bhw = new BackupHandlerWorker(bsi.getOpId(), incomingConn, this, bsi.getSessionName(), bsi.getTransNumber());
					///\todo{Reduce the overhead of the thread creation at this point. Use a pool or reuse the same worker}
					Thread newConn = new Thread(bhw);
					newConn.start();
				}
				else{
					NodeManager.nLogger.warning("Sent backup chunk from OPID. SESSION CLOSED HERE: "+incomingAddr);
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


//public ArrayList<MappedByteBuffer> getBackupHandler(){
//	if(isSessionClosed.get()){
//		return lastBackupHandlers;
//	}
//	else{
//		//this option or better yet: wait for current session to finish
//		return backupLastBackupHandlers;
//	}
//}


//public void _closeSession(int opId, SocketAddress remoteAddress){
//// We reset the transNumber for the next session
////transNumber = 0;
//// We configure the last session name as the one just finished
//lastSessionName = sessionName;
//isSessionClosed.set(true);
//System.out.println("TOTAL SESSION TIME: "+(System.currentTimeMillis() - s_sessiontime));
//}
