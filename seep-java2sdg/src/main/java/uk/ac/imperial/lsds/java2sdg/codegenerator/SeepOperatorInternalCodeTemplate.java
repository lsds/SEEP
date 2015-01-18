package uk.ac.imperial.lsds.java2sdg.codegenerator;

import java.util.List;

import soot.IntType;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElement;
import uk.ac.imperial.lsds.java2sdg.bricks.Variable;
import uk.ac.imperial.lsds.java2sdg.bricks.SDG.SendType;

public class SeepOperatorInternalCodeTemplate {

	public static String getCodeForMultiOp(List<TaskElement> tes){
		StringBuilder sb = new StringBuilder();
		sb.append("{"); // open block
		sb.append(_getCodeForMultiOp(tes));
		sb.append("}"); // close block
		return sb.toString();
	}
	
	private static String _getCodeForMultiOp(List<TaskElement> tes){
		StringBuilder sb = new StringBuilder();
		// Build IF block and insert code for first TE
		TaskElement firstTE = tes.remove(0);
		String initIFBlock = getInitIFBlock();
		sb.append(initIFBlock);
		sb.append(_getCodeForSingleOp(firstTE));
		sb.append("}");
		// Once the IF block has started, we just complete it with else clauses
		int branchId = 1; // 0 is used for firstTE
		for(TaskElement te : tes){
			sb.append("else if(branchId == "+branchId+"){");
			branchId++;
			sb.append(_getCodeForSingleOp(te));
			sb.append("}");
		}
		return sb.toString();
	}
	
	private static String getInitIFBlock(){
		StringBuilder sb = new StringBuilder();
		String unbox = getUnboxCode("java.lang.Integer", "branchId");
		sb.append(unbox);
		sb.append("if(branchId == 0){");
		return sb.toString();
	}
	
	public static String getCodeForSingleOp(TaskElement te){
		StringBuilder sb = new StringBuilder();
		sb.append("{"); // open block
		sb.append(_getCodeForSingleOp(te));
		sb.append("}"); // close block
		return sb.toString();
	}
	
	private static String _getCodeForSingleOp(TaskElement te){
		// Extract and synthesize code for getting the right variables. This is constant
		List<Variable> localVars = te.getLocalVars();
		String header = getCodeToLocalVars(localVars);
		// Append TE code
		String code = te.getCode();
		// Get code to send downstream. Append branching id always.
		SendType st = te.getSendType();
		List<Variable> varsToStream = te.getVarsToStream();
		// Create var for branchId and append at the beginning of varsToStream
		Variable branchId = Variable.var(IntType.v(), "branchId");
		
		//FIXME: should not be null if there is a configured sink.
		String footer = "";
		if(varsToStream != null){
			varsToStream.add(0, branchId);
			footer = getSendCode(varsToStream, st);
		}
		
		StringBuilder sb = new StringBuilder();
//		sb.append("{"); // open block
		sb.append(header);
		sb.append(code);
		sb.append("int branchId = "+te.getSendType().getBranchingIdentifier()+";"); // declare branchid before sending
		sb.append(footer);
//		sb.append("}"); // close block
		 
		return sb.toString();
	}
	
	private static String getCodeToLocalVars(List<Variable> localVars){
		StringBuilder code = new StringBuilder();
		for(int i = 0; i<localVars.size(); i++){
			Variable v = localVars.get(i);
			String type = v.getType();
			String name = v.getName();
			String unbox = getUnboxCode(type, name);
//			String stmt = "Integer "+localVars.get(i)+" = $1.getInt("+"\""+localVars.get(i)+"\""+");\n";
//			String stmt_unbox = "int "+localVars.get(i)+" = "+localVars.get(i)+".intValue();";
//			code.append(stmt);
			code.append(unbox);
		}
		return code.toString();
	}
	
	private static String getUnboxCode(String type, String name){
		StringBuilder sb = new StringBuilder();
		System.out.println("type: "+type);
		String varType_stmt1 = null;
		String unboxVarMethodName_stmt1 = null;
		String varType_stmt2 = null;
		String unboxVarMethodName_stmt2 = null;
		System.out.println("TYPE: "+type);
		if(type.equals("java.lang.Integer")){
			varType_stmt1 = "Integer";
			varType_stmt2 = "int";
			unboxVarMethodName_stmt1 = " = $1.getInt(";
			unboxVarMethodName_stmt2 = ".intValue();";
		}
		else if(type.equals("java.lang.String")){
			varType_stmt1 = "String";
			unboxVarMethodName_stmt1 = " = $1.getString(";
		}
		else if(type.equals("java.lang.Long")){
			varType_stmt1 = "Long";
			varType_stmt2 = "long";
			unboxVarMethodName_stmt1 = " = $1.getLong(";
			unboxVarMethodName_stmt2 = ".longValue();";
		}
		else if(type.equals("java.lang.Double")){
			varType_stmt1 = "Double";
			varType_stmt2 = "double";
			unboxVarMethodName_stmt1 = " = $1.getDouble(";
			unboxVarMethodName_stmt2 = ".doubleValue();";
		}
		else if(type.equals("java.lang.Character")){
			varType_stmt1 = "Character";
			varType_stmt2 = "char";
			unboxVarMethodName_stmt1 = " = $1.getChar(";
			unboxVarMethodName_stmt2 = ".charValue();";
		}
		else if(type.equals("java.lang.Boolean")){
			varType_stmt1 = "Boolean";
			varType_stmt2 = "boolean";
			unboxVarMethodName_stmt1 = " = $1.getBoolean(";
			unboxVarMethodName_stmt2 = ".booleanValue();";
		}
		else{
			System.out.println("ERROR. getUnboxCode unknows type");
			System.exit(0);
		}
		// Build actual lines
		sb.append(varType_stmt1+" "+name+" "+unboxVarMethodName_stmt1+"\""+name+"\""+");\n");
		if(varType_stmt2 != null){ // It there is such statement 2
			sb.append(varType_stmt2+" "+name+" = "+name+""+unboxVarMethodName_stmt2);
		}
		return sb.toString();
	}
	
	private static String getSendCode(List<Variable> varsToStream, SendType st){
		String code = null;
		if(st.equals(SendType.SEND)){
			code = getCodeToSend(varsToStream);
		}
		else if(st.equals(SendType.SEND_ALL)){
			
		}
		else if(st.equals(SendType.SEND_KEY)){
			
		}
		else if(st.equals(SendType.SEND_STREAMID)){
			
		}
		else if(st.equals(SendType.SEND_STREAMID_KEY)){
			
		}
		
		return code;
	}
	
	private static String getCodeToSend(List<Variable> varsToStream){
		StringBuffer vars = new StringBuffer();
//		vars.append("int branchingId = "+);
		for(int i = 0; i<varsToStream.size(); i++){
			Variable v = varsToStream.get(i);
			String boxCode = getBoxCode(v.getType(), v.getName());
			if(i == (varsToStream.size()-1))
				vars.append(boxCode);
			else
				vars.append(boxCode+", ");
		}
		// Note that $1 is the tuple we receive -> data
		String code = "" +
				"DataTuple output = $1.setValues(new Object[] {"+vars.toString()+"});\n" +
				"api.send(output);\n" +
		"";
		return code;
	}
	
	private static String getBoxCode(String type, String name){
		String c = null;
		if(type.equals("java.lang.Integer") || type.equals("int")){
			c = "new Integer("+name+")";
		}
		else if(type.equals("java.lang.String")){
			c = "new String("+name+")";
		}
		else{
			System.out.println("ERROR. getBoxCode unknows type");
			System.exit(0);
		}
		return c;
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
}