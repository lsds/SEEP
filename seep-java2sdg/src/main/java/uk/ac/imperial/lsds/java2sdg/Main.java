/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.java2sdg;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

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
		
		/** Parse input parameters **/
		
		// Define options
		org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
		options.addOption("h", "help", false, "print this message");
		options.addOption("t", "target", true, "desired target. dot for DOT file or seepjar for SEEP runnable jar");
		options.addOption("o", "output", true, "desired output file name");
		options.addOption("i", "input", true, "the name of the input program");
		options.addOption("cp", "classpath", true, "the path to additional libraries and code used by the input program");
		
		// generate helper
		HelpFormatter formatter = new HelpFormatter();
		
		// Parse arguments
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} 
		catch (ParseException e) {
			// TODO Auto-generated catch block
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
		}
		
		// Get values
		String className = null;
		String pathToSeepJar = "../seep-system/target/seep-system-0.0.1-SNAPSHOT.jar";
		String pathToDriverFile = null;
		String outputTarget = null;
		String outputFileName = null;
		if(cmd.hasOption("i")){
			className = cmd.getOptionValue("i");
			if(className == null){
				formatter.printHelp( "java2sdg", options );
				System.exit(0);
			}
		}
		else{
			formatter.printHelp( "java2sdg", options );
			System.exit(0);
		}
		if(cmd.hasOption("cp")){
			pathToDriverFile = cmd.getOptionValue("cp");
			if(pathToDriverFile == null){
				System.err.println("cp parameter cannot be empty. Please specify the path to your classpath");
				System.exit(0);
			}
		}
		else{
			pathToDriverFile = ".";
		}
		if(cmd.hasOption("o")){
			outputFileName = cmd.getOptionValue("o");
			if(outputFileName == null){
				System.err.println("o parameter cannot be empty. Please specify a name for the output file");
				System.exit(0);
			}
		}
		else{
			formatter.printHelp( "java2sdg", options );
			System.exit(0);
		}
		if(cmd.hasOption("t")){
			outputTarget = cmd.getOptionValue("t");
			if(outputTarget == null){
				System.err.println("t parameter cannot be empty. Please specify a target, dot/seepjar");
				System.exit(0);
			}
			if(!(outputTarget.equals("dot") || outputTarget.equals("seepjar"))){
				System.err.println("Invalid target option");
				formatter.printHelp( "java2sdg", options );
				System.exit(0);
			}
		}
		else{
			outputTarget = "dot";
		}
		
		/** Set up SOOT for compilation and java manipulation **/
		
		// Get java.home to access rt.jar, required by soot
		String javaHome = System.getProperty("java.home");
		String sootClassPath = javaHome+"/lib/rt.jar:"+pathToSeepJar+":./";
		String pathToSourceCode = pathToDriverFile+"/"+className;
		
		/** Parse input program source code. This stage performs operations at high level only **/
		
		// Parse original source code
		log.info("Parsing source code...");
		SourceCodeHandler sch = SourceCodeHandler.getInstance(pathToSourceCode);
		log.info("Parsing source code...OK");
		
		/** Initialise soot and load input program **/
		
		// With the class loaded, we can then get the SootClass wrapper to work with
		log.info("-> Setting soot classpath: "+sootClassPath);
		Scene.v().setSootClassPath(sootClassPath);
		Options.v().setPhaseOption("jb", "preserve-source-annotations");
		log.info("-> Loading class...");
		SootClass c = null;
		try{
			System.out.println();
			c = Scene.v().loadClassAndSupport(className);
			c.setApplicationClass();
		}
		catch(CompilationDeathException cde){
			System.out.println();
			log.severe(cde.getMessage());
			System.exit(1);
		}
		log.info("-> Loading class...OK");
		
		/** Extract fields and workflows **/
		
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
		
		/** Build partialSDGs, one per workflow **/
		
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
		
		/** Build SDG from partialSDGs **/
		
		log.info("Building SDG from "+sdgBuilder.getNumberOfPartialSDGs()+" partialSDGs...");
		List<OperatorBlock> sdg = sdgBuilder.synthetizeSDG();
		for(OperatorBlock ob : sdg){
			System.out.println(ob);
		}
		log.info("Building SDG from partialSDGs...OK");
		
		/** Ouput SDG in a given format **/
		
		// Output
		if(outputTarget.equals("dot")){ // dot output
			// Export SDG to dot
			log.info("Exporting SDG to DOT file...");
			DOTExporter exporter = DOTExporter.getInstance();
			exporter.export(sdg, outputFileName);
			log.info("Exporting SDG to DOT file...OK");
		}
		else if(outputTarget.equals("seepjar")){
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
}
