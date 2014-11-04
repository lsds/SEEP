package uk.ac.imperial.lsds.seepmaster.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.KryoFactory;
import uk.ac.imperial.lsds.seep.comm.protocol.BootstrapCommand;
import uk.ac.imperial.lsds.seep.comm.protocol.Command;
import uk.ac.imperial.lsds.seep.comm.protocol.ProtocolAPI;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public class MasterWorkerCommManager {

	final private Logger LOG = LoggerFactory.getLogger(MasterWorkerCommManager.class.getName());
	
	private ServerSocket serverSocket;
	private Kryo k;
	private Thread listener;
	private boolean working = false;
	private MasterWorkerAPIImplementation api;
	
	public MasterWorkerCommManager(int port, MasterWorkerAPIImplementation api){
		this.api = api;
		this.k = KryoFactory.buildKryoForMasterWorkerProtocol();
		try {
			serverSocket = new ServerSocket(port);
			LOG.info(" Listening on {}:{}", InetAddress.getLocalHost(), port);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		listener = new Thread(new CommMasterWorker());
		listener.setName(CommMasterWorker.class.getName());
		// TODO: set uncaughtexceptionhandler
	}
	
	public void start(){
		this.working = true;
		LOG.info("Start MasterWorkerCommManager");
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
				Input i = null;
				Socket incomingSocket = null;
				try{
					// Blocking call
					incomingSocket = serverSocket.accept();
					
					InputStream is = incomingSocket.getInputStream();
					i = new Input(is);
					
					Command command = k.readObject(i, Command.class);
					short type = command.type();
					
					if(type == ProtocolAPI.BOOTSTRAP.type()){
						LOG.info("RX-> Bootstrap command");
						BootstrapCommand bc = command.getBootstrapCommand();
						api.bootstrapCommand(bc);
					}
					else if(type == ProtocolAPI.CRASH.type()){
						LOG.info("RX-> Crash command");
					}
					
//					bis = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));
//					String command = bis.readLine();
//					if(command == null){
//						//TODO: indicate error here
//					}
//					LOG.info("Received command: {}", command);
//					String[] commandTokens = command.split(" ");
//					String commandCode = commandTokens[0];
//					Map<String, String> commandArguments = MasterWorkerAPI.arrayToMap(commandTokens);
//					MasterWorkerAPI.API apiInstance = MasterWorkerAPI.getAPIByName(command);
//					if(apiInstance != null){
//						//TODO: indicate error here, no such command
//						if (! MasterWorkerAPI.validatesCommand(apiInstance, commandArguments)){
//							//TODO: indicate error here, args do not validate
//						}
//					}
					
//					if (MasterWorkerAPI.API.BOOTSTRAP.commandName().equals(commandCode)) {
//						LOG.info("Bootstrap command");
//						api.bootstrapCommand(commandArguments);
//					}
//					else if (MasterWorkerAPI.API.CRASH.commandName().equals(commandCode)) {
//						LOG.info("Crash command");
//						//TODO: implement
//					}
					
				}
				catch(IOException io){
					io.printStackTrace();
				}
				finally {
					if (incomingSocket != null){
						try {
							i.close();
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
