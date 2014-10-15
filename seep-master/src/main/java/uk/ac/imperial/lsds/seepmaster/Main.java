package uk.ac.imperial.lsds.seepmaster;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.api.LogicalSeepQuery;
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
			System.out.println("<querySourceFile.jar> <MainClass>");
			System.exit(0);
		}

		instance.executeMaster(args);
	}
	
	private void executeMaster(String[] args){
		// Get instance of MasterController and initialize it
		MasterController mc = MasterController.getInstance();
		mc.init();
		
		LogicalSeepQuery lsq = null;
		// If the user provided a query when launching the master node...
		if(args[0] != null){
			if(!(args.length > 2)){
				System.out.println("Error. Main Master <path_to_query.jar> <Base_class_name>");
				System.exit(0);
			}
			// Then we execute the compose method and get the QueryPlan back
			lsq = mc.executeComposeFromQuery(args[0], args[1]);
			// Once we have the QueryPlan from the user submitted query, we submit the query plan to the MasterController
			mc.submitQuery(lsq);
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