package uk.ac.imperial.lsds.seepmaster.query;

import java.io.File;
import java.io.IOException;
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

import com.esotericsoftware.kryo.Kryo;

import uk.ac.imperial.lsds.seep.api.LogicalSeepQuery;
import uk.ac.imperial.lsds.seep.api.Operator;
import uk.ac.imperial.lsds.seep.api.PhysicalOperator;
import uk.ac.imperial.lsds.seep.api.PhysicalSeepQuery;
import uk.ac.imperial.lsds.seep.api.SeepQueryPhysicalOperator;
import uk.ac.imperial.lsds.seep.comm.Comm;
import uk.ac.imperial.lsds.seep.comm.Connection;
import uk.ac.imperial.lsds.seep.comm.KryoFactory;
import uk.ac.imperial.lsds.seep.comm.protocol.Command;
import uk.ac.imperial.lsds.seep.comm.protocol.ProtocolCommandFactory;
import uk.ac.imperial.lsds.seep.infrastructure.EndPoint;
import uk.ac.imperial.lsds.seep.util.Utils;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.ExecutionUnit;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;

public class QueryManager {

	final private Logger LOG = LoggerFactory.getLogger(QueryManager.class);
	
	private static QueryManager qm;
	private String pathToQuery;
	private LogicalSeepQuery lsq;
	private PhysicalSeepQuery originalQuery;
	private PhysicalSeepQuery runtimeQuery;
	private int executionUnitsRequiredToStart;
	
	private InfrastructureManager inf;
	private Map<Integer, EndPoint> opToEndpointMapping;
	
	private final Comm comm;
	private final Kryo k;
	
	public PhysicalSeepQuery getOriginalPhysicalQuery(){
		return originalQuery;
	}
	
	public PhysicalSeepQuery getRuntimePhysicalQuery(){
		return runtimeQuery;
	}
	
	public QueryManager(LogicalSeepQuery lsq, InfrastructureManager inf, Map<Integer, EndPoint> mapOpToEndPoint,
			Comm comm){
		this.lsq = lsq;
		this.executionUnitsRequiredToStart = this.computeRequiredExecutionUnits(lsq);
		this.inf = inf;
		
		this.opToEndpointMapping = mapOpToEndPoint;
		this.comm = comm;
		this.k = KryoFactory.buildKryoForMasterWorkerProtocol();
	}
	
	private QueryManager(InfrastructureManager inf, Map<Integer, EndPoint> mapOpToEndPoint, Comm comm){
		this.inf = inf;
		this.opToEndpointMapping = mapOpToEndPoint;
		this.comm = comm;
		this.k = KryoFactory.buildKryoForMasterWorkerProtocol();
	}
	
	public static QueryManager getInstance(InfrastructureManager inf, Map<Integer, EndPoint> mapOpToEndPoint, Comm comm){
		if(qm == null){
			return new QueryManager(inf, mapOpToEndPoint, comm);
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
		// get logical query
		this.lsq = executeComposeFromQuery(pathToJar, definitionClass);
		LOG.debug("Logical query loaded: {}", lsq.toString());
		// get *all* classes required by that query and store their names
		this.executionUnitsRequiredToStart = this.computeRequiredExecutionUnits(lsq);
		LOG.info("New query requires: {} units to start execution", this.executionUnitsRequiredToStart);
	}
	
	public void deployQueryToNodes(){
		// Check whether there are sufficient execution units to deploy query
		if(!canStartExecution()){
			LOG.warn("Cannot deploy query, not enough nodes. Required: {}, available: {}"
					, executionUnitsRequiredToStart, inf.executionUnitsAvailable());
			return;
		}
		LOG.info("Building physicalQuery from logicalQuery...");
		originalQuery = createOriginalPhysicalQuery();
		LOG.debug("Building physicalQuery from logicalQuery...OK {}", originalQuery.toString());
		// 4 deploy query to nodes
		// first send starTopology
		// send operator, serialization of operator
		Set<Integer> involvedEUId = originalQuery.getIdOfEUInvolved();
		Set<Connection> connections = inf.getConnectionsTo(involvedEUId);
		sendQueryInformationToNodes(connections);
		// after all nodes have operators, then send init (the one who activates connections)
		// broadcast state so that they can register these states
		// send SET-RUNTIME command
	}
	
	private PhysicalSeepQuery createOriginalPhysicalQuery(){
		Set<SeepQueryPhysicalOperator> physicalOperators = new HashSet<>();
		Map<PhysicalOperator, List<PhysicalOperator>> instancesPerOriginalOp = new HashMap<>();
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
			this.opToEndpointMapping = new HashMap<>();
			
			for(Operator lso : lsq.getAllOperators()){
				ExecutionUnit eu = inf.getExecutionUnit();
				SeepQueryPhysicalOperator po = SeepQueryPhysicalOperator.createPhysicalOperatorFromLogicalOperatorAndEndPoint(lso, eu.getEndPoint());
				int pOpId = po.getOperatorId();
				EndPoint ep = eu.getEndPoint();
				LOG.debug("LogicalOperator: {} will run on: {}", pOpId, ep.getId());
				opToEndpointMapping.put(pOpId, ep);
				physicalOperators.add(po);
				// get number of replicas
				int numInstances = lsq.getInitialPhysicalInstancesForLogicalOperator(lso.getOperatorId());
				LOG.debug("LogicalOperator: {} requires {} executionUnits", lso.getOperatorId(), numInstances);
				int originalOpId = lso.getOperatorId();
				// Start with 1 because that's the minimum anyway
				for(int i = 1; i < numInstances; i++) {
					int instanceOpId = getNewOpIdForInstance(originalOpId, i);
					ExecutionUnit euInstance = inf.getExecutionUnit();
					SeepQueryPhysicalOperator poInstance = SeepQueryPhysicalOperator.createPhysicalOperatorFromLogicalOperatorAndEndPoint(instanceOpId, lso, euInstance.getEndPoint());
					physicalOperators.add(poInstance);
					addInstanceForOriginalOp(po, poInstance, instancesPerOriginalOp);
				}
			}
		}
		PhysicalSeepQuery psq = PhysicalSeepQuery.buildPhysicalQueryFrom(physicalOperators, instancesPerOriginalOp, lsq);
		return psq;
	}
	
	private void addInstanceForOriginalOp(SeepQueryPhysicalOperator po, SeepQueryPhysicalOperator newInstance, 
			Map<PhysicalOperator, List<PhysicalOperator>> instancesPerOriginalOp) {
		if(instancesPerOriginalOp.containsKey(po)) {
			instancesPerOriginalOp.get(po).add(newInstance);
		}
		else{
			List<PhysicalOperator> newInstances = new ArrayList<>();
			newInstances.add(newInstance);
			instancesPerOriginalOp.put((PhysicalOperator)po, newInstances);
		}
	}
	
	private int getNewOpIdForInstance(int opId, int it){
		return opId * it + 1000;
	}
	
	private void sendQueryInformationToNodes(Set<Connection> connections){
		
		/**
		 * what a worker expects
		 * 
		 * 1. send command "CODE"
		 * 2. send actual code
		 * 
		 * 3. send starTopology
		 * 4. send operator to only the specific node (instantiation)
		 * 5. send in parallel the op id to specific node (initialization)
		 * 6. broadcast state to all nodes
		 * 
		 * 7. sync. send command "SET-RUNTIME"
		 * 
		 */
		
		// Send data file to nodes
		byte[] queryFile = Utils.readDataFromFile(pathToQuery);
		LOG.info("Ready to send query file of size: {} bytes", queryFile.length);
		Command code = ProtocolCommandFactory.buildCodeCommand(queryFile);
		comm.send_object_sync(code, connections, k);
		
		// Send physical query to all nodes
		Command queryDeploy = ProtocolCommandFactory.buildQueryDeployCommand(originalQuery);
		comm.send_object_sync(queryDeploy, connections, k);
		
		// Send start runtime command to all nodes to finish deployment
		Command runtime = ProtocolCommandFactory.buildStartRuntimeCommand();
		comm.send_object_sync(runtime, connections, k);
	}
	
	public void startQuery(){
		// TODO: take a look at the following two lines. Stateless is good to keep everything lean. Yet consider caching
		Set<Integer> involvedEUId = originalQuery.getIdOfEUInvolved();
		Set<Connection> connections = inf.getConnectionsTo(involvedEUId);
		
		// Send start query command
		Command start = ProtocolCommandFactory.buildStartQueryCommand();
		comm.send_object_sync(start, connections, k);
	}
	
	public void stopQuery(){
		// TODO: take a look at the following two lines. Stateless is good to keep everything lean. Yet consider caching
		Set<Integer> involvedEUId = originalQuery.getIdOfEUInvolved();
		Set<Connection> connections = inf.getConnectionsTo(involvedEUId);
		
		// Send start query command
		Command stop = ProtocolCommandFactory.buildStopQueryCommand();
		comm.send_object_sync(stop, connections, k);
	}
	
	private int computeRequiredExecutionUnits(LogicalSeepQuery lsq){
		int totalInstances = 0;
		for(Operator lo : lsq.getAllOperators()){
			int opId = lo.getOperatorId();
			if(lsq.hasSetInitialPhysicalInstances(opId)){
				totalInstances += lsq.getInitialPhysicalInstancesForLogicalOperator(opId);
			}
			else{
				// At least one is required
				totalInstances++;
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
			// FIXME: eliminate hardcoded name
			compose = baseI.getDeclaredMethod("compose", (Class<?>[])null);
			lsq = (LogicalSeepQuery) compose.invoke(baseInstance, (Object[])null);
			ucl.close();
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
		catch (IOException e) {
			e.printStackTrace();
		}
		//Finally we return the queryPlan
		return lsq;
	}
	
}
