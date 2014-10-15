package uk.ac.imperial.lsds.seepmaster;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.api.QueryPlan;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.OperatorDeploymentException;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.MasterController;

/**
* Main. This can be executed as Main (master Node) or as secondary.
*/

public class Main {
	
	final private static Logger LOG = LoggerFactory.getLogger(Main.class);
	
	public static void main(String args[]){
		
		Main instance = new Main();
		
		if(args.length == 0){
			System.out.println("ARGS:");
			System.out.println("Master <querySourceFile.jar> <policyRulesFile.jar> <MainClass>");
			System.out.println("Worker <localPort>");
			System.exit(0);
		}

		if(args[0].equals("Master")){
			instance.executeMaster(args);
		}
		else if(args[0].equals("Worker")){
			//secondary receives port and ip of master node
			//instance.executeSec(args);
		}
		else{
			System.out.println("Unrecognized command. Type 'Master' or 'Worker' to see usage directions for each mode.");
			System.exit(0);
		}
	}
	
	private void executeMaster(String[] args){
		//Get instance of MasterController and initialize it
		MasterController mc = MasterController.getInstance();
		mc.init();
		
		QueryPlan qp = null;
		//If the user provided a query when launching the master node...
		if(args[1] != null){
			if(!(args.length > 2)){
				System.out.println("Error. Main Master <path_to_query.jar> <Base_class_name>");
				System.exit(0);
			}
			//Then we execute the compose method and get the QueryPlan back
			qp = mc.executeComposeFromQuery(args[1], args[2]);
			//Once we have the QueryPlan from the user submitted query, we submit the query plan to the MasterController
			mc.submitQuery(qp);
		}
		//In any case we start the MasterController to get access to the interface
		try {
			mc.start();
		}
		catch (OperatorDeploymentException e) {
			e.printStackTrace();
		}
	}
}