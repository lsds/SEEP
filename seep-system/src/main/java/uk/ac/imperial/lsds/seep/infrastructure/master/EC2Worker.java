/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EC2Worker implements Runnable{

	private Infrastructure inf = null;
	
	public EC2Worker(Infrastructure inf){
		this.inf = inf;
	}
	
	@Override
	public void run() {
		InputStreamReader isr = null;
		BufferedReader br = null;
		String command = "scripts/amazonEC2NewMachine";
		Process c = null;
		//Gather required metrics
		try {
			c = (Runtime.getRuntime().exec(command));
		
		isr = new InputStreamReader(c.getInputStream());
		br = new BufferedReader(isr);
		String line = br.readLine();
		
		c.destroy();
		} 
		catch (IOException e) {
			
			e.printStackTrace();
		}
		// instantiate a new amazon machine
		// start new amazon machine and get address
		// wait until available
		// upload necessary files
		// ssh and execute system secondary
		// return
	}

}
