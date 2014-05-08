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

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.java2sdg.Main;
import uk.ac.imperial.lsds.java2sdg.bricks.SDGAnnotation;

public class SDGBuilder {

	private final static Logger log = LoggerFactory.getLogger(Main.class.getCanonicalName());
	
	private int numPartialSDG;
	private int partialSDGid;
	private TreeSet<PartialSDGWrapper> partialSDGs;
	
	public SDGBuilder(){
		this.numPartialSDG = 0;
		this.partialSDGid = 0;
		this.partialSDGs = new TreeSet<PartialSDGWrapper>(PartialSDGWrapper.getComparator());
	}
	
	public void addPartialSDG(List<OperatorBlock> partialSDG){
		PartialSDGWrapper psw = new PartialSDGWrapper(partialSDG, partialSDGid);
		partialSDGid++;
		partialSDGs.add(psw);
		numPartialSDG++;
	}
	
	public int getNumberOfPartialSDGs(){
		return partialSDGs.size();
	}
	
	public List<OperatorBlock> synthetizeSDG(){
		PartialSDGWrapper sdg = partialSDGs.pollLast();
		while(partialSDGs.size() > 0){
			PartialSDGWrapper next = partialSDGs.pollLast();
			log.info("Merge "+next.getId()+" into: "+sdg.getId());
			sdg = merge(sdg, next);
		}
		// Although the name is partialSDG, this is the merged, final SDG we've generated
		return sdg.getPartialSDG();
	}
	
	private PartialSDGWrapper merge(PartialSDGWrapper a, PartialSDGWrapper b){
		// First we check if both are mergeable. Is the disjoint set of states of a and b smaller than b.size?
		boolean areDisjoint = Collections.disjoint(a.getSet(), b.getSet());
		if(areDisjoint){
			// probably just put into one single partialSDG, even if they are not connected. and return something
		}
		// If they are mergeable, we put all ops in a same partialSDG
		a.getPartialSDG().addAll(b.getPartialSDG()); // first we merge both partialSDGs
		// We iterate through the ob in the partialSDG to merge
		for(OperatorBlock ob : b.getPartialSDG()){
			int stateId = ob.getStateId();
			int obId = ob.getId();
			int workflowId = ob.getWorkflowId();
			// If state is in a, then we merge.
			System.out.println("Does a contains: "+stateId+"?");
			if(a.containsState(stateId)){
				log.info("Merge OB: "+obId+"-"+workflowId+" because it has stateId: "+stateId);
				// The TE inside ob is to be merged into a. Where exactly depends on the Nature of the state
				if(this.isStateHandledAsPartial(stateId, a)){
					// in this case we merge obId from workflowId into a as downstream as possible
					log.info("Merge downstream as state is handled as partial");
					a = mergeDownstream(a, obId, workflowId, stateId);
					// remove the node, as it's been already merged
					a.getPartialSDG().remove(ob);
				}
				else{
					// in this case we merge obId from workflowId into a as upstream as possible
					log.info("Merge upstream as state is not handled as partial");
					a = mergeUpstream(a, obId, workflowId, stateId);
					// remove the node here
					a.getPartialSDG().remove(ob);
				}
			}
		}
		a.addStateReferencesOf(b); // first we merge both partialSDGs, keeping track of states we merge
		return a;
	}
	
	private boolean isStateHandledAsPartial(int stateId, PartialSDGWrapper x){
		for(OperatorBlock ob : x.getPartialSDG()){
			if(ob.getStateId() == stateId && ob.getWorkflowId() == x.getPartialSDGId()){
				if(ob.getTE().getAnn() != null && (ob.getTE().getAnn().equals(SDGAnnotation.COLLECTION))){
					return true;
				}
			}
		}
		return false;
	}
	
	private PartialSDGWrapper mergeDownstream(PartialSDGWrapper a, int obId, int workflowId, int stateId){
		int psdgIdA = a.getPartialSDGId();
		// First detect the ob that will host the new TE
		List<OperatorBlock> partialSDG = a.getPartialSDG();
		ListIterator<OperatorBlock> reverseIterator = partialSDG.listIterator(partialSDG.size());
		while(reverseIterator.hasPrevious()){
			OperatorBlock toInspect = reverseIterator.previous();
			OperatorBlock toMerge = this.getOb(obId, workflowId, a.getPartialSDG());
			// We dont merge to different workflowIDs or if the target is collection and the te has local access
			if(toInspect.getWorkflowId() != psdgIdA
					|| ((partialSDG.size() > 0) && toMerge.getTE().getAnn().equals(SDGAnnotation.LOCAL)
							&& toInspect.getTE().getAnn().equals(SDGAnnotation.COLLECTION)))
				continue;
			if(toInspect.getStateId() == stateId){
				// We found the ob where to merge, so we merge it, along with its id to keep track of TE connection
				toInspect.addTE(toMerge.getTE(), toMerge.getId(), toMerge.getWorkflowId());
				// and then we change the connections
				for(Stream st : toMerge.getDownstreamOperator()){
					//First reconfigure those downstream to point to the new ob
					OperatorBlock ob = this.getOb(st.getId(), st.getWorkflowId(), a.getPartialSDG());
					ob.reconfigureUpstream(toMerge.getId(), toMerge.getWorkflowId(), toInspect.getId(), toInspect.getWorkflowId());
					// And add newly reconfigured stream
					toInspect.addDownstream(st.getId(), st.getWorkflowId(), st.getType()); // Just add the downstream conn
				}
				for(Stream st : toMerge.getUpstreamOperator()){
					//First reconfigure those upstream to point to the new ob
					OperatorBlock ob = this.getOb(st.getId(), st.getWorkflowId(), a.getPartialSDG());
					ob.reconfigureDownstream(toMerge.getId(), toMerge.getWorkflowId(), toInspect.getId(), toInspect.getWorkflowId());
					// And add newly reconfigured stream
					toInspect.addUpstream(st.getId(), st.getWorkflowId());
				}
				// We are merging one at a time. So after merging we break
				break;
			}
		}
		return a;
	}
	
	private PartialSDGWrapper mergeUpstream(PartialSDGWrapper a, int obId, int workflowId, int stateId){
		int psdgIdA = a.getPartialSDGId();
		// First detect the ob that will host the new TE
		for(OperatorBlock toInspect : a.getPartialSDG()){
			//OperatorBlock toInspect = reverseIterator.previous();
			if(toInspect.getWorkflowId() != psdgIdA)
				continue;
			if(toInspect.getStateId() == stateId){
				// We found the ob where to merge, so we merge it
				OperatorBlock toMerge = this.getOb(obId, workflowId, a.getPartialSDG());
				toInspect.addTE(toMerge.getTE(), toMerge.getId(), toMerge.getWorkflowId());
				// and then we change the connections
				for(Stream st : toMerge.getDownstreamOperator()){
					//First reconfigure those downstream to point to the new ob
					OperatorBlock ob = this.getOb(st.getId(), st.getWorkflowId(), a.getPartialSDG());
					ob.reconfigureUpstream(toMerge.getId(), toMerge.getWorkflowId(), toInspect.getId(), toInspect.getWorkflowId());
					// And add newly reconfigured stream
					toInspect.addDownstream(st.getId(), st.getWorkflowId(), st.getType()); // Just add the downstream conn
				}
				for(Stream st : toMerge.getUpstreamOperator()){
					//First reconfigure those upstream to point to the new ob
					OperatorBlock ob = this.getOb(st.getId(), st.getWorkflowId(), a.getPartialSDG());
					ob.reconfigureDownstream(toMerge.getId(), toMerge.getWorkflowId(), toInspect.getId(), toInspect.getWorkflowId());
					// And add newly reconfigured stream
					toInspect.addUpstream(st.getId(), st.getWorkflowId());
				}
				// We are merging one at a time. So after merging we break
				break;
			}
		}
		return a;
	}
	
	private OperatorBlock getOb(int obId, int workflowId, List<OperatorBlock> obs){
		for(int i = 0; i<obs.size(); i++){
			OperatorBlock ob = obs.get(i);
			if(ob.getId() == obId && ob.getWorkflowId() == workflowId){
				return ob;
			}
		}
		return null;
	}
}
