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
package query.operators;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.NodeManagerCommunication;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Sink implements StatelessOperator {
	Logger LOG = LoggerFactory.getLogger(Sink.class);

	private static final long serialVersionUID = 1L;
	
	public void setUp() {

	}

	// time control variables
	int c = 0;
	long init = 0;
	int sec = 0;
	
	public void processData(DataTuple dt) {
		int value2 = dt.getInt("value2");
		// TIME CONTROL
		c++;
		if((System.currentTimeMillis() - init) > 1000){
			LOG.info(">>>>>>>>>>>>>>>>>>>>From Sink: "+sec+" "+c+" ");
			c = 0;
			sec++;
			init = System.currentTimeMillis();
		}
	}
	
	public void processData(List<DataTuple> arg0) {
	}
}
