package uk.ac.imperial.lsds.seepmaster.query;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.api.LogicalSeepQuery;
import uk.ac.imperial.lsds.seep.api.Operator;
import uk.ac.imperial.lsds.seep.api.PhysicalSeepQuery;
import uk.ac.imperial.lsds.seep.api.SeepQueryPhysicalOperator;
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
	
	public PhysicalSeepQuery getOriginalPhysicalQuery(){
		return originalQuery;
	}
	
	public PhysicalSeepQuery getRuntimePhysicalQuery(){
		return runtimeQuery;
	}
	
	public QueryManager(LogicalSeepQuery lsq, InfrastructureManager inf, Map<Integer, EndPoint> mapOpToEndPoint){
		this.lsq = lsq;
		this.executionUnitsRequiredToStart = this.computeRequiredExecutionUnits(lsq);
		this.inf = inf;
		this.opToEndpointMapping = mapOpToEndPoint;
	}
	
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
		if(!canStartExecution()){
			// return error to UI
		}
		// 1 create connections between operators
		// get node, and put operator in node by assigning control and data socket, etc
		originalQuery = createOriginalPhysicalQuery();
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
	
	private PhysicalSeepQuery createOriginalPhysicalQuery(){
		Set<SeepQueryPhysicalOperator> physicalOperators = new HashSet<>();
		Map<Operator, List<Operator>> instancesPerOriginalOp = new HashMap<>();
		// use pre-defined description if exists
		if(this.opToEndpointMapping != null){
			for(Entry<Integer, EndPoint> e : opToEndpointMapping.entrySet()){
//				LogicalOperator lo = lsq.getOperatorWithId(e.getKey());
//				if(lo != null) {
//					SeepQueryPhysicalOperator po = SeepQueryPhysicalOperator.createPhysicalOperatorFromLogicalOperatorAndEndPoint(lo, e.getValue());
//					physicalOperators.add(po);
//				}
			}
		}
		// otherwise map to random workers
		else{
			for(Operator lso : lsq.getAllOperators()){
				ExecutionUnit eu = inf.getExecutionUnit();
				SeepQueryPhysicalOperator po = SeepQueryPhysicalOperator.createPhysicalOperatorFromLogicalOperatorAndEndPoint(lso, eu.getEndPoint());
				physicalOperators.add(po);
				// get number of replicas
				int numInstances = lsq.getInitialPhysicalInstancesForLogicalOperator(lso.getOperatorId());
				int originalOpId = lso.getOperatorId();
				for(int i = 0; i<numInstances; i++) {
					int instanceOpId = getNewOpIdForInstance(originalOpId, i);
					ExecutionUnit euInstance = inf.getExecutionUnit();
					SeepQueryPhysicalOperator poInstance = SeepQueryPhysicalOperator.createPhysicalOperatorFromLogicalOperatorAndEndPoint(instanceOpId, lso, euInstance.getEndPoint());
					physicalOperators.add(poInstance);
					addInstanceForOriginalOp(po, poInstance, instancesPerOriginalOp);
				}
			}
		}
		return PhysicalSeepQuery.buildPhysicalQueryFrom(physicalOperators, instancesPerOriginalOp, lsq);
	}
	
	private void addInstanceForOriginalOp(SeepQueryPhysicalOperator po, SeepQueryPhysicalOperator newInstance, 
			Map<Operator, List<Operator>> instancesPerOriginalOp) {
		if(instancesPerOriginalOp.containsKey(po)) {
			instancesPerOriginalOp.get(po).add(newInstance);
		}
		else{
			List<Operator> newInstances = new ArrayList<>();
			newInstances.add(newInstance);
			instancesPerOriginalOp.put(po, newInstances);
		}
	}
	
	private int getNewOpIdForInstance(int opId, int it){
		return opId * it + 1000;
	}
	
	private void sendQueryInformationToNodes(){
		
	}
	
	public void startQuery(){
		
	}
	
	public void stopQuery(){
		
	}
	
	private int computeRequiredExecutionUnits(LogicalSeepQuery lsq){
		int totalInstances = 0;
		for(Operator lo : lsq.getAllOperators()){
			int opId = lo.getOperatorId();
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
