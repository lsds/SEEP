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

public class CodeGenerator {

	public static List<OperatorBlock> assemble(List<OperatorBlock> ops){
		
		List<OperatorBlock> assembled = CodeGenerator.assembleMultiTEOperators(ops);
		
		return assembled;
	}
	
	private static List<OperatorBlock> assembleMultiTEOperators(List<OperatorBlock> ops){
		for(OperatorBlock op : ops){
			System.out.println("---->");
			System.out.println(op);
			System.out.println("");
			System.out.println("");
			System.out.println("");
		}
		for(OperatorBlock op : ops){
			// Check if it is multi-operator
			if(op.getTEs().size() > 1){
				for(TaskElement te : op.getTEs()){
					
				}
			}
		}
		
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
