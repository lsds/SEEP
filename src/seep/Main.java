package seep;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import seep.comm.BasicCommunicationUtils;
import seep.comm.routing.Router;
import seep.comm.tuples.Seep;
import seep.elastic.ElasticInfrastructureUtils;
import seep.infrastructure.DeploymentException;
import seep.infrastructure.ESFTRuntimeException;
import seep.infrastructure.Infrastructure;
import seep.infrastructure.Node;
import seep.infrastructure.NodeManager;
import seep.operator.Operator;
import seep.operator.collection.SmartWordCounter;
import seep.operator.collection.WordCounter;
import seep.operator.collection.WordSplitter;
import seep.operator.collection.WordSrc;
import seep.operator.collection.lrbenchmark.BACollector;
import seep.operator.collection.lrbenchmark.DataFeeder;
import seep.operator.collection.lrbenchmark.Forwarder;
import seep.operator.collection.lrbenchmark.Snk;
import seep.operator.collection.lrbenchmark.TollAssessment;
import seep.operator.collection.lrbenchmark.TollCalculator;
import seep.operator.collection.lrbenchmark.TollCollector;
import seep.operator.collection.testing.Bar;
import seep.operator.collection.testing.Foo;
import seep.operator.collection.testing.TestSink;
import seep.operator.collection.testing.TestSource;
import seep.utils.ExecutionConfiguration;

/**
* Main. The entry point of the whole system. This can be executed as Main (master Node) or as secondary.
*/

public class Main {
	
	//Runtime variable globals
	public static int eventR;
	public static int period;
	public static boolean maxRate;
	public static boolean eftMechanismEnabled;
	public static int numberOfXWays;
	
	//Properties object
	private static Properties globals = new Properties();
	
	//Method to get value doing: Main.valueFor(key) instead of Main.globals.getProperty(key)
	public static String valueFor(String key){
		return globals.getProperty(key);
	}
	
	//Load properties from file
	public boolean loadProperties(){
		boolean success = false;
		try {
			globals.load(new FileInputStream("config.properties"));
			success = true;
		}
		catch (FileNotFoundException e1) {
			System.out.println("Properties file not found "+e1.getMessage());
			e1.printStackTrace();
		}
		catch (IOException e1) {
			System.out.println("While loading properties file "+e1.getMessage());
			e1.printStackTrace();
		}
		//LOAD RUNTIME VAR GLOBALS FROM FILE HERE
		//#######################################
		return success;
	}
	
	public static void main(String args[]){
		
		Main instance = new Main();
		//Load configuration properties from the config file
		instance.loadProperties();
		
		if(args.length == 0){
			System.out.println("ARGS:");
			System.out.println("{Main/Sec} {masterIp} {masterPort/3500} {ownPort/3500}");
			System.exit(-1);
		}

		//master ip		
		InetAddress bindAddr = null;
		if (args.length >= 2){
			try {
				bindAddr = InetAddress.getByName(args[1]);
			}
			catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}

		//master node, 3500 by default
		int port;
		if (args.length >= 3){
			port = Integer.parseInt(args[2]);
		}
		else {
			port = 3500;
		}
		
		//own port, where I am listening
		int ownPort;
		if (args.length >= 4){
			ownPort = Integer.parseInt(args[3]);
		}
		else{
			ownPort = 3500;
		}

		//execution mode, main or secondary
		if(args[0].equals("Main")){
			//main receives the port where it will listen
			instance.executeMain(port);
		}
		else if(args[0].equals("Sec")){
			//secondary receives port and ip of master node
			instance.executeSec(port, bindAddr, ownPort);
		}
		else{
			System.out.println("Error. See Usage");
			System.exit(-1);
		}
	}
	
	void executeMain(int port){
		
		try {
			
			Infrastructure inf = new Infrastructure(port);
			ElasticInfrastructureUtils eiu = new ElasticInfrastructureUtils(inf);
			inf.startInfrastructure();

			boolean alive = true;
			
			/// \todo{make this robust}
			while(alive){
				consoleOutputMessage();
				try{
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					String option = br.readLine();
					int opt = Integer.parseInt(option);
					switch(opt){
						//deploy wordcounter
						case 1:
							deployWordCounterQueryOption(inf);
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
						// linear road benchmark example
						case 6:
							deployLinearRoadBenchmark(inf);
							break;
						case 7:
							deployTestingTopology(inf);
							break;
						case 8:
							deployLinearRoadBenchmark2(inf);
							break;
						case 9:
							parseTextFileToBinaryFile();
							System.out.println("FINISHED");
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
						case 14:
							System.out.println("Testing v0.1");
							testing01(inf);
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
		catch(DeploymentException de){
			System.out.println(de.getMessage());
		}
		catch(ESFTRuntimeException ere){
			System.out.println(ere.getMessage());
		}
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

	private void parseTextFileToBinaryFile() {
		String event = null;
		try{
			File inputFile = null;
			File outputFile = null;
			if(Main.valueFor("normalLRB").equals("True")){
				outputFile = new File(Main.valueFor("pathToOutputFile"));
				inputFile = new File(Main.valueFor("pathToInputFile"));
			}
			else{
				outputFile = new File(Main.valueFor("pathToOutputFileConstant"));
				inputFile = new File(Main.valueFor("pathToInputFileConstant"));
			}
			//FileOutputStream out = new FileOutputStream(outputFile);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
			//GZIPOutputStream gZipOut = new GZIPOutputStream(out);
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			event = null;
			while((event = br.readLine()) != null){
				//System.out.println("E_READ: "+event.length()*2);
				Seep.DataTuple.Builder tuple = buildDataTuple(event);
				if(tuple != null){
					Seep.DataTuple d = tuple.build();
					//System.out.println("E_SIZE: "+d.getSerializedSize());
					d.writeDelimitedTo(out);
				}
			}
		}
		catch(IOException io){
			System.out.println("THIS BROKE "+event);
			io.printStackTrace();
			
		}
	}
	
	private Seep.DataTuple.Builder buildDataTuple(String event){
//		System.out.println(event);
		Seep.DataTuple.Builder tuple = Seep.DataTuple.newBuilder();
		String fields[] = event.split(",");
		tuple.setTs(Integer.parseInt(fields[1]));
		tuple.setType(Integer.parseInt(fields[0]));
		if(tuple.getType() == 3 || tuple.getType() == 4) return null;
		tuple.setTime(Integer.parseInt(fields[1]));
		tuple.setVid(Integer.parseInt(fields[2]));
		tuple.setSpeed(Integer.parseInt(fields[3]));
		tuple.setXway(Integer.parseInt(fields[4]));
		tuple.setLane(Integer.parseInt(fields[5]));
		tuple.setDir(Integer.parseInt(fields[6]));
		tuple.setSeg(Integer.parseInt(fields[7]));
		tuple.setPos(Integer.parseInt(fields[8]));
		tuple.setQid(Integer.parseInt(fields[9]));
//		tuple.setSInit(Integer.parseInt(fields[10]));
//		tuple.setSEnd(Integer.parseInt(fields[11]));
//		tuple.setDow(Integer.parseInt(fields[12]));
//		tuple.setTow(Integer.parseInt(fields[13]));
//		tuple.setDay(Integer.parseInt(fields[14]));
		return tuple;
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
	}
	
	public void testing01(Infrastructure inf){
		//Instantiate operators
		TestSource src = new TestSource(-2);
		Foo foo = new Foo(0);
		Foo foo2 = new Foo(1);
		Bar bar = new Bar(2);
		TestSink snk = new TestSink(-1);
		//Configure source and sink
		inf.setSource(src);
		inf.setSink(snk);
		//Add operators to infrastructure
		inf.addOperator(src);
		inf.addOperator(foo);
		inf.addOperator(foo2);
		inf.addOperator(bar);
		inf.addOperator(snk);
		//Connect the operators to form the query
		src.connectTo(foo);
		foo.connectTo(foo2);
		foo.connectTo(bar);
		foo2.connectTo(snk);
		bar.connectTo(snk);
		//Routing information for the operators
		foo.setRoutingQueryFunction("getType");
		foo.route(Router.RelationalOperator.EQ, 0, foo2);
		foo.route(Router.RelationalOperator.EQ, 1, bar);
		//Set the query
		src.set();
		foo.set();
		foo2.set();
		bar.set();
		snk.set();
	}
	
	public void deployWordCounterQueryOption(Infrastructure inf) throws DeploymentException{
		System.out.println("Creating WordCounter query...");
		//Create operators
		WordSrc src = new WordSrc(-2);
		WordSplitter wsOp = new WordSplitter(0);
		SmartWordCounter wcOp = new SmartWordCounter(1);
		seep.operator.collection.Snk sink = new seep.operator.collection.Snk(-1);
		//Add operators to infrastructure
		inf.setSource(src);
		inf.addOperator(src);
		inf.addOperator(wsOp);
		inf.addOperator(wcOp);
		inf.setSink(sink);
		inf.addOperator(sink);
		//connect operators
		src.connectTo(wsOp);
		wsOp.connectTo(wcOp);
		wcOp.connectTo(sink);
		//Place operators in nodes
		inf.placeNew(src, inf.getNodeFromPool());
		inf.placeNew(wsOp, inf.getNodeFromPool());
		inf.placeNew(wcOp, inf.getNodeFromPool());
		inf.placeNew(sink, inf.getNodeFromPool());
		//deploy infrastructure
		inf.deploy();
	}
	
	private void deployLinearRoadBenchmark(Infrastructure inf) throws DeploymentException {
		System.out.println("Creating linear road benchmark...");
		//Create operators
		DataFeeder src = new DataFeeder(-2);
		Forwarder fw0 = new Forwarder(0);
		Forwarder fw1 = new Forwarder(1);
		Forwarder fw2 = new Forwarder(2);
		Forwarder fw3 = new Forwarder(3);
		Forwarder fw4 = new Forwarder(4);
		Forwarder fw5 = new Forwarder(5);
		Forwarder fw6 = new Forwarder(6);
		Forwarder fw7 = new Forwarder(7);
		Forwarder fw8 = new Forwarder(8);
		Forwarder fw9 = new Forwarder(9);
		Forwarder fw10 = new Forwarder(10);
		Forwarder fw11 = new Forwarder(11);
		Forwarder fw12 = new Forwarder(12);
		Forwarder fw13 = new Forwarder(13);
		Forwarder fw14 = new Forwarder(14);
		Forwarder fw15 = new Forwarder(15);
		
		TollCalculator tc0 = new TollCalculator(20);
		TollCalculator tc1 = new TollCalculator(21);
		TollCalculator tc2 = new TollCalculator(22);
		TollCalculator tc3 = new TollCalculator(23);
		TollCalculator tc4 = new TollCalculator(24);
		TollCalculator tc5 = new TollCalculator(25);
		TollCalculator tc6 = new TollCalculator(26);
		TollCalculator tc7 = new TollCalculator(27);
		TollCalculator tc8 = new TollCalculator(28);
		TollCalculator tc9 = new TollCalculator(29);
		TollCalculator tc10 = new TollCalculator(30);
		TollCalculator tc11 = new TollCalculator(31);
		TollCalculator tc12 = new TollCalculator(32);
		TollCalculator tc13 = new TollCalculator(33);
		TollCalculator tc14 = new TollCalculator(34);
		TollCalculator tc15 = new TollCalculator(35);
		
		TollAssessment ta0 = new TollAssessment(40);
		TollAssessment ta1 = new TollAssessment(41);
		TollAssessment ta2 = new TollAssessment(42);
		TollAssessment ta3 = new TollAssessment(43);
		TollAssessment ta4 = new TollAssessment(44);
		TollAssessment ta5 = new TollAssessment(45);
		TollAssessment ta6 = new TollAssessment(46);
		TollAssessment ta7 = new TollAssessment(47);
		TollAssessment ta8 = new TollAssessment(48);
		TollAssessment ta9 = new TollAssessment(49);
		TollAssessment ta10 = new TollAssessment(50);
		TollAssessment ta11 = new TollAssessment(51);
		TollAssessment ta12 = new TollAssessment(52);
		TollAssessment ta13 = new TollAssessment(53);
		TollAssessment ta14 = new TollAssessment(54);
		TollAssessment ta15 = new TollAssessment(55);
		
		TollCollector tCollector0 = new TollCollector(60);
		TollCollector tCollector1 = new TollCollector(61);
		TollCollector tCollector2 = new TollCollector(62);
		TollCollector tCollector3 = new TollCollector(63);
		TollCollector tCollector4 = new TollCollector(64);
		TollCollector tCollector5 = new TollCollector(65);
		TollCollector tCollector6 = new TollCollector(66);
		TollCollector tCollector7 = new TollCollector(67);
		
		BACollector baCollector = new BACollector(70);
		Snk c = new Snk(-1);
		//Add operators to infrastructure
		inf.setSource(src);
		inf.addOperator(src);
		inf.addOperator(fw0);
		inf.addOperator(fw1);
		inf.addOperator(fw2);
		inf.addOperator(fw3);
		inf.addOperator(fw4);
		inf.addOperator(fw5);
		inf.addOperator(fw6);
		inf.addOperator(fw7);
		inf.addOperator(fw8);
		inf.addOperator(fw9);
		inf.addOperator(fw10);
		inf.addOperator(fw11);
		inf.addOperator(fw12);
		inf.addOperator(fw13);
		inf.addOperator(fw14);
		inf.addOperator(fw15);
		
		inf.addOperator(tc0);
		inf.addOperator(tc1);
		inf.addOperator(tc2);
		inf.addOperator(tc3);
		inf.addOperator(tc4);
		inf.addOperator(tc5);
		inf.addOperator(tc6);
		inf.addOperator(tc7);
		inf.addOperator(tc8);
		inf.addOperator(tc9);
		inf.addOperator(tc10);
		inf.addOperator(tc11);
		inf.addOperator(tc12);
		inf.addOperator(tc13);
		inf.addOperator(tc14);
		inf.addOperator(tc15);
		
		inf.addOperator(ta0);
		inf.addOperator(ta1);
		inf.addOperator(ta2);
		inf.addOperator(ta3);
		inf.addOperator(ta4);
		inf.addOperator(ta5);
		inf.addOperator(ta6);
		inf.addOperator(ta7);
		inf.addOperator(ta8);
		inf.addOperator(ta9);
		inf.addOperator(ta10);
		inf.addOperator(ta11);
		inf.addOperator(ta12);
		inf.addOperator(ta13);
		inf.addOperator(ta14);
		inf.addOperator(ta15);

		inf.addOperator(tCollector0);
		inf.addOperator(tCollector1);
		inf.addOperator(tCollector2);
		inf.addOperator(tCollector3);
		inf.addOperator(tCollector4);
		inf.addOperator(tCollector5);
		inf.addOperator(tCollector6);
		inf.addOperator(tCollector7);
		
		inf.addOperator(baCollector);
		
		inf.setSink(c);
		inf.addOperator(c);
		//Connect operators
		src.connectTo(fw0);
		src.connectTo(fw1);
		src.connectTo(fw2);
		src.connectTo(fw3);
		src.connectTo(fw4);
		src.connectTo(fw5);
		src.connectTo(fw6);
		src.connectTo(fw7);
		src.connectTo(fw8);
		src.connectTo(fw9);
		src.connectTo(fw10);
		src.connectTo(fw11);
		src.connectTo(fw12);
		src.connectTo(fw13);
		src.connectTo(fw14);
		src.connectTo(fw15);

		fw0.connectTo(tc0);
		fw0.connectTo(ta0);
		fw1.connectTo(tc1);
		fw1.connectTo(ta1);
		fw2.connectTo(tc2);
		fw2.connectTo(ta2);
		fw3.connectTo(tc3);
		fw3.connectTo(ta3);
		fw4.connectTo(tc4);
		fw4.connectTo(ta4);
		fw5.connectTo(tc5);
		fw5.connectTo(ta5);
		fw6.connectTo(tc6);
		fw6.connectTo(ta6);
		fw7.connectTo(tc7);
		fw7.connectTo(ta7);
		fw8.connectTo(tc8);
		fw8.connectTo(ta8);
		fw9.connectTo(tc9);
		fw9.connectTo(ta9);
		fw10.connectTo(tc10);
		fw10.connectTo(ta10);
		fw11.connectTo(tc11);
		fw11.connectTo(ta11);
		fw12.connectTo(tc12);
		fw12.connectTo(ta12);
		fw13.connectTo(tc13);
		fw13.connectTo(ta13);
		fw14.connectTo(tc14);
		fw14.connectTo(ta14);
		fw15.connectTo(tc15);
		fw15.connectTo(ta15);

		tc0.connectTo(tCollector0);
		tc0.connectTo(ta0);
		tc1.connectTo(tCollector0);
		tc1.connectTo(ta1);
		tc2.connectTo(tCollector1);
		tc2.connectTo(ta2);
		tc3.connectTo(tCollector1);
		tc3.connectTo(ta3);
		tc4.connectTo(tCollector2);
		tc4.connectTo(ta4);
		tc5.connectTo(tCollector2);
		tc5.connectTo(ta5);
		tc6.connectTo(tCollector3);
		tc6.connectTo(ta6);
		tc7.connectTo(tCollector3);
		tc7.connectTo(ta7);
		tc8.connectTo(tCollector4);
		tc8.connectTo(ta8);
		tc9.connectTo(tCollector4);
		tc9.connectTo(ta9);
		tc10.connectTo(tCollector5);
		tc10.connectTo(ta10);
		tc11.connectTo(tCollector5);
		tc11.connectTo(ta11);
		tc12.connectTo(tCollector6);
		tc12.connectTo(ta12);
		tc13.connectTo(tCollector6);
		tc13.connectTo(ta13);
		tc14.connectTo(tCollector7);
		tc14.connectTo(ta14);
		tc15.connectTo(tCollector7);
		tc15.connectTo(ta15);
		
		ta0.connectTo(baCollector);
		ta1.connectTo(baCollector);
		ta2.connectTo(baCollector);
		ta3.connectTo(baCollector);
		ta4.connectTo(baCollector);
		ta5.connectTo(baCollector);
		ta6.connectTo(baCollector);
		ta7.connectTo(baCollector);
		ta8.connectTo(baCollector);
		ta9.connectTo(baCollector);
		ta10.connectTo(baCollector);
		ta11.connectTo(baCollector);
		ta12.connectTo(baCollector);
		ta13.connectTo(baCollector);
		ta14.connectTo(baCollector);
		ta15.connectTo(baCollector);
		
		tCollector0.connectTo(c);
		tCollector1.connectTo(c);
		tCollector2.connectTo(c);
		tCollector3.connectTo(c);
		tCollector4.connectTo(c);
		tCollector5.connectTo(c);
		tCollector6.connectTo(c);
		tCollector7.connectTo(c);
		
		baCollector.connectTo(c);
		//Place operators in nodes
		
		inf.placeNew(src, inf.getNodeFromPool());
		
//		Node n1 = inf.getNodeFromPool();
//		Node n2 = inf.getNodeFromPool();
//		Node n3 = inf.getNodeFromPool();
//		Node n4 = inf.getNodeFromPool();
//		Node n5 = inf.getNodeFromPool();
//		Node n6 = inf.getNodeFromPool();
//		Node n7 = inf.getNodeFromPool();
//		Node n8 = inf.getNodeFromPool();
		
		
		inf.placeNew(fw0, inf.getNodeFromPool());
		inf.placeNew(fw1, inf.getNodeFromPool());
		inf.placeNew(fw2, inf.getNodeFromPool());
		inf.placeNew(fw3, inf.getNodeFromPool());
		inf.placeNew(fw4, inf.getNodeFromPool());
		inf.placeNew(fw5, inf.getNodeFromPool());
		inf.placeNew(fw6, inf.getNodeFromPool());
		inf.placeNew(fw7, inf.getNodeFromPool());
		inf.placeNew(fw8, inf.getNodeFromPool());
		inf.placeNew(fw9, inf.getNodeFromPool());
		inf.placeNew(fw10, inf.getNodeFromPool());
		inf.placeNew(fw11, inf.getNodeFromPool());
		inf.placeNew(fw12, inf.getNodeFromPool());
		inf.placeNew(fw13, inf.getNodeFromPool());
		inf.placeNew(fw14, inf.getNodeFromPool());
		inf.placeNew(fw15, inf.getNodeFromPool());
		
		inf.placeNew(tc0, inf.getNodeFromPool());
		inf.placeNew(ta0, inf.getNodeFromPool());
		inf.placeNew(tc1, inf.getNodeFromPool());
		inf.placeNew(ta1, inf.getNodeFromPool());
		inf.placeNew(tc2, inf.getNodeFromPool());
		inf.placeNew(ta2, inf.getNodeFromPool());
		inf.placeNew(tc3, inf.getNodeFromPool());
		inf.placeNew(ta3, inf.getNodeFromPool());
		inf.placeNew(tc4, inf.getNodeFromPool());
		inf.placeNew(ta4, inf.getNodeFromPool());
		inf.placeNew(tc5, inf.getNodeFromPool());
		inf.placeNew(ta5, inf.getNodeFromPool());
		inf.placeNew(tc6, inf.getNodeFromPool());
		inf.placeNew(ta6, inf.getNodeFromPool());
		inf.placeNew(tc7, inf.getNodeFromPool());
		inf.placeNew(ta7, inf.getNodeFromPool());
		inf.placeNew(tc8, inf.getNodeFromPool());
		inf.placeNew(ta8, inf.getNodeFromPool());
		inf.placeNew(tc9, inf.getNodeFromPool());
		inf.placeNew(ta9, inf.getNodeFromPool());
		inf.placeNew(tc10, inf.getNodeFromPool());
		inf.placeNew(ta10, inf.getNodeFromPool());
		inf.placeNew(tc11, inf.getNodeFromPool());
		inf.placeNew(ta11, inf.getNodeFromPool());
		inf.placeNew(tc12, inf.getNodeFromPool());
		inf.placeNew(ta12, inf.getNodeFromPool());
		inf.placeNew(tc13, inf.getNodeFromPool());
		inf.placeNew(ta13, inf.getNodeFromPool());
		inf.placeNew(tc14, inf.getNodeFromPool());
		inf.placeNew(ta14, inf.getNodeFromPool());
		inf.placeNew(tc15, inf.getNodeFromPool());
		inf.placeNew(ta15, inf.getNodeFromPool());
		
		inf.placeNew(tCollector0, inf.getNodeFromPool());
		inf.placeNew(tCollector1, inf.getNodeFromPool());
		inf.placeNew(tCollector2, inf.getNodeFromPool());
		inf.placeNew(tCollector3, inf.getNodeFromPool());
		inf.placeNew(tCollector4, inf.getNodeFromPool());
		inf.placeNew(tCollector5, inf.getNodeFromPool());
		inf.placeNew(tCollector6, inf.getNodeFromPool());
		inf.placeNew(tCollector7, inf.getNodeFromPool());
		
		inf.placeNew(baCollector, inf.getNodeFromPool());
		inf.placeNew(c, inf.getNodeFromPool());
		//deploy infrastructure
		inf.deploy();
	}
	
	private void deployLinearRoadBenchmark2(Infrastructure inf) throws DeploymentException {
		System.out.println("Creating linear road benchmark...");
		//Create operators
		DataFeeder src = new DataFeeder(-2);
		Forwarder fw0 = new Forwarder(0);
		//AccidentDetector ad = new AccidentDetector(0);
		TollCalculator tc0 = new TollCalculator(10);
		//Notifier n = new Notifier(2);
		TollAssessment ta0 = new TollAssessment(20);
		TollCollector tCollector0 = new TollCollector(31);
		BACollector baCollector = new BACollector(34);
		Snk c = new Snk(-1);
		//Add operators to infrastructure
		inf.setSource(src);
		inf.addOperator(src);
		inf.addOperator(fw0);
		//inf.addOperator(ad);
		inf.addOperator(tc0);
		//inf.addOperator(n);
		inf.addOperator(ta0);
		inf.addOperator(tCollector0);
		inf.addOperator(baCollector);
		inf.setSink(c);
		inf.addOperator(c);
		//Connect operators
		src.connectTo(fw0);
		fw0.connectTo(tc0);
		fw0.connectTo(ta0);
		tc0.connectTo(tCollector0);
		tc0.connectTo(ta0);
		ta0.connectTo(baCollector);
		tCollector0.connectTo(c);
		baCollector.connectTo(c);
		//Place operators in nodes
		/*
		Node n1 = inf.getNodeFromPool();
		Node n2 = inf.getNodeFromPool();
		Node n3 = inf.getNodeFromPool();
		Node n4 = inf.getNodeFromPool();
		*/
//		Node n = inf.getNodeFromPool();
		inf.placeNew(src, inf.getNodeFromPool());
		inf.placeNew(fw0, inf.getNodeFromPool());
		inf.placeNew(tc0, inf.getNodeFromPool());
		inf.placeNew(ta0, inf.getNodeFromPool());
		inf.placeNew(tCollector0, inf.getNodeFromPool());
		inf.placeNew(baCollector, inf.getNodeFromPool());
		inf.placeNew(c, inf.getNodeFromPool());
		//deploy infrastructure
		inf.deploy();
	}
	
	private void deployTestingTopology(Infrastructure inf) throws DeploymentException {
		System.out.println("Creating testing topology");
		//Create operators
		TestSource src = new TestSource(-2);
//		DataFeeder src = new DataFeeder(-2);
		Foo foo = new Foo(0);
		Foo foo2 = new Foo(1);
//		TestSink snk = new Snk(-1);
//		Foo op1 = new Foo(0);
//		Foo op2 = new Foo(1);
//		Foo op3 = new Foo(2);
		TestSink snk = new TestSink(-1);
		//Add operators
		inf.setSource(src);
		inf.addOperator(src);
		inf.addOperator(foo);
		inf.addOperator(foo2);
//		inf.addOperator(op1);
//		inf.addOperator(op2);
//		inf.addOperator(op3);
		inf.setSink(snk);
		inf.addOperator(snk);
		//Connect operators
//		src.connectTo(snk);
		src.connectTo(foo);
		foo.connectTo(foo2);
		foo2.connectTo(snk);
//		foo2.connectTo(snk);
		//Place operators in nodes
		inf.placeNew(src, inf.getNodeFromPool());
		inf.placeNew(foo, inf.getNodeFromPool());
		inf.placeNew(foo2, inf.getNodeFromPool());
		inf.placeNew(snk, inf.getNodeFromPool());
		//deploy
		inf.deploy();
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
	
	/**
	 * SECONDARY NODES EXECUTE THIS METHOD
	 */
	
	private void executeSec(int port, InetAddress bindAddr, int ownPort){
		// NodeManager instantiation
		NodeManager nm = new NodeManager(port, bindAddr, ownPort);
		nm.init();
		NodeManager.nLogger.info("NodeManager was remotely stopped");
	}
}
