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
import java.util.Vector;
import java.util.List;
import uk.ac.imperial.lsds.seep.api.Partial;
import uk.ac.imperial.lsds.seep.api.Global;
import uk.ac.imperial.lsds.seep.api.Collection;
import uk.ac.imperial.lsds.seep.api.DriverProgram;
import uk.ac.imperial.lsds.seep.api.largestateimpls.SeepMap;

public class UT3 implements DriverProgram{

	@Partial
	public SeepMap<String, Integer> counter = new SeepMap<String, Integer>();

	public void main(){
		String newword = "testupdate"; // get data somehow
		// just update word in distributed fashion
		update(newword); // call function -> implies this is an entry point
		String word = "testread";
		// just returns an accurate counting of that word
		count(word);
	}

	public void update(String key){
		int newCounter = 0;
		if(counter.containsKey(key)){
			newCounter = ((Integer)counter.get(key)) + 1;
		}
		counter.put(key, newCounter);
	}

	public int count(String key){
		 int counts = countWord( counter, key); // Make sure returns some valid value
		int c = merge(counts);
		return c;
	}

	private int countWord(SeepMap<String, Integer> counter, String key){
		if(counter.containsKey(key))
			return (Integer)counter.get(key);
		else
			return 0;
	}

	private int merge(int counts){
		return 0;
	}

	private int merge( List<Integer> counts){
		int totalCount = 0;
		for(Integer counter : counts){
			totalCount += counter;
		}
		return totalCount;
	}
}
