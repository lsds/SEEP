package uk.ac.imperial.lsds.seepmaster.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.query.QueryManager;

public class SimpleConsole implements UI{

	final private Logger LOG = LoggerFactory.getLogger(SimpleConsole.class.getName());
	
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
	
	public SimpleConsole(QueryManager qm, InfrastructureManager inf){
		this.qm = qm;
		this.inf = inf;
	}
	
	@Override
	public void start() {
		this.consoleOutputMessage();
		while(working){
			try{
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String option = br.readLine();
				switch(option){
				case "1":
					qm.deployQueryToNodes();
					break;
				case "2":
					qm.startQuery();
					break;
				case "3":
					qm.stopQuery();
					break;
				case "100":
					//
					break;
				default:
					//
				}
			}
			catch(IOException io){
				
			}
		}
		
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
		// TODO Auto-generated method stub
		
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
