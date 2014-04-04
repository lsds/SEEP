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

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class OperatorBuilder {

	private ClassPool cp = null;

	public OperatorBuilder(){
		cp = ClassPool.getDefault();
		cp.importPackage("uk.ac.imperial.lsds.seep.api.QueryPlan");
		cp.importPackage("uk.ac.imperial.lsds.seep.api.QueryBuilder");
		cp.importPackage("uk.ac.imperial.lsds.seep.api.QueryComposer");
		cp.importPackage("uk.ac.imperial.lsds.seep.operator.Connectable");
		cp.importPackage("uk.ac.imperial.lsds.seep.operator.StatelessOperator");
		cp.importPackage("uk.ac.imperial.lsds.seep.operator.StatefulOperator");
		cp.importPackage("uk.ac.imperial.lsds.seep.infrastructure.master.Node");
		cp.importPackage("uk.ac.imperial.lsds.seep.comm.serialization.DataTuple");
		cp.importPackage("uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload");
		cp.importPackage("java.util.ArrayList");
		cp.importPackage("java.util.Map");
		cp.importPackage("java.util.HashMap");
	}
	
//	public void includeClassPath(String path){
//		try {
//			cp.insertClassPath(path);
//		} 
//		catch (NotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public CtClass getStatefulOperatorClassTemplate(String opName){
		CtClass cc = cp.makeClass(opName);
	
		return cc;	
	}
	
	public CtClass getStatelessOperatorClass_oneTupleAtATime(String opName, String code){
		CtClass cc = cp.makeClass(opName);
		
		CtClass[] implInterfaces = new CtClass[1];
		try {
			implInterfaces[0] = cp.get("uk.ac.imperial.lsds.seep.operator.StatelessOperator"); 
			//cc.setSuperclass(cp.get("uk.ac.imperial.lsds.seep.operator.Operator"));
			cc.setInterfaces(implInterfaces);
			
			//Fields
			CtField f = CtField.make("private static final long serialVersionUID = 1L;", cc);
			cc.addField(f);
			
			//Mandatory methods
			CtMethod processDataSingle = CtNewMethod.make(
	                 "public void processData(DataTuple data) {\n"+code+"\n }", cc);
			System.out.println("NEW METHOD: ");
			
			System.out.println(processDataSingle.toString());
			cc.addMethod(processDataSingle);
			
			CtMethod processDataBatch = CtNewMethod.make(
	                 "public void processData(ArrayList data) { }", cc);
			cc.addMethod(processDataBatch);
			
			CtMethod setUp = CtNewMethod.make(
	                 "public void setUp() { }", cc);
			cc.addMethod(setUp);
			
		}
		catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cc;	
	}
	
	public CtClass getStatelessOperatorClass_window(String opName, String code){
		CtClass cc = cp.makeClass(opName);
		
		CtClass[] implInterfaces = new CtClass[1];
		try {
			implInterfaces[0] = cp.get("uk.ac.imperial.lsds.seep.operator.StatelessOperator"); 
			//cc.setSuperclass(cp.get("uk.ac.imperial.lsds.seep.operator.Operator"));
			cc.setInterfaces(implInterfaces);
			
			//Fields
			CtField f = CtField.make("private static final long serialVersionUID = 1L;", cc);
			cc.addField(f);
			
			//Mandatory methods
			CtMethod processDataSingle = CtNewMethod.make(
	                 "public void processData(DataTuple data) { }", cc);
			cc.addMethod(processDataSingle);
			
			CtMethod processDataBatch = CtNewMethod.make(
	                 "public void processData(ArrayList data) {\n"+code+"\n }", cc);
			cc.addMethod(processDataBatch);
			
			CtMethod setUp = CtNewMethod.make(
	                 "public void setUp() { }", cc);
			cc.addMethod(setUp);
			
		}
		catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cp.insertClassPath(new ClassClassPath(cc.getClass()));
		return cc;	
	}
	
	
	public CtClass getBaseIClass(String code){
		CtClass cc = cp.makeClass("Base");
		CtClass[] implInterfaces = new CtClass[1];
		try{
			implInterfaces[0] = cp.get("uk.ac.imperial.lsds.seep.api.QueryComposer");
			cc.setInterfaces(implInterfaces);
			
			CtMethod compose = CtNewMethod.make(
	                 "public QueryPlan compose() {" +
	                 code +
	                 "}", cc);
			cc.addMethod(compose);
		}
		catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cc;
	}
	
}
