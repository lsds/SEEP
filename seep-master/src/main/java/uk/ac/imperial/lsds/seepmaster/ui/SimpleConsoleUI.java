package uk.ac.imperial.lsds.seepmaster.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.query.QueryManager;

public class SimpleConsoleUI implements UI{

	final private Logger LOG = LoggerFactory.getLogger(SimpleConsoleUI.class.getName());
	
	private static String uiText;
	private static String emptyText;
	
	private boolean working = false;
	
	static{
		// Build ui text
		StringBuilder sb = new StringBuilder();
		sb.append("#############");
		sb.append(System.getProperty("line.separator"));
		sb.append("SEEP Master SimpleConsole");
		sb.append(System.getProperty("line.separator"));
		sb.append("--------------");
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		sb.append("Choose an option and press Enter");
		sb.append(System.getProperty("line.separator"));
		sb.append("1. Deploy query to Cluster");
		sb.append(System.getProperty("line.separator"));
		sb.append("2. Start query");
		sb.append(System.getProperty("line.separator"));
		sb.append("3. Stop query");
		sb.append(System.getProperty("line.separator"));
		sb.append("100. Exit");

		uiText = sb.toString();
		
		StringBuilder sb2 = new StringBuilder();
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		
		emptyText = sb2.toString();
	}
	
	private QueryManager qm;
	private InfrastructureManager inf;
	
	public SimpleConsoleUI(QueryManager qm, InfrastructureManager inf){
		this.qm = qm;
		this.inf = inf;
	}
	
	@Override
	public void start() {
		working = true;
		this.consoleOutputMessage();
		LOG.info("Entering UI simpleConsole...");
		while(working){
			try{
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String option = br.readLine();
				switch(option){
				case "1":
					LOG.info("Deploying query to nodes...");
					qm.deployQueryToNodes();
					LOG.info("Deploying query to nodes...OK");
					break;
				case "2":
					LOG.info("Starting query...");
					qm.startQuery();
					LOG.info("Starting query...OK");
					break;
				case "3":
					LOG.info("Stopping query...");
					qm.stopQuery();
					LOG.info("Stopping query...OK");
					break;
				case "100":
					//
					break;
				default:
					System.out.println("NOT RECOGNIZED");
					consoleOutputMessage();
				}
			}
			catch(IOException io){
				
			}
		}
		LOG.info("Exiting UI simpleConsole...");
		
//		LOG.info("-> Console, waiting for commands: ");
//		try {
//			boolean alive = true;
//			/// \todo{make this robust}
//			while(alive){
//				consoleOutputMessage();
//				try{
//					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//					String option = br.readLine();
//					int opt = Integer.parseInt(option);
//					switch(opt){
//						//Map operators to nodes
//						case 0:
//							System.out.println("Not implemented yet");
//							//Submit Query to the system
//							break;
//						case 1:
//							deployQueryToNodes();
//							break;
//						//start system
//						case 2:
//							startSystemOption(inf);
//							break;
//						//configure source rate
////						case 3:
////							configureSourceRateOption(inf);
////							break;
//						//parallelize operator manually
//						case 4:
//							parallelizeOpManualOption(inf, eiu);
//							break;
//						//silent the console
//						case 5:
//							alive = false;
//							inf.stopWorkers();
//							System.out.println("ENDING console...");
//							break;
//						//Exit the system
//						case 6:
//							System.out.println("BYE");
//							System.exit(0);
//							break;
//						case 10:
//							System.out.println("Parsing txt file...");
//							inf.parseFileForNetflix();
//							break;
//						default:
//							System.out.println("Wrong option. Try again...");
//					}
//				}
//				catch(IOException io){
//					System.out.println("While reading from terminal: "+io.getMessage());
//					io.printStackTrace();
//				}			
//			}
//			System.out.println("BYE");
//
//		}
//		catch(ESFTRuntimeException ere){
//			System.out.println(ere.getMessage());
//		}
//		catch(Exception g){
//			System.out.println(g.getMessage());
//		}
		
	}

	@Override
	public void stop() {
		this.working = false;
	}
	
	private String getUserInput(String msg) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println(msg);
		String option = br.readLine();
		return option;
	}
	
	public void consoleOutputMessage(){
		// Shallow attempt to empty screen
		System.out.println(emptyText);
		// Print message
		System.out.println(uiText);
	}

}
