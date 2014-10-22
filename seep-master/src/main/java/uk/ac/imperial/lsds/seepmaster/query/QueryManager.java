package uk.ac.imperial.lsds.seepmaster.query;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.api.LogicalOperator;
import uk.ac.imperial.lsds.seep.api.LogicalSeepQuery;
import uk.ac.imperial.lsds.seep.api.PhysicalOperator;
import uk.ac.imperial.lsds.seep.api.PhysicalSeepQuery;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.ExecutionUnit;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;

public class QueryManager {

	final private Logger LOG = LoggerFactory.getLogger(QueryManager.class);
	
	private static QueryManager qm;
	private String pathToQuery;
	private LogicalSeepQuery lsq;
	
	private PhysicalSeepQuery originalQuery;
	private PhysicalSeepQuery runtimeQuery;
	
	private InfrastructureManager inf;
	private Map<Integer, EndPoint> opToEndpointMapping;
	
	private int executionUnitsRequiredToStart;
	
	private QueryManager(InfrastructureManager inf, Map<Integer, EndPoint> mapOpToEndPoint){
		this.inf = inf;
		this.opToEndpointMapping = mapOpToEndPoint;
	}
	
	public static QueryManager getInstance(InfrastructureManager inf, Map<Integer, EndPoint> mapOpToEndPoint){
		if(qm == null){
			return new QueryManager(inf, mapOpToEndPoint);
		}
		else{
			return qm;
		}
	}
	
	private boolean canStartExecution(){
		return inf.executionUnitsAvailable() >= executionUnitsRequiredToStart;
	}
	
	public void loadQueryFromFile(String pathToJar, String definitionClass){
		this.pathToQuery = pathToJar;
		this.lsq = executeComposeFromQuery(pathToJar, definitionClass);
		this.executionUnitsRequiredToStart = this.computeRequiredExecutionUnits(lsq);
	}
	
	public void deployQueryToNodes(){
		// Check whether there are sufficient execution units to deploy query
		if(! canStartExecution()){
			// return error to UI
		}
		// 1 create connections between operators
		// get node, and put operator in node by assigning control and data socket, etc
		createOriginalPhysicalQuery();
		// 2 create initial star topology
		// stupid stuff
		// 3 deploy code to nodes
		// read and send the actual code to all workers
		// 4 deploy query to nodes
		// first send starTopology
		// send operator, serialization of operator
		sendQueryInformationToNodes();
		// after all nodes have operators, then send init (the one who activates connections)
		// broadcast state so that they can register these states
		// send SET-RUNTIME command
	}
	
	private void createOriginalPhysicalQuery(){
		// use pre-defined description if exists
		if(this.opToEndpointMapping != null){
			for(Entry<Integer, EndPoint> e : opToEndpointMapping.entrySet()){
				LogicalOperator lo = lsq.getOperatorWithId(e.getKey());
				if(lo != null) {
					PhysicalOperator po = PhysicalOperator.createPhysicalOperatorFromLogicalOperatorAndEndPoint(lo, e.getValue());
				}
			}
		}
		// otherwise map to random workers
		else{
			for(LogicalOperator lso : lsq.getAllOperators()){
				ExecutionUnit eu = inf.getExecutionUnit();
				PhysicalOperator po = PhysicalOperator.createPhysicalOperatorFromLogicalOperatorAndEndPoint(lso, eu.getEndPoint());
			}
		}
	}
	
	private void sendQueryInformationToNodes(){
		
	}
	
	public void startQuery(){
		
	}
	
	public void stopQuery(){
		
	}
	
	private int computeRequiredExecutionUnits(LogicalSeepQuery lsq){
		int totalInstances = 0;
		for(LogicalOperator lo : lsq.getAllOperators()){
			int opId = lo.getLogicalOperatorId();
			if(lsq.hasSetInitialPhysicalInstances(opId)){
				totalInstances += lsq.getInitialPhysicalInstancesForLogicalOperator(opId);
			}
		}
		return totalInstances;
	}
	
	private LogicalSeepQuery executeComposeFromQuery(String pathToJar, String definitionClass){
		Class<?> baseI = null;
		Object baseInstance = null;
		Method compose = null;
		LogicalSeepQuery lsq = null;
		//inf.setPathToQueryDefinition(pathToJar);
		File urlPathToQueryDefinition = new File(pathToJar);
		LOG.debug("-> Set path to query definition: {}", urlPathToQueryDefinition.getAbsolutePath());
		URL[] urls = new URL[1];
		try {
			urls[0] = urlPathToQueryDefinition.toURI().toURL();
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// First time it is created we pass the urls
		URLClassLoader ucl = new URLClassLoader(urls);
		//eiu.setClassLoader(ucl);
		try {
			baseI = ucl.loadClass(definitionClass);
			baseInstance = baseI.newInstance();
			compose = baseI.getDeclaredMethod("compose", (Class<?>[])null);
			lsq = (LogicalSeepQuery) compose.invoke(baseInstance, (Object[])null);
		}
		catch (SecurityException e) {
			e.printStackTrace();
		} 
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
		catch (InstantiationException e) {
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		//Finally we return the queryPlan
		return lsq;
	}
	
}
