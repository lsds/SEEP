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
package uk.ac.imperial.lsds.seep.comm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectStreamClass;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.ExtendedObjectOutputStream;
import uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.operator.Operator;

/**
 * BasicCommunicationUtils. This class provides simple methods to communicate between master and secondary nodes
 */

public class NodeManagerCommunication {
	
	final private Logger LOG = LoggerFactory.getLogger(NodeManagerCommunication.class);
	
    public boolean sendObject(Node n, Object o) {
        return sendObject(n, 0, o);
    }
    
	public boolean sendObject(Node n, int operatorId, Object o){
		//Get destiny address, port is preconfigured to 3500 for deployer tasks
		InetAddress ip = n.getIp();
		InetAddress masterWorkerCommsIp = n.getMasterWorkerCommsIp();
		int port = n.getPort();
/// \bug {creating socket again and again.}
		Socket connection = null;
		ExtendedObjectOutputStream oos = null;
		BufferedReader in = null;
		boolean success = false;
		try{
			if(connection == null){
				LOG.info("-> BCU. Creating new socket, IP: "+ip.toString()+", masterWorkerCommsIp: "+masterWorkerCommsIp+" Port: "+port);
				connection = new Socket(masterWorkerCommsIp == null? ip : masterWorkerCommsIp, port);
			}
			oos = new ExtendedObjectOutputStream(connection.getOutputStream());
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
            LOG.debug("Class about to send: "+o.getClass());
			oos.writeClassDescriptor(ObjectStreamClass.lookup(o.getClass()));
			oos.writeObject(o);
            
            LOG.debug("Waiting for ack/nack reply from operatorId [{}]", operatorId);
			String reply = null;
			reply = in.readLine();
            LOG.debug("Received response [{}] from operatorId [{}]", reply, operatorId);
            
			///\fixme{handle error properly}
			if(reply.equals("ack")){
				success = true;
			}
			else if(reply.equals("nack")){
				//TODO
			}
			else{
				LOG.error("ERROR: MSG Received: {}",reply);
			}
			oos.close();
			in.close();
			connection.close();
		}
		catch(IOException e){
			LOG.error("-> While sending Object "+e.getMessage());
			e.printStackTrace();
		}
		return success;
	}
	
//	public void sendObjectNonBlocking(ArrayList<Operator> ops){
//		ArrayList<Thread> activeT = new ArrayList<Thread>();
//		for(Operator o : ops){
//			Node n = o.getOpContext().getOperatorStaticInformation().getMyNode();
//			int opId = o.getOperatorId();
//			Thread t = new Thread(new ConnHandler(n, opId));
//			t.start();
//		}
//		for(Thread t : activeT){
//			try {
//				t.join();
//			} 
//			catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
	public void sendFile(Node n, byte[] data){
		sendObject(n, "CODE");
		InetAddress ip = n.getIp();
		InetAddress masterWorkerCommsIp = n.getMasterWorkerCommsIp();
		int port = n.getPort();
		Socket connection = null;
		try{
			connection = new Socket(masterWorkerCommsIp == null ? ip : masterWorkerCommsIp, port);
			DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
			dos.writeInt(data.length);
			dos.write(data);
			dos.flush();
//			fis.close();
			dos.close();
			connection.close();
		}
		catch(IOException io){
			LOG.error("IOEX when trying to send file over the network");
			io.printStackTrace();
		}
	}
	
	//This method gets the local IP and sends a BOOT message to the central node.
	public void sendBootstrapInformation(InetAddress controlIp, int port, InetAddress bindAddr, int ownPort){
		try{
			sendBootstrapInformation(InetAddress.getLocalHost(), controlIp, port, bindAddr, ownPort);
		}
		catch(UnknownHostException uhe){
			System.out.println("INF.sendBootstrapInformation: "+uhe.getMessage());
			LOG.error("-> Infrastructure. sendBootstrapInfo "+uhe.getMessage());
			uhe.printStackTrace();
		}
	}

	//This method gets the local IP and sends a BOOT message to the central node.
	public void sendBootstrapInformation(InetAddress ownIp, InetAddress controlIp, int port, InetAddress bindAddr, int ownPort){
		try{
			//InetAddress ownIp = InetAddress.getLocalHost();
			String command = "bootstrap "+(ownIp.getHostAddress()+" "+ controlIp.getHostAddress()+" "+ownPort+"\n");
			LOG.info("--> Boot Info: {} to: {} on: {}", command, bindAddr, port);
			Socket conn = new Socket(bindAddr, port);
			(conn.getOutputStream()).write(command.getBytes());
			conn.close();
		}
		catch(IOException io){
			LOG.error("-> Infrastructure. sendBootstrapInfo "+io.getMessage());
			io.printStackTrace();
		}
	}
}
