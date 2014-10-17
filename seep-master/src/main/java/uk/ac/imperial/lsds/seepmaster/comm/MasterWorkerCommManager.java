package uk.ac.imperial.lsds.seepmaster.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterWorkerCommManager {

	final private Logger LOG = LoggerFactory.getLogger(MasterWorkerCommManager.class.getName());
	
	private ServerSocket serverSocket;
	private Thread listener;
	private boolean working = false;
	private MasterWorkerAPIImplementation api;
	
	public MasterWorkerCommManager(int port, MasterWorkerAPIImplementation api){
		this.api = api;
		try {
			serverSocket = new ServerSocket(port);
			LOG.info(" Listening on {}:{}", InetAddress.getLocalHost(), port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listener = new Thread(new CommManagerWorker());
	}
	
	public void startListening(){
		this.working = true;
		this.listener.start();
	}
	
	public void stopListening(){
		//TODO: do some other cleaning work here
		this.working = false;
	}
	
	class CommManagerWorker implements Runnable{

		@Override
		public void run() {
			while(working){
				BufferedReader bis = null;
				Socket incomingSocket = null;
				try{
					// Blocking call
					incomingSocket = serverSocket.accept();
					bis = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));
					String command = bis.readLine();
					if(command == null){
						//TODO: inidicate error here
					}
					String[] commandTokens = command.split(" ");
					String commandCode = commandTokens[0];
					switch(commandCode){
					case MasterWorkerAPI.BOOTSTRAP:
						LOG.info("Bootstrap command");
						if(MasterWorkerAPI.validatesCommand(MasterWorkerAPI.BOOTSTRAP, commandTokens)){
							
						}
						break;
					case MasterWorkerAPI.CRASH:
						LOG.info("Crash command");
						if(MasterWorkerAPI.validatesCommand(MasterWorkerAPI.CRASH, commandTokens)){
							
						}
					}
					
				}
				catch(IOException io){
					io.printStackTrace();
				}
				finally {
					if (incomingSocket != null){
						try {
							incomingSocket.close();
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}	
		}
	}
}
