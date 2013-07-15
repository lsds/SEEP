/*******************************************************************************
 * Copyright (c) 2013 Raul Castro Fernandez (Ra).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Ra - Design and initial implementation
 ******************************************************************************/
package uk.co.imperial.lsds.seep.comm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectStreamClass;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import uk.co.imperial.lsds.seep.infrastructure.NodeManager;
import uk.co.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.co.imperial.lsds.seep.infrastructure.master.Node;
import uk.co.imperial.lsds.seep.utils.dynamiccodedeployer.ExtendedObjectOutputStream;

/**
 * BasicCommunicationUtils. This class provides simple methods to communicate between master and secondary nodes
 */

public class NodeManagerCommunication {
	
	public boolean sendObject(Node n, Object o){
		//Get destiny address, port is preconfigured to 3500 for deployer tasks
		InetAddress ip = n.getIp();
		int port = n.getPort();
/// \bug {creating socket again and again.}
		Socket connection = null;
		ExtendedObjectOutputStream oos = null;
		BufferedReader in = null;
		boolean success = false;
		try{
			if(connection == null){
				System.out.println("Creating socket to: "+ip.toString()+" port: "+port);
				connection = new Socket(ip, port);
				Infrastructure.nLogger.info("-> BCU. New socket created, IP: "+ip.toString()+" Port: "+port);
			}
			oos = new ExtendedObjectOutputStream(connection.getOutputStream());
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Infrastructure.nLogger.info("Class about to send: "+o.getClass());
			oos.writeClassDescriptor(ObjectStreamClass.lookup(o.getClass()));
			oos.writeObject(o);
			String reply = null;
			reply = in.readLine();
			if(reply.equals("ack")){
				success = true;
			}
			else if(reply.equals("nack")){
				//TODO
			}
			else{
				System.out.println("ERROR: MSG Received: "+reply);
			}
			oos.close();
			in.close();
			connection.close();
		}
		catch(IOException e){
			Infrastructure.nLogger.severe("-> Infrastructure. While sending Object "+e.getMessage());
			e.printStackTrace();
		}
		return success;
	}
	
	public void sendFile(Node n, byte[] data){
		sendObject(n, "CODE");
		InetAddress ip = n.getIp();
		int port = n.getPort();
		Socket connection = null;
		try{
			connection = new Socket(ip, port);
			DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
			dos.writeInt(data.length);
			dos.write(data);
//			fis.close();
			dos.close();
			connection.close();
		}
		catch(IOException io){
			NodeManager.nLogger.severe("IOEX when trying to send file over the network");
			io.printStackTrace();
		}
	}
	
	
	//This method gets the local IP and sends a BOOT message to the central node.
	public void sendBootstrapInformation(int port, InetAddress bindAddr, int ownPort){
		try{
			InetAddress ownIp = InetAddress.getLocalHost();
			String command = "bootstrap "+(ownIp.getHostAddress()+" "+ownPort+"\n");
			Infrastructure.nLogger.info("--> BOOT: "+command+" to: "+bindAddr+" on: "+port+" port");
			Socket conn = new Socket(bindAddr, port);
			(conn.getOutputStream()).write(command.getBytes());
			conn.close();
		}
		catch(UnknownHostException uhe){
			System.out.println("INF.sendBootstrapInformation: "+uhe.getMessage());
			Infrastructure.nLogger.severe("-> Infrastructure. sendBootstrapInfo "+uhe.getMessage());
			uhe.printStackTrace();
		}
		catch(IOException io){
			Infrastructure.nLogger.severe("-> Infrastructure. sendBootstrapInfo "+io.getMessage());
			io.printStackTrace();
		}
	}
}
