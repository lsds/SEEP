package seep.reliable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;

import seep.infrastructure.NodeManager;
import seep.runtimeengine.CoreRE;

public class BackupHandlerWorker implements Runnable{

	private Socket incomingSocket = null;
	private CoreRE owner = null;
	private boolean goOn = true;
	private String fileName = null;
	private File backupFile = null;
	
	private PipedOutputStream pos = null;
	private PipedInputStream pis = null;
	private PipeReader pr = null;
	
	private Thread pipeRH = null;
	
	public BackupHandlerWorker(Socket incomingSocket, CoreRE owner, String fileName) {
		this.incomingSocket = incomingSocket;
		this.owner = owner;
		// Create pipedStreams
		this.pos = new PipedOutputStream();
		this.pis = new PipedInputStream(1024);
		this.fileName = fileName;
	}
	
	public void init(){
		try {
			// Create backupFile
			if(fileName != null){
				backupFile = new File(fileName);
			}
			else{
				fileName = "_backup_"+incomingSocket.getInetAddress().toString();
				backupFile = new File(fileName);
			}
			// Connect pipes
			pis.connect(pos);
			
			pr = new PipeReader(pis, backupFile);
			pipeRH = new Thread(pr);
			pipeRH.start();
		} 
		catch (IOException e) {
			NodeManager.nLogger.severe("While connecting PIPE: "+e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.init();
//		while(goOn){
			byte[] buffer = new byte[1024];
			
			try {
				BufferedInputStream bis = new BufferedInputStream(incomingSocket.getInputStream());
				int bytesRead = 0;
	            while ((bytesRead = bis.read(buffer)) != -1) {
	                pos.write(buffer, 0, bytesRead);
	            }
	            pos.flush();
	            pos.close();
	            bis.close();
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		}
	}

}
