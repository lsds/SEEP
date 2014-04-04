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

import java.util.List;

import uk.ac.imperial.lsds.java2sdg.bricks.TaskElement;
import uk.ac.imperial.lsds.java2sdg.bricks.SDG.OperatorBlock;
import uk.ac.imperial.lsds.java2sdg.bricks.SDG.SendType;
import uk.ac.imperial.lsds.java2sdg.bricks.SDG.Stream;

public class CodeGenerator {

	public static List<OperatorBlock> assemble(List<OperatorBlock> ops){
		
		List<OperatorBlock> assembledCom = CodeGenerator.addConnectivityInformation(ops);
		List<OperatorBlock> assembled = CodeGenerator.assembleMultiTEOperators(assembledCom);
		
		return assembled;
	}
	
	private static List<OperatorBlock> addConnectivityInformation(List<OperatorBlock> ops){
		for(OperatorBlock op : ops){
			// We first select a TE
			for(TaskElement te : op.getTEs()){
				// We then select the sendType required to send to this TE
				SendType demandedSendType = SendType.getSendType(te.getAnn(), te.getOpType());
				// We assign such sendType to the upstreamTypes that connect to this TE in particular
				for(Stream up : op.getUpstreamOperator()){
					
				}
			}
		}
		return ops;
	}
	
	private static List<OperatorBlock> assembleMultiTEOperators(List<OperatorBlock> ops){
		
		return ops;
	}
	
	private static OperatorBlock getOb(int obId, int workflowId, List<OperatorBlock> obs){
		for(int i = 0; i<obs.size(); i++){
			OperatorBlock ob = obs.get(i);
			if(ob.getId() == obId && ob.getWorkflowId() == workflowId){
				return ob;
			}
		}
		return null;
	}
	
}
