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
package uk.ac.imperial.lsds.java2sdg.flowanalysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.tagkit.SourceLnPosTag;
import soot.tagkit.StringTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.UnitGraph;
import uk.ac.imperial.lsds.java2sdg.Main;
import uk.ac.imperial.lsds.java2sdg.bricks.InternalStateRepr;
import uk.ac.imperial.lsds.java2sdg.bricks.SDGAnnotation;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElementNature;
import uk.ac.imperial.lsds.java2sdg.bricks.Variable;
import uk.ac.imperial.lsds.java2sdg.bricks.InternalStateRepr.StateLabel;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElement.TaskElementBuilder;
import uk.ac.imperial.lsds.java2sdg.input.SourceCodeHandler;

public class TEBoundaryAnalysis {
	
	private final static Logger log = LoggerFactory.getLogger(Main.class.getCanonicalName());
	
	private StateAccessUtil util = new StateAccessUtil();
	private UnitGraph cfg = null;
	private final Map<String, InternalStateRepr> stateElements;
	private Iterator<Unit> units = null;
	private LinkedList<PointOfInterest> stateAccesses = null;
	
	private SourceCodeHandler sch;
	private LiveVariableAnalysis lva;
	
	private Map<TaskElementBuilder, LinkedHashSet<Integer>> op_code = new LinkedHashMap<TaskElementBuilder, LinkedHashSet<Integer>>();
	
	private TEBoundaryAnalysis(UnitGraph cfg, Map<String, InternalStateRepr> stateElements, 
			SourceCodeHandler sch, LiveVariableAnalysis lva){
		this.cfg = cfg;
		this.stateElements = stateElements;
		this.sch = sch;
		this.lva = lva;
		units = cfg.iterator();
		stateAccesses = new LinkedList<PointOfInterest>();
	}
	
	public static TEBoundaryAnalysis getBoundaryAnalyzer(UnitGraph cfg, Map<String, InternalStateRepr> stateElements, 
													   SourceCodeHandler sch, LiveVariableAnalysis lva){
		return new TEBoundaryAnalysis(cfg, stateElements, sch, lva);
	}
	
	/**
	 * Dataflow style analysis
	 */
	
	public List<TaskElementBuilder> performTEAnalysis(){
		// Get sequential TE
		return getTeInSequence();
	}
	
	public LinkedList<PointOfInterest> getStateAccessList(){
		return stateAccesses;
	}
	
	public TaskElementBuilder getOperatorBlockBuilder(int opId){
		for(TaskElementBuilder obb : op_code.keySet()){
			if(obb.getId() == opId){
				return obb;
			}
		}
		return null;
	}
	
	public LinkedHashSet<Integer> getCodeForOperator(int opId){
		for(TaskElementBuilder obb : op_code.keySet()){
			if(obb.getId() == opId){
				return op_code.get(obb);
			}
		}
		return null;
	}
	
	public int getNumberOperators(){
		return op_code.keySet().size();
	}
	
	private int getLineNumber(Unit u){
		int lineNumber = -1;
		SourceLnPosTag tag = (SourceLnPosTag)u.getTag("SourceLnPosTag");
		if (tag != null){
			lineNumber = tag.startLn();	// get line number
		}
		return lineNumber;
	}
	
	private Iterator<ValueBox> getValueBoxes(Unit u){
		return u.getUseBoxes().iterator();
	}

	
	private PointOfInterest analyzeLine(int originalLineNumber, List<Unit> groupOfJimpleLines){
		PointOfInterest poi = new PointOfInterest();
		poi.setSourceCodeLine(originalLineNumber); //Assign source code line
		// First check whether there is an important annotation and store it in ann
		SDGAnnotation ann = sch.getSDGAnnotationAtLine(originalLineNumber);
		poi.setAnnotation(ann);
		// Then check if there is state access
		for(Unit u : groupOfJimpleLines){
			Iterator<ValueBox> iValueBox = getValueBoxes(u);
			while(iValueBox.hasNext()){
				ValueBox valueBox = iValueBox.next();
				Value v = valueBox.getValue();
				if(v instanceof FieldRef){
					FieldRef fRef = (FieldRef)v;
					String fieldName = fRef.getField().getName(); // stateName
					if(stateElements.containsKey(fieldName)){
						int stateElementId = getStateElementId(fieldName); // stateElement id
						poi.setStateElementId(stateElementId); // the state this line refers to
						poi.setStateName(fieldName);
						// If there is a ref to partial state and no annotation, then it's local access
						InternalStateRepr isr = stateElements.get(fieldName);
						if(isr.getStateLabel().equals(StateLabel.PARTIAL) && ann == null){
							poi.setAnnotation(SDGAnnotation.LOCAL);
						}
					}
				}
				else if(v instanceof InvokeExpr){
					if(poi.getStateName() != null){
						InvokeExpr m = (InvokeExpr)v;
						SootMethod method = m.getMethod();
						String methodName = method.getName();
						SootClass aux = m.getMethod().getDeclaringClass();
						if(ann == null || !ann.equals(SDGAnnotation.GLOBAL_READ) || !ann.equals(SDGAnnotation.GLOBAL_WRITE)){
							InternalStateRepr isr = stateElements.get(poi.getStateName());
							StateLabel sl = isr.getStateLabel();
							SootClass sc = isr.getStateClass();
							if(aux.toString().equals(sc.toString())){
								// If it's partitioned state then we get the key
								if(sl.equals(StateLabel.PARTITIONED)){
									SootMethod originM = aux.getMethodByName(methodName);
									List<Tag> tags = originM.getTags();
									int partitioningKeyPosition = util.getPartitioningKey(tags);
									Value partitioningKey = m.getArg(partitioningKeyPosition);
									poi.setPartitioningKey(partitioningKey.toString());
								}
							}
						}
					}
				}
			}
		}
		return poi;
	}
	
	private boolean setsNewBound(PointOfInterest poi, int lastSAccess){
		SDGAnnotation ann = poi.getAnnotation();
		if(ann != null && 
				(ann.equals(SDGAnnotation.GLOBAL_READ)
				|| ann.equals(SDGAnnotation.GLOBAL_READ) 
				|| ann.equals(SDGAnnotation.COLLECTION))){
			log.debug("ANN bound at line: "+poi.getSourceCodeLine());
			return true;
		}
		if(poi.isStateAccess()){
			if(lastSAccess != -1){ // first TE does not count (group more lines)
				if(!(poi.getStateElementId() == lastSAccess)){
					log.debug("StateAccess bound at line: "+poi.getSourceCodeLine());
					return true;
				}
			}
		}
		return false;
	}
	
	private TaskElementBuilder processPOI(PointOfInterest poi, TaskElementBuilder buildingTE){
		SDGAnnotation ann = poi.getAnnotation();
		if(ann != null && (ann.equals(SDGAnnotation.GLOBAL_READ) || ann.equals(SDGAnnotation.GLOBAL_WRITE))){
			// Say it's global and check for state access
			buildingTE.ann(ann);
		}
		else if(ann != null && (ann.equals(SDGAnnotation.LOCAL))){
			buildingTE.ann(ann);
			TaskElementNature ten = TaskElementNature.getStatefulTaskElement(poi.getStateElementId(), poi.getStateName(), 
					poi.getPartitioningKey());
			buildingTE.opType(ten);
			return buildingTE;
		}
		else if(ann != null && (ann.equals(SDGAnnotation.COLLECTION))){
			// Say it's merge and return (we don't want these to be stateful)
			buildingTE.ann(ann);
			// ... so we make it stateless with a state reference to whatever it merges
			int stateId = poi.getStateElementId();
			System.out.println("COLLECTION WITH STATE ID: "+stateId);
			TaskElementNature ten = TaskElementNature.getStatelessTaskElementWithStateReference(stateId);
			buildingTE.opType(ten);
			return buildingTE;
		}
		// If it's state access fill stateful info
		if(poi.isStateAccess()){
			TaskElementNature ten = TaskElementNature.getStatefulTaskElement(poi.getStateElementId(), poi.getStateName(), 
														  poi.getPartitioningKey());
			buildingTE.opType(ten);
		}
		else{
			// Make sure we are initialising this state, because otherwise we just need to continue
			if(buildingTE.getOpType() == null){
				TaskElementNature ten = TaskElementNature.getStatelessTaskElement();
				buildingTE.opType(ten);
			}
		}
		return buildingTE;
	}
	
	private List<Variable> filterThis(List<Variable> vars){
		Iterator<Variable> iv = vars.iterator();
		while(iv.hasNext()){
			Variable v = iv.next();
			if(v.getName().equals("this")){
				iv.remove();
			}
		}
		return vars;
	}
	
	private List<TaskElementBuilder> getTeInSequence(){
		int teId = 0;
		int lastSAccess = -1;
		List<TaskElementBuilder> sequentialListOfTeb = new ArrayList<TaskElementBuilder>();
		TaskElementBuilder teb = new TaskElementBuilder(0, new String("TE_"+teId));
		int prevLineNumber = -1;
		int curLineNumber = -1;
		List<Unit> jimpleCodeLines = new ArrayList<Unit>();
		Unit firstUnit = units.next();
		int firstLine = getLineNumber(firstUnit);
		List<Variable> localVars = null;
		try {
			localVars = filterThis(lva.getInLiveVariablesAtLine(firstLine));
		}
		catch (NoDataForLine e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		teb.localVars(localVars);
		while(units.hasNext()){
			Unit u = units.next();
			curLineNumber = getLineNumber(u);
			if(curLineNumber != prevLineNumber && prevLineNumber != -1){
				PointOfInterest poi = analyzeLine(prevLineNumber, jimpleCodeLines);
				jimpleCodeLines.clear();
				// Check whether poi or not
				if(!poi.isPointOfInterest()){ // Not poi, we just store the line
					String originalSourceCodeLine = sch.getLineAt(poi.getSourceCodeLine());
					teb.addCodeLine(originalSourceCodeLine);
				}
				else{ // process poi
					if(setsNewBound(poi, lastSAccess)){
						// BUILD AND STORE PREVIOUS TE
						List<Variable> varsToStream = null;
						try {
							varsToStream = filterThis(lva.getInLiveVariablesAtLine(poi.getSourceCodeLine()));
						} 
						catch (NoDataForLine e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						teb.varsToStream(varsToStream);
						sequentialListOfTeb.add(teb);
						
						//PREPARE NEW TE
						teId++;
						teb = new TaskElementBuilder(teId, new String("TE_"+teId));
						String originalSourceCodeLine = sch.getLineAt(poi.getSourceCodeLine());
						teb.addCodeLine(originalSourceCodeLine); // Add the POI
						teb = processPOI(poi, teb); // PROCESS poi
						try {
							localVars = filterThis(lva.getInLiveVariablesAtLine(firstLine));
						} 
						catch (NoDataForLine e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						teb.localVars(localVars);
						
					}
					else{
						String originalSourceCodeLine = sch.getLineAt(poi.getSourceCodeLine());
						teb.addCodeLine(originalSourceCodeLine);
						teb = processPOI(poi, teb);
					}
					
					//Regardless what happened with the line -> remember to update last state access
					if(poi.isStateAccess()){
						lastSAccess = poi.getStateElementId();
					}
				}
			}
			jimpleCodeLines.add(u);
			prevLineNumber = curLineNumber;
		}
		PointOfInterest poi = analyzeLine(prevLineNumber, jimpleCodeLines);
		if(poi.isPointOfInterest() && setsNewBound(poi, lastSAccess)){
			// BUILD PREV TEB
			List<Variable> varsToStream = null;
			try {
				varsToStream = filterThis(lva.getInLiveVariablesAtLine(poi.getSourceCodeLine()));
			} 
			catch (NoDataForLine e) {
				e.printStackTrace();
			}
			teb.varsToStream(varsToStream);
			sequentialListOfTeb.add(teb);
			
			// BUILD NEW ONE
			teId++;
			teb = new TaskElementBuilder(teId, new String("TE_"+teId));
			String originalSourceCodeLine = sch.getLineAt(poi.getSourceCodeLine());
			teb.addCodeLine(originalSourceCodeLine); // Add the POI
			teb = processPOI(poi, teb); // PROCESS poi
			try {
				localVars = filterThis(lva.getInLiveVariablesAtLine(firstLine));
			} 
			catch (NoDataForLine e) {
				e.printStackTrace();
			}
			teb.localVars(localVars);
			sequentialListOfTeb.add(teb);
		}
		else{
			teb = processPOI(poi, teb);
			// We check if there is a previous TEB, so that we can tell it which vars to stream
			if(!sequentialListOfTeb.isEmpty()){
				TaskElementBuilder previous = sequentialListOfTeb.get(sequentialListOfTeb.size()-1);
				List<Variable> varsToStream = null;
				try {
					varsToStream = filterThis(lva.getInLiveVariablesAtLine(curLineNumber));
				} 
				catch (NoDataForLine e) {
					e.printStackTrace();
				}
			
				previous.varsToStream(varsToStream);
			}
			String originalSourceCodeLine = sch.getLineAt(poi.getSourceCodeLine());
			teb.addCodeLine(originalSourceCodeLine);
			sequentialListOfTeb.add(teb);
		}
		return sequentialListOfTeb;
	}
	
	public void printOpCode(){
		for(Map.Entry<TaskElementBuilder, LinkedHashSet<Integer>> opcode : op_code.entrySet()){
			System.out.println("");
			System.out.println("OP: "+opcode.getKey().getId());
			for(Integer line : opcode.getValue()){
				System.out.print(line+" ");
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	public void printOperatorBounds(){
		units = cfg.iterator(); //reset iterator
		while(units.hasNext()){
			Unit unit = units.next();
			int lineNumber = -1;
			SourceLnPosTag lineTag = (SourceLnPosTag)unit.getTag("SourceLnPosTag");
			if (lineTag != null){
				lineNumber = lineTag.startLn();
			}
			StringTag stringTag = (StringTag)unit.getTag("StringTag");
			System.out.println("L: "+lineNumber+" TE: "+stringTag.getInfo()+" # "+unit.toString());
		}
	}
	
	private int getStateElementId(String stateName){
		InternalStateRepr isr = stateElements.get(stateName);
		return isr.getSeId(); 
	}
}
