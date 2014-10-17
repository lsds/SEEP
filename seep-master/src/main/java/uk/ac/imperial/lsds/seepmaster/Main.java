package uk.ac.imperial.lsds.seepmaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seepmaster.comm.MasterWorkerAPIImplementation;
import uk.ac.imperial.lsds.seepmaster.comm.MasterWorkerCommManager;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManagerFactory;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureType;
import uk.ac.imperial.lsds.seepmaster.query.QueryManager;
import uk.ac.imperial.lsds.seepmaster.ui.UI;
import uk.ac.imperial.lsds.seepmaster.ui.UIFactory;
import uk.ac.imperial.lsds.seepmaster.ui.UIType;

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
		// Get the properties that apply
		// TODO
		QueryManager qm = QueryManager.getInstance();
		InfrastructureManager inf = InfrastructureManagerFactory.createInfrastructureManager(InfrastructureType.PHYSICAL_CLUSTER);
		// put this in the config manager
		int port = Integer.parseInt(GLOBALS.valueFor("mainPort"));
		MasterWorkerAPIImplementation api = new MasterWorkerAPIImplementation(qm, inf);
		MasterWorkerCommManager mwcm = new MasterWorkerCommManager(port, api);
		mwcm.start();
		UI ui = UIFactory.createUI(UIType.CONSOLE, qm, inf);
		//OldMasterController mc = OldMasterController.getInstance();
		//ManagerWorker manager = new ManagerWorker(this, port);
		//Thread centralManagerT = new Thread(manager, "managerWorkerT");
		//centralManagerT.start();
		//mc.init();
		
		//LogicalSeepQuery lsq = null;
		// If the user provided a query when launching the master node...
		if(args[0] != null){
			if(!(args.length > 2)){
				System.out.println("Error. Main Master <path_to_query.jar> <Base_class_name>");
				System.exit(0);
			}
			// Then we execute the compose method and get the QueryPlan back
			//lsq = mc.executeComposeFromQuery(args[0], args[1]);
			//lsq = qm.executeComposeFromQuery(args[0], args[1]);
			qm.loadQueryFromFile(args[0], args[1]);
			// Once we have the QueryPlan from the user submitted query, we submit the query plan to the MasterController
			//mc.submitQuery(lsq);
		}
		ui.start();
	}
}