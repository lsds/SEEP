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
package uk.ac.imperial.lsds.seepmaster.infrastructure.master;

import uk.ac.imperial.lsds.seepworker.processingunit.Operator;

public class ConnHandler implements Runnable {

	Operator o;
	OldInfrastructure inf;
	
	public ConnHandler(Operator o, OldInfrastructure inf){
		this.o = o;
		this.inf = inf;
	}
	
	@Override
	public void run() {
		inf.init(o);
	}
}
