package uk.ac.imperial.lsds.java2sdg;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import soot.CompilationDeathException;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.options.Options;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import uk.ac.imperial.lsds.java2sdg.bricks.InternalStateRepr;
import uk.ac.imperial.lsds.java2sdg.bricks.SDG.OperatorBlock;
import uk.ac.imperial.lsds.java2sdg.bricks.SDG.PartialSDGBuilder;
import uk.ac.imperial.lsds.java2sdg.bricks.SDG.SDGBuilder;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElement.TaskElementBuilder;
import uk.ac.imperial.lsds.java2sdg.flowanalysis.LiveVariableAnalysis;
import uk.ac.imperial.lsds.java2sdg.flowanalysis.TEBoundaryAnalysis;
import uk.ac.imperial.lsds.java2sdg.input.SourceCodeHandler;
import uk.ac.imperial.lsds.java2sdg.output.DOTExporter;

public class Main {

	private final static Logger log = Logger.getLogger(Main.class.getCanonicalName());
	
	public static void main(String args[]){
		
		// Parsing input parameters
		String pathToDriverFile = null;
		String pathToSeepJar = null;
		String className = null;
		if(args.length < 3){
			System.out.println("USAGE: java2sdg <pathToClass> <pathToSEEP.jar> <ClassName>");
			System.exit(0);
		}
		else{
			pathToDriverFile = args[0];
			pathToSeepJar = args[1];
			className = args[2];
		}
		
		// Get java.home to access rt.jar, required by soot
		String javaHome = System.getProperty("java.home");
//		String sootClassPath = javaHome+"/lib/rt.jar:"+pathToDriverFile+":"+pathToSeepJar;
		String sootClassPath = javaHome+"/lib/rt.jar:"+pathToSeepJar+":./";
		String pathToSourceCode = pathToDriverFile+"/"+className;
		
		// Parse original source code
		log.info("Parsing source code...");
		SourceCodeHandler sch = SourceCodeHandler.getInstance(pathToSourceCode);
		log.info("Parsing source code...OK");
		
		// With the class loaded, we can then get the SootClass wrapper to work with
		log.info("-> Setting soot classpath: "+sootClassPath);
		Scene.v().setSootClassPath(sootClassPath);
		Options.v().setPhaseOption("jb", "preserve-source-annotations");
		log.info("-> Loading class...");
		SootClass c = null;
		try{
			System.out.println();
			System.out.println();
			System.out.println();
//			className = "UT3.java";
			c = Scene.v().loadClassAndSupport(className);
			c.setApplicationClass();
		}
		catch(CompilationDeathException cde){
			System.out.println();
			System.out.println();
			System.out.println();
			log.severe(cde.getMessage());
			System.exit(1);
		}
		log.info("-> Loading class...OK");
		
		PhaseOptions.v().setPhaseOption("tag.ln", "on"); // tell compiler to include line numbers
		
		// List fields and indicate which one is state
		log.info("Extracting state information...");
		Chain<SootField> fields = c.getFields();
		Iterator<SootField> fieldsIterator = fields.iterator();
		Map<String, InternalStateRepr> stateElements = Util.extractStateInformation(fieldsIterator);
		log.info("Extracting state information...OK");
		
		// List relevant methods (the ones that need to be analyzed)
		log.info("-> Extracting workflows...");
		Iterator<SootMethod> methodIterator = c.methodIterator();
		List<String> workflows = Util.extractWorkflows(methodIterator, c);
		log.info("-> Extracting workflows...OK");
		
		//TODO: Insert a module here that checks for illegal programs (lower level than only parsing source code)
		
		SDGBuilder sdgBuilder = new SDGBuilder();
		int workflowId = 0;
		// Analyse and extract a partial SDG per workflow
		for(String methodName : workflows){
			// Build CFG
			log.info("Building partialSDG for workflow: "+methodName);
			UnitGraph cfg = Util.getCFGForMethod(methodName, c); // get cfg
			// Perform live variable analysis
			LiveVariableAnalysis lva = LiveVariableAnalysis.getInstance(cfg); //compute livevariables
			// Perform TE boundary analysis
			TEBoundaryAnalysis oba = TEBoundaryAnalysis.getBoundaryAnalyzer(cfg, stateElements, sch, lva);
			List<TaskElementBuilder> sequentialTEList = oba.performTEAnalysis();
			List<OperatorBlock> partialSDG = PartialSDGBuilder.buildPartialSDG(sequentialTEList, workflowId);
			for(OperatorBlock ob : partialSDG){
				System.out.println(ob);
			}
			workflowId++;
			sdgBuilder.addPartialSDG(partialSDG);
		}
		log.info("Building SDG from "+sdgBuilder.getNumberOfPartialSDGs()+" partialSDGs...");
		List<OperatorBlock> sdg = sdgBuilder.synthetizeSDG();
		for(OperatorBlock ob : sdg){
			System.out.println(ob);
		}
		log.info("Building SDG from partialSDGs...OK");
		// Export SDG to dot
		log.info("Exporting SDG to DOT file...");
		DOTExporter exporter = DOTExporter.getInstance();
		exporter.export(sdg, "test");
		log.info("Exporting SDG to DOT file...OK");
		//Set<TaskElement> sdg = SDGAssembler.getSDG(oba, lva, sch);
//		
////		SDGAssembler sdgAssembler = new SDGAssembler();
////		Set<OperatorBlock> sdg = sdgAssembler.getFakeLinearPipelineOfStatelessOperators(1);
//		
//		QueryBuilder qBuilder = new QueryBuilder();
//		String q = qBuilder.generateQueryPlanDriver(sdg);
//		System.out.println("QueryPlan: "+q);
//		qBuilder.buildAndPackageQuery();
	}
}
