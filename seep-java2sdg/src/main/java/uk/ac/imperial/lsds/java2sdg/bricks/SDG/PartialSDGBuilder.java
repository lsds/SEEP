package uk.ac.imperial.lsds.java2sdg.bricks.SDG;

import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.lsds.java2sdg.bricks.SDGAnnotation;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElement;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElementNature;
import uk.ac.imperial.lsds.java2sdg.bricks.TaskElement.TaskElementBuilder;

public class PartialSDGBuilder {

	public static List<OperatorBlock> buildPartialSDG(List<TaskElementBuilder> sequentialTEList, int workflowId){
		// Connections per local TE
		LinkedList<OperatorBlock> partialSDG = new LinkedList<OperatorBlock>();
		int id = 0;
		int stateIdPtr = -1;
		for(TaskElementBuilder teb : sequentialTEList){
			TaskElement te = teb.build();
			OperatorBlock ob = null;
			if(te.getOpType().getNature().equals(TaskElementNature.Nature.STATEFUL_OPERATOR)){
				int stateId = teb.getOpType().getStateElementId();
				ob = OperatorBlock.makeStatefulOperator(id, workflowId, stateId);
				// If it's stateful there might be loops
				if(stateId > stateIdPtr){
					stateIdPtr = stateId; // update pointer to most downstream state
				}
				else{
					//loop. connect to whoever is required
					for(OperatorBlock looping : partialSDG){
						// If op is stateful AND its state is the same
						if(looping.getTE().getOpType().getNature().equals(TaskElementNature.Nature.STATEFUL_OPERATOR) &&
								looping.getStateId() == stateId){
							// assume there is no barrier in this conn (usually is just to update or something...)
							ob.addDownstream(looping.getId(), workflowId, StreamType.ONE_AT_A_TIME);
							looping.addUpstream(id, workflowId);
						}
					}
				}
			}
			else if(te.getOpType().getNature().equals(TaskElementNature.Nature.STATELESS_OPERATOR)){
				int stateRef = te.getOpType().getStateElementId();
				ob = OperatorBlock.makeStatelessOperator(id, workflowId, stateRef);
			}
			ob.addTE(te);
			// Perform connections with previous OB
			if(!partialSDG.isEmpty()){
				// Create a stream to connect upstream to me. For now, only annotations decide on this
				StreamType st = null;
				SDGAnnotation ann = te.getAnn();
				if(ann != null){
					if(ann.equals(SDGAnnotation.COLLECTION)){
						st = StreamType.SYNC_BARRIER;
					}
					else{
						st = StreamType.ONE_AT_A_TIME;
					}
				}
				// OperatorBlock up = partialSDG.getLast();
				// connect upstream to me
				partialSDG.getLast().addDownstream(ob.getId(), workflowId, st);
				// connect myself to upstream
				ob.addUpstream(partialSDG.getLast().getId(), workflowId);
			}
			// Finally I add myself to the graph
			partialSDG.add(ob);
			id++;
		}
		return partialSDG;
	}
}