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
package uk.ac.imperial.lsds.java2sdg.bricks.SDG;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PartialSDGWrapper implements Comparator<PartialSDGWrapper>{

	private final int id;
	private final int partialSDGId;
	private Set<Integer> stateIds;
	private List<OperatorBlock> partialSDG;
	private int partialSDGDepth;
	
	private PartialSDGWrapper(){
		this.id = -1;
		this.partialSDGId = -1;
	}
	
	public PartialSDGWrapper(List<OperatorBlock> partialSDG, int id){
		this.id = id;
		this.partialSDG = partialSDG;
		this.partialSDGId = partialSDG.get(0).getWorkflowId();
		this.partialSDGDepth = partialSDG.size();
		this.stateIds = new HashSet<Integer>();
		for(OperatorBlock ob : partialSDG){
			int stateId = ob.getStateId();
			if(stateId != -1){ // We want to ignore this case
				stateIds.add(ob.getStateId());
			}
		}
	}
	
	public int getId(){
		return id;
	}
	
	public int getPartialSDGId(){
		return partialSDGId;
	}
	
	public boolean containsState(int stateId){
		return stateIds.contains(stateId);
	}
	
	public Set<Integer> getSet(){
		return stateIds;
	}
	
	public List<OperatorBlock> getPartialSDG(){
		return partialSDG;
	}
	
	public int getDepth(){
		return partialSDGDepth;
	}
	
	public void addStateReferencesOf(PartialSDGWrapper partialSDG){
		for(Integer sId : partialSDG.stateIds){
			this.stateIds.add(sId);
		}
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("ID: "+this.id+"\n");
		sb.append("Depth: "+this.partialSDGDepth+"\n");
		StringBuilder sb2 = new StringBuilder();
		sb2.append("[");
		for(Integer sid : stateIds){
			sb2.append(sid+",");
		}
		sb2.append("]");
		sb.append(sb2.toString()+"\n");	
		return sb.toString();
	}

	@Override
	public int compare(PartialSDGWrapper o1, PartialSDGWrapper o2) {
		int d1 = o1.getDepth();
		int d2 = o2.getDepth();
		if(d1 > d2)
			return 1;
		else if(d1 < d2)
			return -1;
		else
			// If they are equal, it means that there is no order enforced, but the element should anyway added
			// So we explicitly impose an order
			return -1;
	}
	
	public static Comparator<PartialSDGWrapper> getComparator(){
		return new PartialSDGWrapper();
	}
	
}
