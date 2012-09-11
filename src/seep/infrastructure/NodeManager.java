package seep.infrastructure;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import seep.Main;
import seep.comm.tuples.Seep;
import seep.infrastructure.monitor.Monitor;
import seep.operator.Operator;
import seep.utils.ExecutionConfiguration;

/**
 * NodeManager. This is the entity that controls the system info associated to a given node, for instance, the monitor of the node, and the 
 * operators that are within that node.
 */

public class NodeManager{
	
	public static Logger nLogger = Logger.getLogger("seep");
	
	static public boolean monitorOfSink = false;
	
	static public long clock = 0;
	
	static public Map<Integer, Operator> mapOP_ID = new HashMap<Integer, Operator>();
	
	static public Monitor nodeMonitor = new Monitor();
	static public int second;
	static public double throughput;
	
	private  Socket connMaster;
	
	private Thread monitorT = null;
	
	public NodeManager(int port, InetAddress bindAddr) {
		//monitorT = new Thread(nodeMonitor);
	}

	public boolean newOperatorInstantiation(Object o) {
//System.out.println("MONITOR THREAD STATE: "+monitorT);
		if(monitorT == null){
			monitorT = new Thread(nodeMonitor);
			monitorT.start();
			nLogger.info("-> Node Monitor running");
		}
		mapOP_ID.put(((Operator)o).getOperatorId(), (Operator)o);
		return ((Operator)o).initializeOperator();
	}

	public boolean newOperatorInitialization(Object o) {
		return mapOP_ID.get(((Integer)o).intValue()).initializeCommunications();
	}

	public void startOperator(Integer opToInitialize) {
		int opId = opToInitialize.intValue();
		Seep.DataTuple.Builder dt = Seep.DataTuple.newBuilder();
		dt.setTs(0);
		nLogger.info("-> Starting system");
		mapOP_ID.get(opId).processData(dt.build());
	}
	
	/// \todo{the client-server model implemented here is crap, must be refactored}
	static public void setSystemStable(){
		String command = "systemStable \n";
		try{
			//if(connMaster == null){
			Socket connMaster = new Socket(InetAddress.getByName(Main.valueFor("mainAddr")), Integer.parseInt(Main.valueFor("mainFor")));
			//connMaster.setSoLinger(true, 0);
			OutputStream os = connMaster.getOutputStream();
				//connMaster.setReuseAddress(true);
				//Server is expecting new conn with accept
				
			//}
			//(connMaster.getOutputStream()).write(command.getBytes());
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
}
