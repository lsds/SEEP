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
package uk.ac.imperial.lsds.seep.infrastructure;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

import uk.ac.imperial.lsds.seep.comm.NodeManagerCommunication;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.Monitor;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.State;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.utils.dynamiccodedeployer.ExtendedObjectInputStream;
import uk.ac.imperial.lsds.seep.utils.dynamiccodedeployer.RuntimeClassLoader;

/**
 * NodeManager. This is the entity that controls the system info associated to a given node, for instance, the monitor of the node, and the 
 * operators that are within that node.
 */

public class NodeManager{
	
	private WorkerNodeDescription nodeDescr;
	private RuntimeClassLoader rcl = null;
	
	//Endpoint of the central node
	private int bindPort;
	private InetAddress bindAddr;
	//Bind port of this NodeManager
	private int ownPort;
	
	public static Logger nLogger = Logger.getLogger("seep");
	private NodeManagerCommunication bcu = new NodeManagerCommunication();
	
	static public boolean monitorOfSink = false;
	static public long clock = 0;
	static public Monitor nodeMonitor = new Monitor();
	static public int second;
	static public double throughput;
		
	private Thread monitorT = null;
	
	public NodeManager(int bindPort, InetAddress bindAddr, int ownPort) {
		this.bindPort = bindPort;
		this.bindAddr = bindAddr;
		this.ownPort = ownPort;
		try {
			nodeDescr = new WorkerNodeDescription(InetAddress.getLocalHost(), ownPort);
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		rcl = new RuntimeClassLoader(new URL[0], this.getClass().getClassLoader());
	}
	
	/// \todo{the client-server model implemented here is crap, must be refactored}
	static public void setSystemStable(){
		String command = "systemStable \n";
		try{
			Socket connMaster = new Socket(InetAddress.getByName("146.169.5.130"), Integer.parseInt("3500"));
			OutputStream os = connMaster.getOutputStream();
			os.write(command.getBytes());
			System.out.println("finished method!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			Thread.sleep(100);
			os.close();
		}
		catch(UnknownHostException uhe){
			System.out.println("NodeManager.setSystemStable: "+uhe.getMessage());
			uhe.printStackTrace();
		}
		catch(IOException io){
			System.out.println("NodeManager.setSystemStable: "+io.getMessage());
			io.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void init(){
		//Get unique identifier for this node
		int nodeId = nodeDescr.getNodeId();
		//Initialize node engine ( CoreRE + ProcessingUnit )
		CoreRE core = new CoreRE(nodeDescr, rcl);
		//Initialize monitor
		nodeMonitor.setNodeId(nodeId);
		monitorT = new Thread(nodeMonitor);
		monitorT.start();
		nLogger.info("-> Node Monitor running");
		//Local variables
		ServerSocket serverSocket = null;
		PrintWriter out = null;
		ExtendedObjectInputStream ois = null;
		
		Object o = null;
		boolean listen = true;
		
		try{
			serverSocket = new ServerSocket(ownPort);
			NodeManager.nLogger.info("NODEMANAGER: Waiting for incoming requests on port: "+ownPort);
			Socket clientSocket = null;
			//Send bootstrap information
			bcu.sendBootstrapInformation(bindPort, bindAddr, ownPort);
			while(listen){
				//Accept incoming connections
				clientSocket = serverSocket.accept();
				//Establish output stream
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				//Establish input stream, which receives serialized objects
				ois = new ExtendedObjectInputStream(clientSocket.getInputStream(), rcl);
				//Read the serialized object sent.
				ObjectStreamClass osc = ois.readClassDescriptor();
				//Lazy load of the required class in case is an operator
				if(!(osc.getName().equals("java.lang.String")) && !(osc.getName().equals("java.lang.Integer"))){
					NodeManager.nLogger.info("-> Received Unknown Class ->"+osc.getName()+"<- Using custom class loader to resolve it");
					rcl.loadClass(osc.getName());
					o = ois.readObject();
					if(o instanceof Operator){
						NodeManager.nLogger.info("-> OPERATOR resolved, OP-ID: "+((Operator)o).getOperatorId());
					}
					else if (o instanceof State){
						NodeManager.nLogger.info("-> STATE resolved, Class: "+o.getClass().getName());
					}
				}
				else{
					o = ois.readObject();
				}
				//Check the class of the object received and initialized accordingly
				if(o instanceof Operator){
					core.pushOperator((Operator)o);
				}
				else if(o instanceof Integer){
					core.setOpReady((Integer)o);
				}
				else if(o instanceof ArrayList<?>){
					core.pushStarTopology((ArrayList<EndPoint>)o);
				}
				else if(o instanceof String){
					String tokens[] = ((String)o).split(" ");
					System.out.println("Tokens received: "+tokens[0]);
					if(tokens[0].equals("CODE")){
						NodeManager.nLogger.info("-> CODE Command");
						//Send ACK back
						out.println("ack");
						// Establish subconnection to receive the code
						NodeManager.nLogger.info("-> Waiting for receiving the file");
						Socket subConnection = serverSocket.accept();
						DataInputStream dis = new DataInputStream(subConnection.getInputStream());
						int codeSize = dis.readInt();
						byte[] serializedFile = new byte[codeSize];
						dis.readFully(serializedFile);
						int bytesRead = serializedFile.length;
						if(bytesRead != codeSize){
							NodeManager.nLogger.warning("Mismatch between read and file size");
						}
						else{
							NodeManager.nLogger.info("-> CODE received completely");
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
							NodeManager.nLogger.info("-> Loading CODE from: "+pathToCode.getAbsolutePath());
							loadCodeToRuntime(pathToCode);
						}
						else{
							NodeManager.nLogger.severe("-> No access to the CODE");
						}
					}
					if(tokens[0].equals("STOP")){
						NodeManager.nLogger.info("-> STOP Command");
						core.stopDataProcessing();
						listen = false;
						out.println("ack");
						o = null;
						ois.close();
						out.close();
						clientSocket.close();
						//since listen=false now, finish the loop
						continue;
					}
					if(tokens[0].equals("SET-RUNTIME")){
						NodeManager.nLogger.info("-> SET-RUNTIME Command");
						core.setRuntime();
						out.println("ack");
					}
					if(tokens[0].equals("START")){
						NodeManager.nLogger.info("-> START Command");
                        //We call the processData method on the source
                        /// \todo {Is START used? is necessary to answer with ack? why is this not using startOperator?}
                        out.println("ack");
                        core.startDataProcessing();
					}
					if(tokens[0].equals("CLOCK")){
						NodeManager.nLogger.info("-> CLOCK Command");
						NodeManager.clock = System.currentTimeMillis();
						out.println("ack");
					}
				}
				//Send message back.
				out.println("ack");
				o = null;
				ois.close();
				out.close();
				clientSocket.close();
			}
			serverSocket.close();
		}
		//For now send nack, probably this is not the best option...
		catch(IOException io){
			System.out.println("IOException: "+io.getMessage());
			io.printStackTrace();
		}
		catch(IllegalThreadStateException itse){
			System.out.println("IllegalThreadStateException, no problem, monitor thing");
			itse.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		catch (SecurityException e) {
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
	}
	
	private void loadCodeToRuntime(File pathToCode){
		URL urlToCode = null;
		try {
			urlToCode = new URL("file://"+pathToCode.getAbsolutePath());
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
