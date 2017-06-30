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
package uk.ac.imperial.lsds.seep.infrastructure.master;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.elastic.IPNotBoundToOperatorException;
import uk.ac.imperial.lsds.seep.elastic.NodePoolEmptyException;
import uk.ac.imperial.lsds.seep.operator.Connectable;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.runtimeengine.DisposableCommunicationChannel;

/**
* ManagerWorker. This class implements runnable, it is in charge of listening to events from the running system.
*/

public class ManagerWorker implements Runnable {

	final private Logger LOG = LoggerFactory.getLogger(ManagerWorker.class);
	
	private Infrastructure inf = null;
	private ServerSocket managerS = null;
	private Socket clientSocket = null;

	private int refPort = 3500;
		
	private boolean goOn;

	private int port;
		
	public ManagerWorker(Infrastructure inf, int port){
		this.inf = inf;
		this.port = port;
	}

	private void crashCommand(String oldIP_txt, String oldPort_txt, String newIP_txt, String newPort_txt) throws UnknownHostException, IPNotBoundToOperatorException{
		//get opId from ip
		int opId = inf.getOpIdFromIp(InetAddress.getByName(oldIP_txt));
		System.out.println("OLD OP ID IS FROM: "+opId);
		if(opId == -1){
			throw new IPNotBoundToOperatorException("IP not bounded to an operator: "+oldIP_txt);
		}
		//get numDownstreams from opId
		int numOfUpstreams = inf.getNumUpstreams(opId);
		//set initial time of crash and number of downstreams
		Infrastructure.msh.setCrashInitialTime(System.currentTimeMillis(), numOfUpstreams);
		InetAddress oldIP = InetAddress.getByName(oldIP_txt);
		int oldPort = Integer.parseInt(oldPort_txt);
		InetAddress newIP = InetAddress.getByName(newIP_txt);
		int newPort = Integer.parseInt(newPort_txt);
		
		/// \todo{this case has never been tested}
		if(!(newIP.equals(oldIP) && newPort == oldPort)){
			System.out.println("MANAGER: Remapping communications, new IP");
		//remap could get nodes instead of IPs to build correct nodes, but
			//it also work just with IPs
			inf.reMap(oldIP,newIP);
		}
		System.out.println("MANAGER: Calling reDeploy... the IP: "+oldIP.toString());
		Node newNode = new Node(newIP,newPort);
		long init = System.currentTimeMillis();
System.out.println("updating star topology");
		// Update star topology. remove failed node and add new instantiated one.
		inf.removeNodeFromStarTopology(opId);
		inf.addNodeToStarTopology(opId, newIP);
		// Then broadcast the new star topology
System.out.println("broadcast star topology");
		inf.broadcastStarTopology();
		for(EndPoint ep: inf.getStarTopology()){
			System.out.println("Op: "+ep.getOperatorId()+" IP: "+((DisposableCommunicationChannel)ep).getIp().toString());
		}
		inf.reDeploy(newNode);
		long end = System.currentTimeMillis();
		System.out.println("INIT OP: "+(end-init));
		
		System.out.println("MANAGER: reDeploy ops in node IP: "+InetAddress.getByName(newIP_txt).toString());
		System.out.println("MANAGER: Updating upstream and downstream connections...");
		//updateU_D could get nodes instead of IPs to build correct nodes, but
		//it also work just with IPs
		
System.out.println("calling updateUD");
		inf.updateU_D(oldIP,newIP, false);
		
		Operator toInit = inf.getOperatorById(opId);
System.out.println("broadcast state and runtime init");
		inf.broadcastState(toInit);
		inf.initRuntime(toInit);
		
		// Tell star topology to stream state
		System.out.println("calling failure");
		inf.failure(opId);
	}
		
	private void crashCommandParallelRecovery(String oldIP_txt, String oldPort_txt, String newIP_txt, String newPort_txt) throws UnknownHostException, IPNotBoundToOperatorException{
		System.out.println("PARALLEL CRASH RECOVERY");
		//get opId from ip
		int opId = inf.getOpIdFromIp(InetAddress.getByName(oldIP_txt));
		System.out.println("OLD OP ID IS FROM: "+opId);
		if(opId == -1){
			throw new IPNotBoundToOperatorException("IP not bounded to an operator: "+oldIP_txt);
		}
		//get numDownstreams from opId
		int numOfUpstreams = inf.getNumUpstreams(opId);
		//set initial time of crash and number of downstreams
		Infrastructure.msh.setCrashInitialTime(System.currentTimeMillis(), numOfUpstreams);
		InetAddress oldIP = InetAddress.getByName(oldIP_txt);
		int oldPort = Integer.parseInt(oldPort_txt);
		InetAddress newIP = InetAddress.getByName(newIP_txt);
		int newPort = Integer.parseInt(newPort_txt);
		
		/// \todo{this case has never been tested}
		if(!(newIP.equals(oldIP) && newPort == oldPort)){
			System.out.println("MANAGER: Remapping communications, new IP");
			//remap could get nodes instead of IPs to build correct nodes, but
			//it also work just with IPs
			inf.reMap(oldIP,newIP);
		}
		System.out.println("MANAGER: Calling reDeploy... the IP: "+oldIP.toString());
		//Node newNode = new Node(newIP,newPort);
		Node newNode = null;
		try {
			newNode = inf.getNodeFromPool();
		} 
		catch (NodePoolEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long init = System.currentTimeMillis();
System.out.println("updating star topology");
		// Update star topology. remove failed node and add new instantiated one.
		inf.removeNodeFromStarTopology(opId);
		inf.addNodeToStarTopology(opId, newIP);
		// Then broadcast the new star topology
System.out.println("broadcast star topology");
		inf.broadcastStarTopology();
		for(EndPoint ep: inf.getStarTopology()){
			System.out.println("Op: "+ep.getOperatorId()+" IP: "+((DisposableCommunicationChannel)ep).getIp().toString());
		}
		inf.reDeploy(newNode);
		long end = System.currentTimeMillis();
		System.out.println("INIT OP: "+(end-init));
		
		System.out.println("MANAGER: reDeploy ops in node IP: "+InetAddress.getByName(newIP_txt).toString());
		System.out.println("MANAGER: Updating upstream and downstream connections...");
		//updateU_D could get nodes instead of IPs to build correct nodes, but
		//it also work just with IPs
			
System.out.println("calling updateUD");
		inf.updateU_D(oldIP, newNode.getIp(), true);
			
		Operator toInit = inf.getOperatorById(opId);
System.out.println("broadcast state and runtime init");
		inf.broadcastState(toInit);
		inf.initRuntime(toInit);
				
		System.out.println("FAILED OP REINSTANTIATED");
		System.out.println("SCALING OUT OP: "+opId);
		Node newNodeSO = null;
		try {
			newNodeSO = inf.getNodeFromPool();
		} 
		catch (NodePoolEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("NODE NEW FOR SO: "+newNodeSO);
		inf.getEiu().scaleOutOperator(opId, 555, newNodeSO);
	}
		
	private void bootstrapCommand(String ip, String controlIp, InetAddress masterWorkerCommsIp, int port) throws UnknownHostException{
		InetAddress bootIp = InetAddress.getByName(ip);
		InetAddress bootControlIp = InetAddress.getByName(controlIp);
		Node n = new Node(bootIp, bootControlIp, masterWorkerCommsIp, port);
		//add node to the stack
		inf.addNode(n);
		if(inf.isSystemRunning()){
			byte[] data = inf.getDataFromFile(inf.getPathToQueryDefinition());
			LOG.debug("-> Sending code to recently added worker");
			inf.sendCode(n, data);
		}
	}
		
	private void addOperatorCommand(String className, String opID_txt, String opArg)
	throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException,
	InstantiationException, IllegalAccessException, InvocationTargetException{
		
		Operator op = null;
		int opID = Integer.parseInt(opID_txt);
		if (className.equals("seep.operator.collection.AverageOperator") || className.equals("seep.operator.collection.AdderOperator")) {
			Constructor constr = Class.forName(className).getConstructor(int.class,int.class);
			op = (Operator)	constr.newInstance(opID,Integer.parseInt(opArg));
		}
		else if (className.equals("seep.operator.collection.WordCounter") || className.equals("seep.operator.collection.WordSplitter")) {
			op = (Operator)Class.forName(className).getConstructor(int.class).newInstance(opID);
		}
		else {
			System.err.println("Class not known");
		}
		inf.addOperator(op);
	}
		
	private void addPartitioningConnectionCommand(String[] token)	throws NumberFormatException {
		Connectable src = inf.elements.get(Integer.parseInt(token[1]));
		Connectable dst = inf.elements.get(Integer.parseInt(token[2]));
		inf.deployConnection("add_downstream_partition", src, dst, "Null");
	}
	
	private void addDownstreamConnectionCommand(String[] token)	throws NumberFormatException {
		Connectable src = inf.elements.get(Integer.parseInt(token[1]));
		Connectable dst = inf.elements.get(Integer.parseInt(token[2]));
		inf.deployConnection("add_downstream", src, dst, "null");
	}
	
	private void addUpstreamConnectionCommand(String[] token)throws NumberFormatException {
		Connectable src = inf.elements.get(Integer.parseInt(token[1]));
		Connectable dst = inf.elements.get(Integer.parseInt(token[2]));
		inf.deployConnection("add_upstream", dst, src, "NuLL");
	}
	
	private void placeCommand(String[] token) throws NumberFormatException, UnknownHostException {
		Operator op = (Operator) inf.elements.get(Integer.parseInt(token[1]));
		Node n = new Node(InetAddress.getByName(token[2]),Integer.parseInt(token[3]));
		System.out.println(op);
		inf.placeNew(op,n);
		System.out.println(op);
		inf.updateContextLocations(op);
		System.out.println(op);
		LOG.error("TODO: Place.");
		throw new RuntimeException("TODO control ip in placeCommand.");
	}
		
	/// \todo {java 7 supports switch(string)}
	public void run(){
		try {
		//TODO change this
		if( "true".equals(GLOBALS.valueFor("useCoreAddr")))
		{
			managerS = new ServerSocket(port, 50, InetAddress.getByName(GLOBALS.valueFor("coreMainAddr")));
		}
		else { managerS = new ServerSocket(port, 50, InetAddress.getByName(GLOBALS.valueFor("mainAddr"))); }
		
		LOG.info(" Listening on {}:{}", managerS.getInetAddress(), port);
		BufferedReader bis = null;
		goOn = true;
		while(goOn){
			try {
				clientSocket = managerS.accept();
				InetAddress masterWorkerCommsIp = clientSocket.getInetAddress(); 
				LOG.info(" Received worker connection from {}", masterWorkerCommsIp);
				bis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String com = "";
				while( (com = bis.readLine()) != null) {
					String token[] = com.split(" ");
					System.out.println("Manager: Command received -> "+com);
						
					if(token[0].equals("crash")){
						if(GLOBALS.valueFor("parallelRecovery").equals("true")){
							crashCommandParallelRecovery(token[1],token[2],token[3],token[4]);
						}
						else{
							//params oldIp, oldPort, newIp, newPort
							crashCommand(token[1],token[2],token[3],token[4]);
						}
					}
					else if(token[0].equals("bootstrap")){
						bootstrapCommand(token[1], token[2], masterWorkerCommsIp, Integer.parseInt(token[3]));
					}
					else if(token[0].equals("systemStable")){
						Infrastructure.msh.setSystemStableTime(System.currentTimeMillis());
					}
					else if(token[0].equals("add_operator")) {
						addOperatorCommand(token[1], token[2], token[3]);
					}
					else if(token[0].equals("place")) {
						placeCommand(token);
					}
					else if(token[0].equals("deploy_operator")) {
						Operator op = (Operator) inf.elements.get(Integer.parseInt(token[1]));
						inf.remoteOperatorInstantiation(op);
					}
					else if(token[0].equals("add_upstream_conn")) {
						addUpstreamConnectionCommand(token);
inf.printCurrentInfrastructure();
					}						
					else if(token[0].equals("add_downstream_conn")) {
						addDownstreamConnectionCommand(token);
					}
					else if(token[0].equals("add_partitioning_conn")) {
						addPartitioningConnectionCommand(token);
					}
					//we could have sent this with a message to the operator control socket.
					else if(token[0].equals("init_all")) {
						Operator op = (Operator) inf.elements.get(Integer.parseInt(token[1]));
						inf.init(op);
					}
				}
				//bis.close();
					//other options...				
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (clientSocket != null)
					try {
						clientSocket.close();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
		}
		managerS.close();
	} 
	catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
