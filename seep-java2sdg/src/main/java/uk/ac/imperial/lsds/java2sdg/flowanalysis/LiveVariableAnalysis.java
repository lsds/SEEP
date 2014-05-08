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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import soot.Unit;
import soot.Value;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLiveLocals;
import uk.ac.imperial.lsds.java2sdg.Main;
import uk.ac.imperial.lsds.java2sdg.bricks.Variable;

public class LiveVariableAnalysis {

	private final static Logger log = Logger.getLogger(Main.class.getCanonicalName());
	
	private static final LiveVariableAnalysis instance = null;
	
	private UnitGraph cfg = null;
	private SimpleLiveLocals sll = null;
	private Iterator<Unit> units = null;
	
	// map line with live variables at that point
	private SortedMap<Integer, List<Variable>> outLive = new TreeMap<Integer, List<Variable>>();
	private SortedMap<Integer, List<Variable>> inLive = new TreeMap<Integer, List<Variable>>();
	
	private LiveVariableAnalysis(){
		
	}
	
	private LiveVariableAnalysis(UnitGraph cfg){
		this.cfg = cfg;
		sll = new SimpleLiveLocals(cfg);
		units = cfg.iterator();
	}
	
	public static LiveVariableAnalysis getInstance(UnitGraph cfg){
		if(instance == null){
			return new LiveVariableAnalysis(cfg).computeLiveVariables();
		}
		else{
			return instance;
		}
	}
	
	public List<Variable> getOutLiveVariablesAtLine(int line) throws NoDataForLine{
		if(outLive.containsKey(line)){
			return outLive.get(line);
		}
		else{
			throw new NoDataForLine();
		}
	}
	
	public List<Variable> getInLiveVariablesAtLine(int line) throws NoDataForLine{
		if(inLive.containsKey(line)){
			return inLive.get(line);
		}
		else{
			throw new NoDataForLine();
		}
	}
	
	private int getLineNumberFromUnit(Unit u){
		int lineNumber = -1;
		SourceLnPosTag tag = (SourceLnPosTag)u.getTag("SourceLnPosTag");
		if (tag != null){
			lineNumber = tag.startLn();
		}
		return lineNumber;
	}
	
	private LiveVariableAnalysis computeLiveVariables(){
		units = cfg.iterator(); // Iterator from scratch
		while(units.hasNext()){
			Unit u = units.next();
			int lineNumber = getLineNumberFromUnit(u);
			List<Value> OUT = sll.getLiveLocalsAfter(u);
			List<Value> IN = sll.getLiveLocalsBefore(u);
			// In case several 
			outLive.put(lineNumber, getVars(OUT));
			inLive.put(lineNumber, getVars(IN));
		}
		return this;
	}
	
	private List<Variable> getVars(List<Value> out){
		List<Variable> vars = new ArrayList<Variable>();
		for(Value v : out){
			vars.add(Variable.var(v.getType(), v.toString()));
		}
		return vars;
	}
	
	public void printTest(){
		for(Map.Entry<Integer, List<Variable>> entry : outLive.entrySet()){
			System.out.println("L: "+entry.getKey()+" V: "+entry.getValue());
		}
	}
	
	public void printInfo(){
		for(Integer line : outLive.keySet()){
			System.out.println(line+" "+outLive.get(line));
		}
	}
	
	public void printLiveVariables(){
		log.info("-> LIVE VARIABLE ANALYSIS");
		units = cfg.iterator();
		while(units.hasNext()){
			Unit u = units.next();
			int lineNumber = -1;
			SourceLnPosTag tag = (SourceLnPosTag)u.getTag("SourceLnPosTag");
			if (tag != null){
				lineNumber = tag.startLn();
			}
			List<Value> IN = sll.getLiveLocalsBefore(u);
			List<Value> OUT = sll.getLiveLocalsAfter(u);
			System.out.println("#############");
			System.out.println("LINE: "+lineNumber);
			System.out.println(u.toString());
			System.out.println("-> IN");
			for(Value l : IN){
				System.out.println("- "+l.toString());
			}
			System.out.println("-> OUT");
			for(Value l : OUT){
				System.out.println("- "+l.toString());
			}
		}
		units = cfg.iterator();
	}
}
