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

import java.util.Iterator;
import java.util.List;

import soot.tagkit.Tag;

public class StateAccessUtil {
	
	public String getReadOrWriteStateAccessType(List<Tag> rawData){
		return returnPattern(rawData, "ReadAccess", "WriteAccess");
	}
	
	public String getGlobalOrPartitionedStateAccessType(List<Tag> rawData){
		return returnPattern(rawData, "PartitionStateAccess", "GlobalStateAccess");
	}
	
	public int getPartitioningKey(List<Tag> rawData){
		Iterator<Tag> iTags = rawData.iterator();
		while(iTags.hasNext()){
			Tag t = iTags.next();
			String raw = t.toString();
			if(raw.contains("PartitionStateAccess")){
				String tokens[] = raw.split(" ");
				int accessIdx = -1;
				for(int i = 0; i<tokens.length; i++){
					if(tokens[i].equals("value:")){
						accessIdx = i + 1;
					}
				}
				String param = tokens[accessIdx].trim().toString();
				param = param.substring(0,1);
				int aux = (int)Integer.parseInt(param);
				return aux;
			}
		}
		return -1;
	}
	
//	public LinkedList<PointOfInterest> unifyStateAccesses(LinkedList<PointOfInterest> stateAccesses){
//		String stateName = null;
//		int line = -1;
//		LinkedList<PointOfInterest> toReturn = new LinkedList<PointOfInterest>();
//		
//		for(int i = 0; i < stateAccesses.size(); i++){
//			PointOfInterest current = stateAccesses.get(i);
//			if(!current.stateName.equals("FAKE")){
//				stateName = current.stateName;
//				line = current.sourceCodeLine;
//			}
//			else if(current.stateName.equals("FAKE")){
//				if(current.sourceCodeLine == line){
//					current.stateName = stateName;
//					toReturn.add(current);
//				}
//			}
//		}
//		return toReturn;
//	}
	
	private String returnPattern(List<Tag> rawData, String option1, String option2){
		Iterator<Tag> iTags = rawData.iterator();
		while(iTags.hasNext()){
			Tag t = iTags.next();
			String raw = t.toString();
			if(raw.contains(option1)){
				return option1;
			}
			else if(raw.contains(option2)){
				return option2;
			}
		}
		return null;
	}
}
