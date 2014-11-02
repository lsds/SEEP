package uk.ac.imperial.lsds.seepmaster.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.MasterWorkerAPI;

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
		listener = new Thread(new CommMasterWorker());
		listener.setName(CommMasterWorker.class.getName());
	}
	
	public void start(){
		this.working = true;
		this.listener.start();
	}
	
	public void stop(){
		//TODO: do some other cleaning work here
		this.working = false;
	}
	
	class CommMasterWorker implements Runnable{

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
						//TODO: indicate error here
					}
					LOG.info("Received command: {}", command);
					String[] commandTokens = command.split(" ");
					String commandCode = commandTokens[0];
					Map<String, String> commandArguments = MasterWorkerAPI.arrayToMap(commandTokens);
					MasterWorkerAPI.API apiInstance = MasterWorkerAPI.getAPIByName(command);
					if(apiInstance != null){
						//TODO: indicate error here, no such command
						if (! MasterWorkerAPI.validatesCommand(apiInstance, commandArguments)){
							//TODO: indicate error here, args do not validate
						}
					}
					
					if (MasterWorkerAPI.API.BOOTSTRAP.commandName().equals(commandCode)) {
						LOG.info("Bootstrap command");
						api.bootstrapCommand(commandArguments);
					}
					else if (MasterWorkerAPI.API.CRASH.commandName().equals(commandCode)) {
						LOG.info("Crash command");
						//TODO: implement
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
