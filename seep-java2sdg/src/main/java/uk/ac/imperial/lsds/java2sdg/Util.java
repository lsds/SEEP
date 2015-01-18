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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import uk.ac.imperial.lsds.java2sdg.bricks.InternalStateRepr;

public class Util {
	
	private final static Logger log = Logger.getLogger(Main.class.getCanonicalName());
	
	private Util(){
		
	}
	
	public static Map<String, InternalStateRepr> extractStateInformation(Iterator<SootField> fieldsIterator){
		int seId = 0;
		Map<String, InternalStateRepr> stateElements = new HashMap<String, InternalStateRepr>();
		while(fieldsIterator.hasNext()){
			SootField field = fieldsIterator.next();
			Type fieldType = field.getType();
			SootClass sc = null;
			try{
				sc = Scene.v().loadClassAndSupport(fieldType.toString());
			}
			catch(RuntimeException re){
				log.warning("Field: "+fieldType.toString()+" is not a valid class");
				continue;
			}
			Tag annotationTag = field.getTag("VisibilityAnnotationTag");
			if(annotationTag != null){
				String rawAnnotationData = annotationTag.toString();
				InternalStateRepr stateRepr = null;
				if(rawAnnotationData.contains("Partitioned")){
					stateRepr = new InternalStateRepr(sc, InternalStateRepr.StateLabel.PARTITIONED, seId);
					seId++;
				}
				else if(rawAnnotationData.contains("Partial")){
					stateRepr = new InternalStateRepr(sc, InternalStateRepr.StateLabel.PARTIAL, seId);
					seId++;
				}
				if(stateRepr != null){
					stateElements.put(field.getName(), stateRepr);
				}
			}
		}
		return stateElements;
	}
	
	private static List<String> extractWorkflowsFromMainMethod(SootMethod sm, SootClass c){
		List<String> workflowsNames = new ArrayList<String>();
		UnitGraph cfg = Util.getCFGForMethod(sm.getName(), c);
		Iterator<Unit> units = cfg.iterator();
		while(units.hasNext()){
			Unit u = units.next();
			Iterator<ValueBox> iValueBox = u.getUseBoxes().iterator();
			while(iValueBox.hasNext()){
				ValueBox valueBox = iValueBox.next();
				Value v = valueBox.getValue();
				if(v instanceof InvokeExpr){
					InvokeExpr m = (InvokeExpr)v;
					SootMethod method = m.getMethod();
					String methodName = method.getName();
					workflowsNames.add(methodName);
				}
			}
		}
		return workflowsNames;
	}
	
	public static List<String> extractWorkflows(Iterator<SootMethod> methods, SootClass c){
		List<String> workflows = null;
		// First we detect the main program to analyze it
		while(methods.hasNext()){
			SootMethod sm = methods.next();
			if(sm.getName().equals("main")){
				System.out.println("detected main");
				workflows = Util.extractWorkflowsFromMainMethod(sm, c);
			}
		}
		return workflows;
	}
	
	public static UnitGraph getCFGForMethod(String methodName, SootClass c){
		SootMethod m = c.getMethodByName(methodName);
		Body b = m.retrieveActiveBody();
		// Build CFG
		UnitGraph cfg = new ExceptionalUnitGraph(b);
		return cfg;
	}
	
	public static boolean isFieldState(String rawData){
		if(rawData == null) return false;
		String[] tokens = rawData.split(" ");
		int accessIdx = -1;
		for(int i = 0; i<tokens.length; i++){
			if(tokens[i].equals("type:")){
				accessIdx = i+1;
				break;
			}
		}
		if(accessIdx != -1){
			String annotation = tokens[accessIdx];
			if(annotation.contains("OperatorState")){
				return true;
			}
		}
		return false;
	}
}
