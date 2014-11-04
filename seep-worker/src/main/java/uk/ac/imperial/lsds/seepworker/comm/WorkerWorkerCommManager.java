package uk.ac.imperial.lsds.seepworker.comm;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.KryoFactory;
import uk.ac.imperial.lsds.seep.comm.protocol.WWCommand;
import uk.ac.imperial.lsds.seep.comm.protocol.WWProtocolAPI;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;


public class WorkerWorkerCommManager {

	final private Logger LOG = LoggerFactory.getLogger(WorkerWorkerCommManager.class.getName());
	
	private ServerSocket serverSocket;
	private Kryo k;
	private Thread listener;
	private boolean working = false;
	private WorkerWorkerAPIImplementation api;
	
	public WorkerWorkerCommManager(int port, WorkerWorkerAPIImplementation api){
		this.api = api;
		this.k = KryoFactory.buildKryoForWorkerWorkerProtocol();
		try {
			serverSocket = new ServerSocket(port);
			LOG.info(" Listening on {}:{}", InetAddress.getLocalHost(), port);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		this.listener = new Thread(new CommWorkerWorker());
		listener.setName(CommWorkerWorker.class.getName());
		// TODO: set uncaughtexceptionhandler
	}
	
	public void start(){
		working = true;
		this.listener.start();
	}
	
	public void stop(){
		// TODO: cleaning
		working = false;
	}
	
	class CommWorkerWorker implements Runnable{

		@Override
		public void run() {
			while(working){
				Input i = null;
				Socket incomingSocket = null;
				try{
					// Blocking call
					incomingSocket = serverSocket.accept();
					InputStream is = incomingSocket.getInputStream();
					i = new Input(is);
					WWCommand command = k.readObject(i, WWCommand.class);
					short type = command.type();
					
					if(type == WWProtocolAPI.ACK.type()){
						LOG.info("RX-> ACK command");
						
					}
					else if(type == WWProtocolAPI.CRASH.type()){
						LOG.info("RX-> Crash command");
						
					}
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
