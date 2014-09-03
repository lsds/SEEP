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
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;

import android.os.AsyncTask;

import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer.ExtendedObjectOutputStream;
import uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.operator.Operator;

/**
 * BasicCommunicationUtils. This class provides simple methods to communicate between master and secondary nodes
 */

public class NodeManagerCommunication {

	public static Logger LOG = LoggerFactory.getLogger(NodeManagerCommunication.class);

	public void sendObject(Node n, Object o) {
		sendObject(n, 0, o);
	}

	public void sendObject(Node n1, int operatorId1, Object o1){
		final Node n = n1;
		final Object o = o1;
		final int operatorId = operatorId1;


		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				//Get destiny address, port is preconfigured to 3500 for deployer tasks
				InetAddress ip = n.getIp();
				int port = n.getPort();
				/// \bug {creating socket again and again.}
				Socket connection = null;
				ExtendedObjectOutputStream oos = null;
				BufferedReader in = null;
				try{
					if(connection == null){
						connection = new Socket(ip, port);
						connection.setKeepAlive(true);
						LOG.debug("-> BCU. New socket created, IP: "+ip.toString()+" Port: "+port);
					}
					oos = new ExtendedObjectOutputStream(connection.getOutputStream());
					in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

					LOG.debug("-> Class about to send: "+o.getClass());

					//	oos.writeClassDescriptor(ObjectStreamClass.lookup(o.getClass()));
					oos.writeObject(o);


					LOG.debug("Waiting for ack/nack reply from operatorId [{}]", operatorId);
					String reply = null;

					reply = in.readLine();

					LOG.debug("Received response [{}] from operatorId [{}]", reply, operatorId);

					///\fixme{handle error properly}
					if(reply.equals("ack")){
						LOG.debug("MSG Received: ACK",reply);

					}
					else if(reply.equals("nack")){
						//TODO						
						LOG.debug("MSG Received: NACK",reply);

					}
					else{
						LOG.debug("ERROR: MSG Received: {}",reply);
					}
					oos.close();
					in.close();
					connection.close();
				}
				catch(IOException e){
					LOG.error("-> Error While sending Object "+o.toString()+": "+e.getMessage());
					e.printStackTrace();
				}

			}
		});

		thread.start();
	}

	public void sendFile(Node n1, byte[] data1){

		final Node n = n1;
		final byte[] data = data1;


		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
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
					Log.info("File sent "+data.length);
					dos.close();
					connection.close();
				}
				catch(IOException io){
					LOG.info("IOEX when trying to send file over the network");
					io.printStackTrace();
				}
			}
		});

		thread.start();
	}

	//This method gets the local IP and sends a BOOT message to the central node.
	public void sendBootstrapInformation(int port, InetAddress bindAddr, int ownPort){
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add(port+"");
		parameters.add(bindAddr.getHostAddress());
		parameters.add(ownPort+"");

		new sendBootstrapInformationTask().execute(parameters);

	}

	class sendBootstrapInformationTask extends AsyncTask<ArrayList<String>, Void, Integer> {

		@Override
		protected Integer doInBackground(ArrayList<String>... passing) {
			ArrayList<String> parameters = passing[0];
			//	InetAddress ownIp;
			String command;
			try {

				StringBuilder IFCONFIG=new StringBuilder();
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
							IFCONFIG.append(inetAddress.getHostAddress().toString()+"\n");
						}

					}
				}
				
				String[] ips = IFCONFIG.toString().split("\n");
				String ip = "";
				
//				if (ips.length > 1){
//					ip = ips[1];
//				} else
					ip = ips[0];
				
				command = "bootstrap"+" "+ip+" "+parameters.get(2)+"\n";
				LOG.info("--> Boot Info: {} to: {} on: {}", command, parameters.get(1), parameters.get(0));
				Socket conn = new Socket(InetAddress.getByName(parameters.get(1)), Integer.parseInt(parameters.get(0)));
				(conn.getOutputStream()).write(command.getBytes());
				conn.close();
				return 0;	
			}
			catch(UnknownHostException uhe){
				System.out.println("INF.sendBootstrapInformation: "+uhe.getMessage());
				LOG.error("-> Infrastructure. sendBootstrapInfo "+uhe.getMessage());
				uhe.printStackTrace();
			}
			catch(IOException io){
				LOG.error("-> Infrastructure. sendBootstrapInfo "+io.getMessage());
				io.printStackTrace();
			}
			return -1;
		}
	}
}


