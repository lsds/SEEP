/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Changes to support scale-in of operators
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure;

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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.AsyncTask;
import uk.ac.imperial.lsds.seep.comm.NodeManagerCommunication;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.ExtendedObjectInputStream;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.RuntimeClassLoader;
import uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTuple;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.MonitorSlave;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.MonitorSlaveFactory;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.OperatorStaticInformation;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

/**
 * NodeManager. This is the entity that controls the system info associated to a given node, for instance, the monitor of the node, and the 
 * operators that are within that node.
 */

public class NodeManager{

	final private Logger LOG = LoggerFactory.getLogger(NodeManager.class);

	private WorkerNodeDescription nodeDescr;
	private RuntimeClassLoader rcl = null;

	//Endpoint of the central node
	private int bindPort;
	private InetAddress bindAddr;
	//Bind port of this NodeManager
	private int ownPort;
	private NodeManagerCommunication bcu = new NodeManagerCommunication();

	static public boolean monitorOfSink = false;
	static public long clock = 0;
	static public MonitorSlave monitorSlave;
	static public int second;
	static public double throughput;

	private Thread monitorT = null;

	public NodeManager(int bindPort, InetAddress bindAddr, int ownPort) {
		this.bindPort = bindPort;
		this.bindAddr = bindAddr;

		this.ownPort = ownPort;
		nodeDescr = new WorkerNodeDescription("127.0.0.1", ownPort);


		rcl = new RuntimeClassLoader(new URL[0], this.getClass().getClassLoader());
	}

	/// \todo{the client-server model implemented here is crap, must be refactored}
	static public void setSystemStable(){
		MetricsTuple tuple = new MetricsTuple();
		tuple.setOperatorId(Infrastructure.RESET_SYSTEM_STABLE_TIME_OP_ID);
		monitorSlave.pushMetricsTuple(tuple);
	}

	public void init() {

		Thread thread1 = new Thread(new Runnable(){
			@Override
			public void run() {
				// Get unique identifier for this node
				int nodeId = nodeDescr.getNodeId();
				//Initialize node engine ( CoreRE + ProcessingUnit )
				CoreRE core = new CoreRE(nodeDescr, rcl);

				//Local variables
				ServerSocket serverSocket = null;
				PrintWriter out = null;
				ExtendedObjectInputStream ois = null;
				//	ObjectStreamClass osc = null;
				Object o = null;
				boolean listen = true;
				try{
					serverSocket = new ServerSocket(ownPort);
					LOG.info("Waiting for incoming requests on port: {}", ownPort);
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
						//	osc = ois.readClassDescriptor();

						//Lazy load of the required class in case is an operator

						//	if(!(osc.getName().equals("java.lang.String")) && !(osc.getName().equals("java.lang.Integer")) && 
						//			!(osc.getName().equals("java.util.ArrayList"))){
						//		LOG.info("-> Received Unknown Class -> {} <- Using custom class loader to resolve it", osc.getName());
						//rcl.loadClass(osc.getName());
						//		Class a = NodeManager.class.getClassLoader().loadClass(osc.getName());
						o = null;
						o = ois.readObject();
						if(o instanceof Operator){
							LOG.info("-> OPERATOR resolved, OP-ID: {}", ((Operator)o).getOperatorId());
							// Initialize monitor slave, start thread, we do it at
							// this stage because we need to know the node is running an operator
				//			int operatorId = ((Operator)o).getOperatorId();

				//			MonitorSlaveFactory factory = new MonitorSlaveFactory(operatorId);
				//			monitorSlave = factory.create();

				//			monitorT = new Thread(monitorSlave);
				//			monitorT.start();

				//			LOG.info("-> Node Monitor running for operatorId={}", operatorId);

							core.pushOperator((Operator)o);

							out.println("ack");
							out.flush();

						}
						else if (o instanceof StateWrapper){
							LOG.info("-> STATE resolved, Class: {}", o.getClass().getName());
							out.println("ack");			
							out.flush();
						}
						else if(o instanceof ArrayList<?>){
							LOG.info("-> Start topology applied");

							core.pushStarTopology((ArrayList<EndPoint>)o);
							out.println("ack");
							out.flush();

						}
						else if(o instanceof Integer){
							LOG.info("-> SetOpReady: "+(Integer)o);

							core.setOpReady((Integer)o);

							out.println("ack");
							out.flush();

						}
						else if(o instanceof String){
							String tokens[] = ((String)o).split(" ");

							LOG.debug("Tokens received: " +tokens[0]);
							if(tokens[0].equals("CODE")){
								LOG.info("-> CODE Command");
								//Send ACK back
								out.println("ack");
								// Establish subconnection to receive the code
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
								}
								else{
									LOG.error("-> No access to the CODE");
								}
							}
							if(tokens[0].equals("STOP")){
								LOG.info("-> STOP Command");
								core.stopDataProcessing();

								// Stop the monitoring slave, this node is stopping
								if (monitorSlave != null) {
									monitorSlave.stop();
								}

								listen = false;

								LOG.info("Sending ACK message back to the master");
								out.println("ack");
								out.flush();

								//since listen=false now, finish the loop
								continue;
							}
							if(tokens[0].equals("SET-RUNTIME")) {
								LOG.info("-> SET-RUNTIME Command");
								core.setRuntime();
								out.println("ack");
							}
							if(tokens[0].equals("START")){
								LOG.info("-> START Command");
								core.startDataProcessingAsync();

								//We call the processData method on the source
								/// \todo {Is START used? is necessary to answer with ack? why is this not using startOperator?}
								out.println("ack");
							}
							if(tokens[0].equals("CLOCK")){
								LOG.info("-> CLOCK Command");
								NodeManager.clock = System.currentTimeMillis();
								out.println("ack");
							}
						}

						o = null;
						ois.close();
						out.close();
						clientSocket.close();
					}

					LOG.info("Waiting before stopping manager and terminating this process");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ex) {
						LOG.error("Unable to wait for 5 seconds");
					}

					serverSocket.close();
					System.exit(0);
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
				catch (SecurityException e) {
					e.printStackTrace();
				} 
				catch (IllegalArgumentException e) {
					e.printStackTrace();
				} 
				catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		thread1.start(); 
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

