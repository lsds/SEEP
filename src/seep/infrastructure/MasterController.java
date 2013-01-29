package seep.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import seep.P;
import seep.elastic.ElasticInfrastructureUtils;


public class MasterController {

	//MasterController must be a singleton
	private static final MasterController instance = new MasterController();
	
	//The current path to the query being submitted. If another query is submitted this is overwritten
	///\todo{Check if this is an issue or not}
	private String pathToQueryDefinition = null;
	private URLClassLoader ucl = null;
	
    private MasterController() {}
 
    public static MasterController getInstance() {
        return instance;
    }
	
    private Infrastructure inf;
    ElasticInfrastructureUtils eiu;
    P prop = new P();
	
	public void init(){
		NodeManager.nLogger.info("-> Initializing Master Controller...");
		prop.loadProperties();
		inf = new Infrastructure(Integer.parseInt(P.valueFor("mainPort")));
		eiu = new ElasticInfrastructureUtils(inf);
		inf.startInfrastructure();
		NodeManager.nLogger.info("-> DONE");
	}
	
	public void submitQuery(QueryPlan qp){
		NodeManager.nLogger.info("-> Submitting query to the system...");
		inf.loadQuery(qp);
		NodeManager.nLogger.info("-> DONE");
	}
	
	public void start() throws OperatorDeploymentException{
		NodeManager.nLogger.info("-> Starting Interactive Console: ");
		try {
			boolean alive = true;
			
			/// \todo{make this robust}
			while(alive){
				consoleOutputMessage();
				try{
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					String option = br.readLine();
					int opt = Integer.parseInt(option);
					switch(opt){
						//Map operators to nodes
						case 0:
							System.out.println("Not implemented yet");
							//Submit Query to the system
							break;
						case 1:
							deployQueryToNodes();
							break;
						//start system
						case 2:
							startSystemOption(inf);
							break;
						//configure source rate
						case 3:
							configureSourceRateOption(inf);
							break;
						//parallelize operator manually
						case 4:
							parallelizeOpManualOption(inf, eiu);
							break;
						//silent the console
						case 5:
							alive = false;
							inf.stopWorkers();
							System.out.println("ENDING console...");
							break;
						//Exit the system
						case 6:
							System.out.println("BYE");
							System.exit(0);
							break;
						case 10:
							System.out.println("Parsing txt file...");
							inf.parseFileForNetflix();
							break;
						default:
							System.out.println("Wrong option. Try again...");
					}
				}
				catch(IOException io){
					System.out.println("While reading from terminal: "+io.getMessage());
					io.printStackTrace();
				}			
			}
			System.out.println("BYE");

		}
		catch(ESFTRuntimeException ere){
			System.out.println(ere.getMessage());
		}
		catch(Exception g){
			System.out.println(g.getMessage());
		}
	}
	
	public QueryPlan executeComposeFromQuery(String pathToJar, String definitionClass){
//		URLClassLoader ucl = null;
		Class<?> baseI = null;
		Object baseInstance = null;
		Method compose = null;
		QueryPlan qp = null;
		pathToQueryDefinition = pathToJar;
		inf.setPathToQueryDefinition(pathToJar);
		String urlPathToQueryDefinition = "file://" + pathToJar;
		URL[] urls = new URL[1];
		try {
			urls[0] = new URL(urlPathToQueryDefinition);
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// First time it is created we pass the urls
		ucl = new URLClassLoader(urls);
		eiu.setClassLoader(ucl);
		try {
			baseI = ucl.loadClass(definitionClass);
			baseInstance = baseI.newInstance();
			compose = baseI.getDeclaredMethod("compose", (Class<?>[])null);
			qp = (QueryPlan) compose.invoke(baseInstance, (Object[])null);
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
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Finally we return the queryPlan
		return qp;
	}
	
	private void deployQueryToNodes(){
		NodeManager.nLogger.info("-> Deploying operators to Nodes");
		//First configure statically (local) the connections between operators
		inf.deployQueryToNodes();
		//Finally deploy the new submitted query (instantiation, etc)
		try {
			// The code is previously sent to the nodes (when these attached to the master)
			//Send code to nodes (query code)
			try {
				inf.setUp();
			} 
			catch (CodeDeploymentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			inf.deploy();
		}
		catch (OperatorDeploymentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		catch (CodeDeploymentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		NodeManager.nLogger.info("-> DONE");
	}
	
	private String getUserInput(String msg) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println(msg);
		String option = br.readLine();
		return option;
	}
	
	public void startSystemOption(Infrastructure inf) throws IOException, ESFTRuntimeException{
		getUserInput("Press a button to start the source");
		//Start the source, and thus the stream processing system
		inf.start();
		System.out.println("System started, ");
		//Initialize local statistics
		inf.getMonitorManager().initSp();
		System.out.println("INITIALIZEZ SP");
	}
	
	public void configureSourceRateOption(Infrastructure inf) throws IOException{
		String option = getUserInput("Introduce number of events: ");
		int numberEvents = Integer.parseInt(option);
		option = getUserInput("Introduce time (ms): ");
		int time = Integer.parseInt(option);
		inf.configureSourceRate(numberEvents, time);
	}
	
	public void parallelizeOpManualOption(Infrastructure inf, ElasticInfrastructureUtils eiu) throws IOException{
		String option = getUserInput("Enter operator ID (old): ");
		int opId = Integer.parseInt(option);
		option = getUserInput("Enter operator ID (new): ");
		int newOpId = Integer.parseInt(option);
		System.out.println("1= get node automatically");
		System.out.println("2= get node manually, put new data");
		option = getUserInput("");
		int opt = Integer.parseInt(option);
		Node newNode = null;
		switch (opt){
			case 1:
				newNode = inf.getNodeFromPool();
				break;
			case 2:
				option = getUserInput("Introduce IP: ");
				InetAddress ip = InetAddress.getByName(option);
				option = getUserInput("Introduce port: ");
				int newPort = Integer.parseInt(option);
				newNode = new Node(ip, newPort);
				inf.addNode(newNode);
				break;
			default:
		}
		if(newNode == null){
			System.out.println("NO NODES AVAILABLE. IMPOSSIBLE TO PARALLELIZE");
			return;
		}
		eiu.scaleOutOperator(opId, newOpId, newNode);
	}
	
	public void consoleOutputMessage(){
		System.out.println("#############");
		System.out.println("USER Console, choose an option");
		System.out.println();
		System.out.println("0- Submit query to the System");
		System.out.println("1- Deploy query to Nodes");
		System.out.println("2- Start system");
		System.out.println("3- Configure source rate");
		System.out.println("4- Parallelize Operator Manually");
		System.out.println("5- Stop system console (EXP)");
		System.out.println("6- Exit");
		System.out.println("10- Parse txt file to binary kryo");
	}
}
