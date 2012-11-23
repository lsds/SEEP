package seep.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import seep.P;
import seep.elastic.ElasticInfrastructureUtils;

public class MasterController {

	//MasterController must be a singleton 
	private static final MasterController instance = new MasterController();
	 
    private MasterController() {}
 
    public static MasterController getInstance() {
        return instance;
    }
	
    private Infrastructure inf;
    ElasticInfrastructureUtils eiu;
    P prop = new P();
	
	public void init(){
		prop.loadProperties();
		inf = new Infrastructure(Integer.parseInt(P.valueFor("masterPort")));
		eiu = new ElasticInfrastructureUtils(inf);
		inf.startInfrastructure();
	}
	
	public void submitQuery(QueryPlan qp){
		inf.loadQuery(qp);
	}
	
	public void start() throws DeploymentException{
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
						case 10:
							System.out.println("BYE");
							System.exit(0);
							break;
						case 11:
							System.out.println("SAVE RESULTS");
							saveResults(inf);
							break;
						case 12:
							System.out.println("SWITCH ESFT MECHANISMS");
							switchMechanisms(inf);
							break;
						case 13:
							System.out.println("save latency SWC-query");
							saveResultsSWC(inf);
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
	
	private void saveResultsSWC(Infrastructure inf) {
		inf.saveResultsSWC();
	}

	private void switchMechanisms(Infrastructure inf){
		inf.switchMechanisms();
	}
	
	private void saveResults(Infrastructure inf){
		inf.saveResults();
	}
	
	public void consoleOutputMessage(){
		System.out.println("#############");
		System.out.println("USER Console, choose an option");
		System.out.println();
		System.out.println("1- Deploy WordCounter example");
		System.out.println("2- Start system");
		System.out.println("3- Configure source rate");
		System.out.println("4- Parallelize Operator Manually");
		System.out.println("5- Stop system console (EXP)");
		System.out.println("6- Deploy Linear Road Benchmark");
		System.out.println("7- Deploy testing topology");
		System.out.println("8- Deploy LRB testing topology");
		System.out.println("9- Parse text file to binary file");
		System.out.println("10- EXIT");
		System.out.println("11- Save results");
		System.out.println("12- Switch ESFT mechanisms activation");
		System.out.println("13");
		System.out.println("14- tsetint v0.1 pipeline");
		System.out.println("15- tsetint v0.1 NOT pipeline");
		System.out.println("16- wikipedia data to binary data");
	}
}
