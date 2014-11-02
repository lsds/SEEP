package uk.ac.imperial.lsds.seepmaster;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;

import joptsimple.OptionParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.Comm;
import uk.ac.imperial.lsds.seep.comm.IOComm;
import uk.ac.imperial.lsds.seep.comm.serialization.JavaSerializer;
import uk.ac.imperial.lsds.seep.config.CommandLineArgs;
import uk.ac.imperial.lsds.seep.config.ConfigKey;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seep.util.Utils;
import uk.ac.imperial.lsds.seepmaster.comm.MasterWorkerAPIImplementation;
import uk.ac.imperial.lsds.seepmaster.comm.MasterWorkerCommManager;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManagerFactory;
import uk.ac.imperial.lsds.seepmaster.query.QueryManager;
import uk.ac.imperial.lsds.seepmaster.ui.UI;
import uk.ac.imperial.lsds.seepmaster.ui.UIFactory;


public class Main {
	
	final private static Logger LOG = LoggerFactory.getLogger(Main.class);

	private void executeMaster(String[] args, MasterConfig mc){
		int infType = mc.getInt(MasterConfig.DEPLOYMENT_TARGET_TYPE);
		InfrastructureManager inf = InfrastructureManagerFactory.createInfrastructureManager(infType);
		// TODO: get file from config if exists and parse it to get a map from operator to endPoint
		Map<Integer, EndPoint> mapOperatorToEndPoint = null;
		// TODO: from properties get serializer and type of thread pool and resources assigned to it
		Comm comm = new IOComm(new JavaSerializer(), Executors.newCachedThreadPool());
		QueryManager qm = QueryManager.getInstance(inf, mapOperatorToEndPoint, comm);
		// TODO: put this in the config manager
		int port = mc.getInt(MasterConfig.LISTENING_PORT);
		MasterWorkerAPIImplementation api = new MasterWorkerAPIImplementation(qm, inf);
		MasterWorkerCommManager mwcm = new MasterWorkerCommManager(port, api);
		mwcm.start();
		int uiType = mc.getInt(MasterConfig.UI_TYPE);
		UI ui = UIFactory.createUI(uiType, qm, inf);
		//OldMasterController mc = OldMasterController.getInstance();
		//ManagerWorker manager = new ManagerWorker(this, port);
		//Thread centralManagerT = new Thread(manager, "managerWorkerT");
		//centralManagerT.start();
		//mc.init();
		
		//LogicalSeepQuery lsq = null;
		// If the user provided a query when launching the master node...
		if(args[0] != null){
			System.out.println(args.length);
			if(!(args.length > 1)){
				System.out.println("Error. Main Master <path_to_query.jar> <Base_class_name>");
				System.exit(0);
			}
			// Then we execute the compose method and get the QueryPlan back
			//lsq = mc.executeComposeFromQuery(args[0], args[1]);
			//lsq = qm.executeComposeFromQuery(args[0], args[1]);
			String queryPathFile = mc.getString(MasterConfig.QUERY_FILE);
			String baseClass = mc.getString(MasterConfig.BASECLASS_NAME);
			qm.loadQueryFromFile(queryPathFile, baseClass);
			// Once we have the QueryPlan from the user submitted query, we submit the query plan to the MasterController
			//mc.submitQuery(lsq);
		}
		ui.start();
	}
	
	public static void main(String args[]){

		// Get Properties with command line configuration 
		List<ConfigKey> configKeys = MasterConfig.getAllConfigKey();
		OptionParser parser = new OptionParser();
		parser.accepts("query.file", "Jar file with the compiled SEEP query").withRequiredArg();
		parser.accepts("baseclass.name", "Name of the Base Class").withRequiredArg();
		CommandLineArgs cla = new CommandLineArgs(args, parser, configKeys);
		Properties commandLineProperties = cla.getProperties();
		
		// Get Properties with file configuration
		Properties fileProperties = null;
		if(commandLineProperties.containsKey(MasterConfig.PROPERTIES_FILE)){
			String propertiesFile = commandLineProperties.getProperty("properties.file");
			fileProperties = Utils.readPropertiesFromFile(propertiesFile, false);
		}
		else{
			fileProperties = Utils.readPropertiesFromFile("config.properties", true);
		}
		
		// Merge both properties, command line has preference
		Properties validatedProperties = Utils.overwriteSecondPropertiesWithFirst(commandLineProperties, fileProperties);
		// TODO: validte properties, making sure all required are there
		MasterConfig mc = new MasterConfig(validatedProperties);
		Main instance = new Main();
		instance.executeMaster(args, mc);
	}
}