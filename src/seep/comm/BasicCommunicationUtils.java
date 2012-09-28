package seep.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import seep.comm.serialization.ControlTuple;
import seep.infrastructure.Infrastructure;
import seep.infrastructure.NodeManager;
import seep.infrastructure.OperatorStaticInformation;
import seep.infrastructure.Node;

/**
 * BasicCommunicationUtils. This class provides simple methods to communicate between master and secondary nodes
 */

public class BasicCommunicationUtils {
	
	private Map<Integer, Socket> uniqueSocket = new HashMap<Integer, Socket>();
	private Kryo k;
	
	public BasicCommunicationUtils(){
		this.k = initializeKryo();
	}
	
	private Kryo initializeKryo(){
		k = new Kryo();
		k.register(ControlTuple.class);
		return k;
	}
	
	public boolean sendObject(Node n, Object o){
		//Get destiny address, port is preconfigured to 3500 for deployer tasks
		InetAddress ip = n.getIp();
		int port = n.getPort();
/// \bug {creating socket again and again.}
		//Socket connection = uniqueSocket.get(port);
		Socket connection = null;
		ObjectOutputStream oos = null;
		BufferedReader in = null;
		boolean success = false;
		try{
			if(connection == null){
				System.out.println("Creating socket to: "+ip.toString()+" port: "+port);
				connection = new Socket(ip, port);
				Infrastructure.nLogger.info("-> BCU. New socket created, IP: "+ip.toString()+" Port: "+port);
				//uniqueSocket.put(port, connection);
			}
//System.out.println("SENDOBJECT: to IP: "+ip.toString()+" Port: "+port);
			oos = new ObjectOutputStream(connection.getOutputStream());
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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

	public void sendControlMsg(OperatorStaticInformation loc, ControlTuple ct, int socketId){
		Socket connection = uniqueSocket.get(socketId);
		try{
			if(connection == null){
				connection = new Socket(loc.getMyNode().getIp(), loc.getInC());
				Infrastructure.nLogger.info("-> BCU. New socket in sendControlMsg");
				uniqueSocket.put(socketId, connection);
			}
			Output output = new Output(connection.getOutputStream());
			Input input = new Input(connection.getInputStream());
//			ct.writeDelimitedTo(connection.getOutputStream());
			System.out.println("sendTo: "+connection.toString());
			k.writeObject(output, ct);
			/**Critical line in KRYO**/
			output.flush();
			//wait for application level ack
//			Seep.ControlTuple ack = Seep.ControlTuple.parseDelimitedFrom(connection.getInputStream());
			ControlTuple ack = k.readObject(input, ControlTuple.class);
			//waiting for ack
			NodeManager.nLogger.info("-> controlMsg ACK");
		}
		catch(IOException io){
			Infrastructure.nLogger.severe("-> Infrastructure. While sending Msg "+io.getMessage());
			io.printStackTrace();
			Infrastructure.nLogger.severe("CONN: "+connection.toString());
		}
	}
	
	public void sendControlMsgWithoutACK(OperatorStaticInformation loc, ControlTuple ct, int socketId){
		Socket connection = uniqueSocket.get(socketId);
		try{
			Output output = new Output(connection.getOutputStream());
			if(connection == null){
				connection = new Socket(loc.getMyNode().getIp(), loc.getInC());
				Infrastructure.nLogger.info("-> BCU. New socket in sendControlMsg");
				uniqueSocket.put(socketId, connection);
			}

			k.writeObject(output, ct);
			output.flush();
		}
		catch(IOException io){
			Infrastructure.nLogger.severe("-> Infrastructure. While sending Msg "+io.getMessage());
			io.printStackTrace();
			Infrastructure.nLogger.severe("CONN: "+connection.toString());
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
