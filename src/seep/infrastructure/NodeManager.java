package seep.infrastructure;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import seep.comm.BasicCommunicationUtils;
import seep.comm.serialization.DataTuple;
import seep.infrastructure.monitor.Monitor;
import seep.operator.Operator;

/**
 * NodeManager. This is the entity that controls the system info associated to a given node, for instance, the monitor of the node, and the 
 * operators that are within that node.
 */

public class NodeManager{
	
	//Endpoint of the central node
	private int bindPort;
	private InetAddress bindAddr;
	//Bind port of this NodeManager
	private int ownPort;
	
	public static Logger nLogger = Logger.getLogger("seep");
	
	private BasicCommunicationUtils bcu = new BasicCommunicationUtils();
	
	static public boolean monitorOfSink = false;
	
	static public long clock = 0;
	
	static public Map<Integer, Operator> mapOP_ID = new HashMap<Integer, Operator>();
	
	static public Monitor nodeMonitor = new Monitor();
	static public int second;
	static public double throughput;
	
	private Socket connMaster;
	
	private Thread monitorT = null;
	
	public NodeManager(int bindPort, InetAddress bindAddr, int ownPort) {
		this.bindPort = bindPort;
		this.bindAddr = bindAddr;
		this.ownPort = ownPort;
	}

	public void newOperatorInstantiation(Object o) throws OperatorInstantiationException {
//System.out.println("MONITOR THREAD STATE: "+monitorT);
		if(monitorT == null){
			int opId = ((Operator)o).getOperatorId();
			nodeMonitor.setOpId(opId);
			monitorT = new Thread(nodeMonitor);
			monitorT.start();
			nLogger.info("-> Node Monitor running");
		}
		mapOP_ID.put(((Operator)o).getOperatorId(), (Operator)o);
		((Operator)o).instantiateOperator();
	}

	public void newOperatorInitialization(Object o) throws OperatorInitializationException {
		mapOP_ID.get(((Integer)o).intValue()).initializeCommunications();
	}

	public void startOperator(Integer opToInitialize) {
		int opId = opToInitialize.intValue();
//		Seep.DataTuple.Builder dt = Seep.DataTuple.newBuilder();
		DataTuple dt = new DataTuple();
		dt.setTs(0);
		nLogger.info("-> Starting system");
		mapOP_ID.get(opId).processData(dt);
	}
	
	/// \todo{the client-server model implemented here is crap, must be refactored}
	static public void setSystemStable(){
//		String command = "systemStable \n";
//		try{
//			//if(connMaster == null){
//			Socket connMaster = new Socket(InetAddress.getByName(Main.valueFor("mainAddr")), Integer.parseInt(Main.valueFor("mainPort")));
//			//connMaster.setSoLinger(true, 0);
//			OutputStream os = connMaster.getOutputStream();
//				//connMaster.setReuseAddress(true);
//				//Server is expecting new conn with accept
//				
//			//}
//			//(connMaster.getOutputStream()).write(command.getBytes());
//			os.write(command.getBytes());
//			System.out.println("finished method!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			Thread.sleep(100);
//			os.close();
//		}
//		catch(UnknownHostException uhe){
//			System.out.println("NodeManager.setSystemStable: "+uhe.getMessage());
//			uhe.printStackTrace();
//		}
//		catch(IOException io){
//			System.out.println("NodeManager.setSystemStable: "+io.getMessage());
//			io.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public void init(){
		//Send bootstrap information
		bcu.sendBootstrapInformation(bindPort, bindAddr, ownPort);
		//Local variables
		ServerSocket serverSocket = null;
		PrintWriter out = null;
		ObjectInputStream ois = null;
		Object o = null;
		boolean listen = true;
		boolean initializationSuccess = false;
		
		try{
			serverSocket = new ServerSocket(ownPort);
			NodeManager.nLogger.info("NODEMANAGER: Waiting for incoming requests on port: "+ownPort);
			Socket clientSocket = null;
			while(listen){
				//Accept incoming connections
				clientSocket = serverSocket.accept();
				//Establish output stream
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				//Establish input stream, which receives serialized objects
				ois = new ObjectInputStream(clientSocket.getInputStream());
				//Read the serialized object sent.
				o = ois.readObject();
				//Check the class of the object received and initialized accordingly
				if(o instanceof Operator){
					this.newOperatorInstantiation(o);
				}
				else if(o instanceof Integer){
					this.newOperatorInitialization(o);
				}
				else if(o instanceof String){
					String tokens[] = ((String)o).split(" ");
					if(tokens[0].equals("STOP")){
						listen = false;
						out.println("ack");
						o = null;
						ois.close();
						out.close();
						clientSocket.close();
						//since listen=false now, finish the loop
						continue;
					}
					if(tokens[0].equals("START")){
						System.out.println("SEC: RECEIVED ORDER TO START this: "+tokens[1]);
                        //We call the processData method on the source
                        /// \todo {Is START used? is necessary to answer with ack? why is this not using startOperator?}
                        out.println("ack");
//                        Seep.DataTuple.Builder dt = Seep.DataTuple.newBuilder();
                        DataTuple dt = new DataTuple();
                        dt.setTs(0);
                        Integer aux = new Integer(tokens[1]);
                        (NodeManager.mapOP_ID.get(aux.intValue())).processData(dt);
					}
					if(tokens[0].equals("CLOCK")){
						NodeManager.clock = System.currentTimeMillis();
						out.println("ack");
					}
				}
				//Send message back.
				out.println("ack");
				o = null;
				ois.close();
				out.close();
				clientSocket.close();
			}
			serverSocket.close();
		}
		//For now send nack, probably this is not the best option...
		catch(IOException io){
			System.out.println("IOException: "+io.getMessage());
			io.printStackTrace();
//			out.println("nack");
		}
		catch(ClassNotFoundException cnfe){
			System.out.println("ClassNotFoundException: "+cnfe.getMessage());
			cnfe.printStackTrace();
//			out.println("nack");
		}
		catch(IllegalThreadStateException itse){
			System.out.println("IllegalThreadStateException, no problem, monitor thing");
			itse.printStackTrace();
		} 
		catch (OperatorInstantiationException e) {
			NodeManager.nLogger.warning("Error while instantiating operator");
			e.printStackTrace();
		}
		catch (OperatorInitializationException e) {
			NodeManager.nLogger.warning("Error while initializing operator");
			e.printStackTrace();
		}
	}
}
