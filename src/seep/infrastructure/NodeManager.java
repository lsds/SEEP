package seep.infrastructure;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import seep.comm.NodeManagerCommunication;
import seep.infrastructure.monitor.Monitor;
import seep.operator.Operator;
import seep.runtimeengine.CoreRE;
import seep.utils.dynamiccodedeployer.ExtendedObjectInputStream;
import seep.utils.dynamiccodedeployer.RuntimeClassLoader;

/**
 * NodeManager. This is the entity that controls the system info associated to a given node, for instance, the monitor of the node, and the 
 * operators that are within that node.
 */

public class NodeManager{
	
	private WorkerNodeDescription nodeDescr;
	
	private CoreRE core = null;
	
	private RuntimeClassLoader rcl = null;
	
	//Endpoint of the central node
	private int bindPort;
	private InetAddress bindAddr;
	//Bind port of this NodeManager
	private int ownPort;
	
	public static Logger nLogger = Logger.getLogger("seep");
	
	private NodeManagerCommunication bcu = new NodeManagerCommunication();
	
	static public boolean monitorOfSink = false;
	
	static public long clock = 0;
	
	
	static public Monitor nodeMonitor = new Monitor();
	static public int second;
	static public double throughput;
	
	private Socket connMaster;
	
	private Thread monitorT = null;
	
	public NodeManager(int bindPort, InetAddress bindAddr, int ownPort) {
		this.bindPort = bindPort;
		this.bindAddr = bindAddr;
		this.ownPort = ownPort;
		
		try {
			nodeDescr = new WorkerNodeDescription(InetAddress.getLocalHost(), ownPort);
		} 
		catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rcl = new RuntimeClassLoader(new URL[0], this.getClass().getClassLoader());
	}

//	public void newOperatorInitialization(Object o) throws OperatorInitializationException {
////		mapOP_ID.get(((Integer)o).intValue()).initializeCommunications();
//		core.setOpReady((Integer)o);
//	}

//	public void startOperator(Integer opToInitialize) {
//		int opId = opToInitialize.intValue();
////		Seep.DataTuple.Builder dt = Seep.DataTuple.newBuilder();
//		DataTuple dt = new DataTuple(-5);
//		dt.setTs(0);
//		nLogger.info("-> Starting system");
//		mapOP_ID.get(opId).processData(dt);
//	}
	
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
		//Get unique identifier for this node
		int nodeId = nodeDescr.getNodeId();
		//Initialize node engine ( CoreRE + ProcessingUnit )
		CoreRE core = new CoreRE(nodeDescr);
		//Initialize monitor
		nodeMonitor.setNodeId(nodeId);
		monitorT = new Thread(nodeMonitor);
		monitorT.start();
		nLogger.info("-> Node Monitor running");
		//Send bootstrap information
		bcu.sendBootstrapInformation(bindPort, bindAddr, ownPort);
		//Local variables
		ServerSocket serverSocket = null;
		PrintWriter out = null;
		ExtendedObjectInputStream ois = null;
		
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
				ois = new ExtendedObjectInputStream(clientSocket.getInputStream(), rcl);
//				hack = rcl.getEOIS(clientSocket.getInputStream());
				//Read the serialized object sent.
				ObjectStreamClass osc = ois.readClassDescriptor();
				System.out.println("Reading class descriptor: "+osc.toString());
				//Lazy load of the required class in case is an operator
				if(!(osc.getName().equals("java.lang.String")) && !(osc.getName().equals("java.lang.Integer"))){
					System.out.println("Loading class with CUSTOM CLASSLOADER: "+osc.getName());
					Class<?> baseI = rcl.loadClass(osc.getName());
					Constructor<?> constructor = baseI.getConstructor(new Class[]{Integer.TYPE});
					Object[] initargs = new Object[1];
					initargs[0] = -666;
					Object base = constructor.newInstance(initargs);
					System.out.println("Mock object id: "+((Operator)base).getOperatorId());
					o = ois.readObject();
					System.out.println("REAL object id: "+((Operator)o).getOperatorId());
				}
				else{
					o = ois.readObject();
				}
				// Can I read the object with the class supposedly loaded in the environment?
//				o = ois.readObject();
				//Check the class of the object received and initialized accordingly
				if(o instanceof Operator){
					core.pushOperator((Operator)o);
					//this.newOperatorInstantiation(o);
				}
				else if(o instanceof Integer){
//					this.newOperatorInitialization(o);
					core.setOpReady((Integer)o);
				}
				else if(o instanceof String){
					String tokens[] = ((String)o).split(" ");
					System.out.println("Tokens received: "+tokens[0]);
					if(tokens[0].equals("CODE")){
						//Send ACK back
						out.println("ack");
						// Establish subconnection to receive the code
						System.out.println("Waiting for receiving the file");
						Socket subConnection = serverSocket.accept();
						DataInputStream dis = new DataInputStream(subConnection.getInputStream());
						int codeSize = dis.readInt();
						byte[] serializedFile = new byte[codeSize];
						dis.readFully(serializedFile);
						int bytesRead = serializedFile.length;
						System.out.println("Code is "+codeSize+ ": Read bytes is: "+bytesRead);
						if(bytesRead != codeSize){
							NodeManager.nLogger.warning("Mismatch between read and file size");
						}
						//Here I have the serialized bytes of the file, we materialize the real file
						//For now the name of the file is always query.jar
						FileOutputStream fos = new FileOutputStream(new File("query.jar"));
						System.out.println("READ "+serializedFile.length+" bytes");
						fos.write(serializedFile);
						fos.close();
						dis.close();
						subConnection.close();
						out.println("ack");
						//At this point we should have the file on disk
						File pathToCode = new File("query.jar");
						System.out.println("PATH TO NEW CODE: "+pathToCode.getAbsolutePath());
						System.out.println("Exists??? "+pathToCode.exists());
						if(pathToCode.exists()){
							loadCodeToRuntime(pathToCode);
						}
					}
					if(tokens[0].equals("STOP")){
						core.stopDataProcessing();
						listen = false;
						out.println("ack");
						o = null;
						ois.close();
						out.close();
						clientSocket.close();
						//since listen=false now, finish the loop
						continue;
					}
					if(tokens[0].equals("SET-RUNTIME")){
						System.out.println("Set runtime");
						core.setRuntime();
						out.println("ack");
					}
					if(tokens[0].equals("START")){
						System.out.println("SEC: RECEIVED ORDER TO START this: "+tokens[1]);
                        //We call the processData method on the source
                        /// \todo {Is START used? is necessary to answer with ack? why is this not using startOperator?}
                        out.println("ack");
//                        Seep.DataTuple.Builder dt = Seep.DataTuple.newBuilder();
//                        DataTuple dt = new DataTuple(-5);
//                        dt.setTs(0);
//                        Integer aux = new Integer(tokens[1]);
//                        (NodeManager.mapOP_ID.get(aux.intValue())).processData(dt);
                        core.startDataProcessing();
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
		catch(IllegalThreadStateException itse){
			System.out.println("IllegalThreadStateException, no problem, monitor thing");
			itse.printStackTrace();
		} 
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void loadCodeToRuntime(File pathToCode){
		URL urlToCode = null;
		try {
			urlToCode = new URL("file://"+pathToCode.getAbsolutePath());
			System.out.println("Loading into class loader: "+urlToCode.toString());
			URL[] urls = new URL[1];
			urls[0] = urlToCode;
			//rcl = new RuntimeClassLoader(urls, this.getClass().getClassLoader());
			rcl.addURL(urlToCode);
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
