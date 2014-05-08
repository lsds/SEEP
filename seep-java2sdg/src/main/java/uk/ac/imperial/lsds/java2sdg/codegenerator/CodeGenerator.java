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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.java2sdg.Main;
import uk.ac.imperial.lsds.java2sdg.bricks.SDG.OperatorBlock;

public class CodeGenerator {

	private final static Logger log = LoggerFactory.getLogger(CodeGenerator.class.getCanonicalName());
	
	public static List<OperatorBlock> assemble(List<OperatorBlock> ops){
		
		List<OperatorBlock> assembled = CodeGenerator.assembleTE(ops);
		
		return assembled;
	}
	
	private static List<OperatorBlock> assembleTE(List<OperatorBlock> ops){
		for(OperatorBlock op : ops){
			// Check if it is multi-TE
			String builtCode = null;
			if(op.getTEs().size() > 1){
				builtCode = SeepOperatorInternalCodeTemplate.getCodeForMultiOp(op.getTEs());
			}
			// In case it is single te
			else{
				builtCode = SeepOperatorInternalCodeTemplate.getCodeForSingleOp(op.getTE());
			}
			try {
				if(builtCode == null){
					System.out.println("ERROR HERE");
				}
				op.setCode(builtCode);
			} 
			catch (Exception e) {
				e.printStackTrace();
				log.error("Invalid code assigment: "+e.getMessage());
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
