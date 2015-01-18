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
package uk.ac.imperial.lsds.java2sdg.codegenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.imperial.lsds.java2sdg.bricks.TaskElement;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElementNature;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElement.TaskElementBuilder;
import uk.ac.imperial.lsds.java2sdg.flowanalysis.LiveVariableAnalysis;
import uk.ac.imperial.lsds.java2sdg.flowanalysis.TEBoundaryAnalysis;
import uk.ac.imperial.lsds.java2sdg.input.SourceCodeHandler;

public class SDGAssembler {	
	private SDGAssembler(){
		
	}
	
	public static Set<TaskElement> getSDG(TEBoundaryAnalysis oba, LiveVariableAnalysis lva, SourceCodeHandler sch){
		Set<TaskElement> sdg = new HashSet<TaskElement>();
//		int numberOperators = oba.getNumberOperators();
//		for(int i = 0; i<numberOperators; i++){
//			LinkedHashSet<Integer> lines = oba.getCodeForOperator(i);
//			int firstLine = 0;
//			int lastLine = 0;
//			List<String> opCode = sch.getChunkOfCode(firstLine, lastLine);
//			List<String> varsToStream = null;
//			try {
//				varsToStream = lva.getOutLiveVariablesAtLine(lastLine);
//			} 
//			catch (NoDataForLine e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			TaskElementBuilder obb = oba.getOperatorBlockBuilder(i);
//			obb.varsToStream(varsToStream);
//			String code = buildCode(opCode);
//			obb.code(code);
//			
////			OperatorBlock ob = new OperatorBlock(i);
////			ob.setOpName(Integer.valueOf(i).toString());
//			//opType
//			//localvars
//			//streamvars
//			//conntype
//			//code
//		}
		
		return sdg;
	}
	
	private static String buildCode(List<String> lines){
		StringBuilder b = new StringBuilder();
		for(String s : lines){
			b.append(s);
		}
		return b.toString();
	}
	
	private static String getCodeToLocalVars(List<String> localVars){
		StringBuffer code = new StringBuffer();
		for(int i = 0; i<localVars.size(); i++){
			String stmt = "Integer "+localVars.get(i)+" = $1.getInt("+"\""+localVars.get(i)+"\""+");\n";
			String stmt_unbox = "int "+localVars.get(i)+" = "+localVars.get(i)+".intValue();";
			code.append("System.out.println(\"h0\");");
			code.append("System.out.println($1);");
			code.append("System.out.println("+"\""+localVars.get(i)+"\""+");");
			code.append(stmt);
			code.append(stmt_unbox);
		}
		return code.toString();
	}
	
	private static String getCodeToSend(List<String> varsToStream){
		StringBuffer vars = new StringBuffer();
		for(int i = 0; i<varsToStream.size(); i++){
			if(i == (varsToStream.size()-1))
				vars.append("new Integer("+varsToStream.get(i)+")");
			else
				vars.append("new Integer("+varsToStream.get(i)+"), ");
		}
		// Note that $1 is the tuple we receive -> data
		String code = "" +
				"DataTuple output = $1.setValues(new Object[] {"+vars.toString()+"});\n" +
				"api.send(output);\n" +
		"";
		return code;
	}
	
	private static String getCodeToSend_Source(List<String> varsToStream){
		StringBuffer vars = new StringBuffer();
		for(int i = 0; i<varsToStream.size(); i++){
			//FIXME: assuming always integer for debugging
			if(i == (varsToStream.size()-1))
				vars.append("new Integer("+varsToStream.get(i)+")");
			else
				vars.append("new Integer("+varsToStream.get(i)+"), ");
		}
		// Note that $1 is the tuple we receive -> data
		String code = "" +
				"DataTuple output = tuple.newTuple(new Object[] {"+vars.toString()+"});\n" +
				"api.send(output);\n" +
		"";
		return code;
	}

//	/** Mainly for debugging **/
//	public static Set<TaskElement> getFakeLinearPipelineOfStatelessOperators(int numOfOperators){
//		ArrayList<TaskElementBuilder> linearPipeline = new ArrayList<TaskElementBuilder>();
//		TaskElementBuilder source = createFakeSourceOperatorBlock();
//		linearPipeline.add(source);
//		for(int i = 0; i<numOfOperators; i++){
//			TaskElementBuilder op = createFakeStatelessOperator(i);
//			linearPipeline.add(op);
//		}
//		TaskElementBuilder sink = createFakeSinkOperatorBlock();
//		linearPipeline.add(sink);
//		
//		//Connect operators in the pipeline
//		TaskElementBuilder previous = null;
//		for(TaskElementBuilder ob : linearPipeline){
//			if(previous != null){
//				previous.addConnection(ob.getOpName(), DataShipmentMode.ONE_AT_A_TIME);
//			}
//			previous = ob;
//		}
//		HashSet<TaskElement> sdg = new HashSet<TaskElement>();
//		ArrayList<TaskElement> ops = new ArrayList<TaskElement>();
//		for(TaskElementBuilder obb : linearPipeline){
//			ops.add(obb.build());
//		}
//		sdg.addAll(ops);
//		return sdg;
//	}
	
	
//	private static TaskElementBuilder createFakeStatelessOperator(int id){
//		String opName = "op_"+(new Integer(id).toString());
//		ArrayList<String> localVars = new ArrayList<String>();
//		localVars.add("value");
//		ArrayList<String> varsToStream = new ArrayList<String>();
//		varsToStream.add("value");
//		HashMap<String, DataShipmentMode> connectionsType = new HashMap<String, DataShipmentMode>();
//		String code = generateFakeOperatorCode(localVars, varsToStream);
//		TaskElementNature ten = TaskElementNature.getStatelessTaskElement();
//		TaskElementBuilder src = new TaskElementBuilder(id, opName).opType(ten)
//										.localVars(localVars).varsToStream(varsToStream).connectionsType(connectionsType).code(code);
//		
//		return src;
//	}
	
//	private static TaskElementBuilder createFakeSourceOperatorBlock(){
//		int id = 0;
//		String opName = "src";
//		ArrayList<String> localVars = new ArrayList<String>();
//		localVars.add("value");
//		ArrayList<String> varsToStream = new ArrayList<String>();
//		varsToStream.add("value");
//		HashMap<String, DataShipmentMode> connectionsType = new HashMap<String, DataShipmentMode>();
//		String code = generateFakeSourceCode(varsToStream);
//		System.out.println("ATTENTION SOURCE CODE: ");
//		System.out.println(code);
//		TaskElementNature ten = TaskElementNature.getStatelessSource();
//		TaskElementBuilder src = new TaskElementBuilder(id, opName).opType(ten)
//										.localVars(localVars).varsToStream(varsToStream).connectionsType(connectionsType).code(code);
//		return src;
//	}
	
//	private static TaskElementBuilder createFakeSinkOperatorBlock(){
//		int id = -1;
//		String opName = "snk";
//		ArrayList<String> localVars = new ArrayList<String>();
//		localVars.add("value");
//		ArrayList<String> varsToStream = null;
//		HashMap<String, DataShipmentMode> connectionsType = null;
//		String code = generateFakeSinkCode(localVars);
//		TaskElementNature ten = TaskElementNature.getStatelessSink();
//		TaskElementBuilder src = new TaskElementBuilder(id, opName).opType(ten)
//										.localVars(localVars).varsToStream(varsToStream).connectionsType(connectionsType).code(code);
//		return src;
//	}
	
	private static String generateFakeOperatorCode(ArrayList<String> localVars, ArrayList<String> varsToStream){
		StringBuffer code = new StringBuffer();
		String localVarsCode = getCodeToLocalVars(localVars);
		String fakeCode = "System.out.println(\"h1\");\n value++;\n System.out.println(\"h2\");\n";
		String codeToSend = getCodeToSend(varsToStream);
		code.append("{"); // open block
		code.append(localVarsCode);
		code.append(fakeCode);
		code.append(codeToSend);
		code.append("}"); // close block
		System.out.println("OP CODE: "+code.toString());
		return code.toString();
	}
	
	private static String generateFakeSinkCode(ArrayList<String> localVars){
		StringBuffer code = new StringBuffer();
		String localVarsCode = getCodeToLocalVars(localVars);
		String fakeCode = "System.out.println("+"\""+localVars.get(0)+"\""+");";
		code.append("{"); // open block
		code.append(localVarsCode);
		code.append(fakeCode);
		code.append("}"); // close block
		return code.toString();
	}
	
	private static String generateFakeSourceCode(ArrayList<String> varsToStream){
		StringBuilder code = new StringBuilder();
		String header = "{" + // open block
				"int value = 0;\n" +
				"Map mapper = api.getDataMapper();\n" +
				"DataTuple tuple = new DataTuple(mapper, new TuplePayload());\n"+
				"boolean goOn = true;\n" +
				"while(goOn){\n" +
					"value++;\n";
		String codeToSend = getCodeToSend_Source(varsToStream);
		String footer = "" +
					"System.out.println(\"sent!!\");"+
					"try {\n" +
						"Thread.sleep(1000L);\n" +
					"}\n" +
					"catch (InterruptedException e) {\n"+
						"e.printStackTrace();\n"+
					"}\n"+
				"}\n"+
				"}"; // close block
		code.append(header);
		code.append(codeToSend);
		code.append(footer);
		return code.toString();
	}
}
