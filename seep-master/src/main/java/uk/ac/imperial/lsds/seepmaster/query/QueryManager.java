package uk.ac.imperial.lsds.seepmaster.query;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.api.LogicalOperator;
import uk.ac.imperial.lsds.seep.api.LogicalSeepQuery;
import uk.ac.imperial.lsds.seepmaster.infrastructure.master.InfrastructureManager;

public class QueryManager {

	final private Logger LOG = LoggerFactory.getLogger(QueryManager.class);
	
	private static QueryManager qm;
	private String pathToQuery;
	private LogicalSeepQuery lsq;
	
	private InfrastructureManager inf;
	
	private int executionUnitsRequiredToStart;
	
	private QueryManager(InfrastructureManager inf){
		this.inf = inf;
	}
	
	public static QueryManager getInstance(InfrastructureManager inf){
		if(qm == null){
			return new QueryManager(inf);
		}
		else{
			return qm;
		}
	}
	
	public boolean canStartExecution(){
		return inf.executionUnitsAvailable() >= executionUnitsRequiredToStart;
	}
	
	public void loadQueryFromFile(String pathToJar, String definitionClass){
		this.pathToQuery = pathToJar;
		this.lsq = executeComposeFromQuery(pathToJar, definitionClass);
		this.executionUnitsRequiredToStart = this.computeRequiredExecutionUnits(lsq);
	}
	
	public void deployQueryToNodes(){
		
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
