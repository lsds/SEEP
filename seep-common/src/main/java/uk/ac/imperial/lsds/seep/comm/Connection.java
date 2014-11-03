package uk.ac.imperial.lsds.seep.comm;

import java.io.IOException;
import java.net.Socket;

import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seep.util.Utils;

public class Connection {

	private final EndPoint ep;
	private Socket s;
	
	public Connection(EndPoint ep) {
		this.ep = ep;
	}
	
	public int getId(){
		return ep.getId();
	}
	
	public Socket getSocket(){
		return s;
	}
	
	public Socket getOpenSocket(){
		if(s == null || s.isClosed()){
			try {
				s = new Socket(ep.getIp(), ep.getPort());
				return s;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(s != null){
			if(s.isConnected()) {
				return s;
			}
		}
		// TODO: reopen if closed
		
		return null;
	}
	
	public void destroy(){
		try {
			this.s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("IP: "+ep.getIp().toString()+" port: "+ep.getPort());
		sb.append(Utils.NL);
		if(s != null){
			sb.append("ConnectionStatus: "+s.toString());
		}
		else{
			sb.append("ConnectionStatus: NULL");
		}
		return sb.toString();
	}
	
}
