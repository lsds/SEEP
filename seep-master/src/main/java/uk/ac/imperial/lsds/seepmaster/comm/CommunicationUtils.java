package uk.ac.imperial.lsds.seepmaster.comm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;

import uk.ac.imperial.lsds.seep.comm.Connection;
import uk.ac.imperial.lsds.seep.comm.serialization.Serializer;

public class CommunicationUtils implements Comm {

	@Override
	public void send_sync(byte[] data, Connection c) {

	}

	@Override
	public void send_async(byte[] data, Connection c) {
		Socket connection = c.getOpenSocket();
		try {
			DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
			dos.writeInt(data.length);
			dos.write(data);
			dos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void send_sync(byte[] data, Set<Connection> cs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_async(byte[] data, Set<Connection> cs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_sync_parallel(byte[] data, Set<Connection> cs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_async_parallel(byte[] data, Set<Connection> cs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_sync(String data, Connection c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_async(String data, Connection c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_sync(String data, Set<Connection> cs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_async(String data, Set<Connection> cs) {
		
	}

	@Override
	public void send_sync_parallel(String data, Set<Connection> cs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_async_parallel(String data, Set<Connection> cs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_sync(Object data, Connection c, Serializer s) {
//		Socket connection = c.getOpenSocket();
//		ExtendedObjectOutputStream oos = null;
//		BufferedReader in = null;
//		
//		try {
//			oos = new ExtendedObjectOutputStream(connection.getOutputStream());
//			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			
//			oos.writeClassDescriptor(ObjectStreamClass.lookup(o.getClass()));
//			oos.writeObject(o);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
	}

	@Override
	public void send_async(Object data, Connection c, Serializer s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_sync(Object data, Set<Connection> cs, Serializer s) {
		byte[] d = s.serialize(data);
		this.send_sync(d, cs);
	}

	@Override
	public void send_async(Object data, Set<Connection> cs, Serializer s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_sync_parallel(Object data, Set<Connection> cs, Serializer s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send_async_parallel(Object data, Set<Connection> cs,
			Serializer s) {
		// TODO Auto-generated method stub
		
	}
	
//	public boolean sendObject(Node n, int operatorId, Object o){
//		//Get destiny address, port is preconfigured to 3500 for deployer tasks
//		InetAddress ip = n.getIp();
//		int port = n.getPort();
///// \bug {creating socket again and again.}
//		Socket connection = null;
//		ExtendedObjectOutputStream oos = null;
//		BufferedReader in = null;
//		boolean success = false;
//		try{
//			if(connection == null){
//				connection = new Socket(ip, port);
//				LOG.debug("-> BCU. New socket created, IP: "+ip.toString()+" Port: "+port);
//			}
//			oos = new ExtendedObjectOutputStream(connection.getOutputStream());
//			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			
//            LOG.debug("Class about to send: "+o.getClass());
//			oos.writeClassDescriptor(ObjectStreamClass.lookup(o.getClass()));
//			oos.writeObject(o);
//            
//            LOG.debug("Waiting for ack/nack reply from operatorId [{}]", operatorId);
//			String reply = null;
//			reply = in.readLine();
//            LOG.debug("Received response [{}] from operatorId [{}]", reply, operatorId);
//            
//			///\fixme{handle error properly}
//			if(reply.equals("ack")){
//				success = true;
//			}
//			else if(reply.equals("nack")){
//				//TODO
//			}
//			else{
//				LOG.error("ERROR: MSG Received: {}",reply);
//			}
//			oos.close();
//			in.close();
//			connection.close();
//		}
//		catch(IOException e){
//			LOG.error("-> While sending Object "+e.getMessage());
//			e.printStackTrace();
//		}
//		return success;
//	}
	

}
