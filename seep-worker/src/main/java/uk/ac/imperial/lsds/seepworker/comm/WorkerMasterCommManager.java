package uk.ac.imperial.lsds.seepworker.comm;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.api.PhysicalSeepQuery;
import uk.ac.imperial.lsds.seep.comm.MasterWorkerAPI;
import uk.ac.imperial.lsds.seep.infrastructure.ExtendedObjectInputStream;
import uk.ac.imperial.lsds.seep.infrastructure.RuntimeClassLoader;

public class WorkerMasterCommManager {

	final private Logger LOG = LoggerFactory.getLogger(WorkerMasterCommManager.class.getName());
	
	private ServerSocket serverSocket;
	private Thread listener;
	private boolean working = false;
	private WorkerMasterAPIImplementation api;
	private RuntimeClassLoader rcl;
	
	public WorkerMasterCommManager(int port, WorkerMasterAPIImplementation api, RuntimeClassLoader rcl){
		this.api = api;
		this.rcl = rcl;
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
				ExtendedObjectInputStream ois = null;
				Socket incomingSocket = null;
				PrintWriter out = null;
				try{
					// Blocking call
					incomingSocket = serverSocket.accept();
					// Establish input stream, which receives serialized objects
					ois = new ExtendedObjectInputStream(incomingSocket.getInputStream(), rcl);
					out = new PrintWriter(incomingSocket.getOutputStream(), true);
					// Read the serialized object sent.
					ObjectStreamClass osc = ois.readClassDescriptor();
					Object o = ois.readObject();
					
					if(!osc.getName().equals("java.lang.String")){
						// Dynamically loading all received classes
						rcl.loadClass(osc.getName());
						PhysicalSeepQuery psq = (PhysicalSeepQuery)o;
						System.out.println(psq);
						// do something with this
					}

					String command = (String)o;
					
					if(command == null){
						//TODO: indicate error here
					}
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
					
					if (MasterWorkerAPI.API.CODE.commandName().equals(commandCode)) {
						// FIXME: eliminate hardcoded names, variables, etc, get from config, working-directory is useful here
						// We handle this locally as it requires access to the connection
						LOG.info("Code command");
						out.print("ack");
						LOG.info("-> Waiting for receiving the CODE...");
						Socket subConnection = serverSocket.accept();
						DataInputStream dis = new DataInputStream(subConnection.getInputStream());
						int codeSize = dis.readInt();
						byte[] serializedFile = new byte[codeSize];
						dis.readFully(serializedFile);
						int bytesRead = serializedFile.length;
						if(bytesRead != codeSize){
							LOG.warn("Mismatch between read and file size");
						}
						else{
							LOG.info("-> CODE received completely");
						}
						//Here I have the serialized bytes of the file, we materialize the real file
						//For now the name of the file is always query.jar
						FileOutputStream fos = new FileOutputStream(new File("query.jar"));
						fos.write(serializedFile);
						fos.close();
						dis.close();
						subConnection.close();
						out.println("ack");
						//At this point we should have the file on disk
						File pathToCode = new File("query.jar");
						if(pathToCode.exists()){
							LOG.info("-> Loading CODE from: {}", pathToCode.getAbsolutePath());
							loadCodeToRuntime(pathToCode);
							/**
							 * if we get all the names of the classes to load, we can iterate and load them now, right?
							 */
						}
						else{
							LOG.error("-> No access to the CODE");
						}
					}
					
//					// Lazy load of the required class in case is an operator
//					if(!(osc.getName().equals("java.lang.String")) && !(osc.getName().equals("java.lang.Integer"))){
//						LOG.debug("-> Received Unknown Class -> {} <- Using custom class loader to resolve it", osc.getName());
//						rcl.loadClass(osc.getName());
//						o = ois.readObject();
//						if(o instanceof Operator){
//							LOG.debug("-> OPERATOR resolved, OP-ID: {}", ((Operator)o).getOperatorId());
//		                }
//						else if (o instanceof StateWrapper){
//							LOG.info("-> STATE resolved, Class: {}", o.getClass().getName());
//						}
//		                
//		                out.println("ack");
//		                out.flush();
//					}
//					else{
//						o = ois.readObject();
//					}
					
				}
				catch(IOException io){
					io.printStackTrace();
				} 
				catch (ClassNotFoundException cnfe) {
					cnfe.printStackTrace();
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
	
	private void loadCodeToRuntime(File pathToCode){
		URL urlToCode = null;
		try {
			urlToCode = pathToCode.toURI().toURL();
			System.out.println("Loading into class loader: "+urlToCode.toString());
			URL[] urls = new URL[1];
			urls[0] = urlToCode;
			rcl.addURL(urlToCode);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
