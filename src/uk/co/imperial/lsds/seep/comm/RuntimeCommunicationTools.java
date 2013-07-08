package uk.co.imperial.lsds.seep.comm;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import uk.co.imperial.lsds.seep.comm.serialization.ControlTuple;
import uk.co.imperial.lsds.seep.infrastructure.NodeManager;
import uk.co.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.co.imperial.lsds.seep.operator.OperatorStaticInformation;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class RuntimeCommunicationTools {

	private Map<Integer, Socket> uniqueSocket = new HashMap<Integer, Socket>();
	private Kryo k = null;
	
	
	public RuntimeCommunicationTools(){
		this.k = initializeKryo();
	}
	
	private Kryo initializeKryo(){
		k = new Kryo();
		k.register(ControlTuple.class);
		return k;
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
			System.out.println("sendTo: "+connection.toString());
			k.writeObject(output, ct);
			/**Critical line in KRYO**/
			output.flush();
			//wait for application level ack
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
			
			
			
			//Output output = new Output(connection.getOutputStream());
			
			connection = new Socket(loc.getMyNode().getIp(), loc.getInC());
			Infrastructure.nLogger.info("-> BCU. New socket in sendControlMsg");
			uniqueSocket.put(socketId, connection);
			Output output = new Output(connection.getOutputStream());
			
//			if(connection == null){
//				connection = new Socket(loc.getMyNode().getIp(), loc.getInC());
//				Infrastructure.nLogger.info("-> BCU. New socket in sendControlMsg");
//				uniqueSocket.put(socketId, connection);
//			}

			k.writeObject(output, ct);
			output.flush();
		}
		catch(IOException io){
			Infrastructure.nLogger.severe("-> Infrastructure. While sending Msg "+io.getMessage());
			io.printStackTrace();
			Infrastructure.nLogger.severe("CONN: "+connection.toString());
		}
	}
}
