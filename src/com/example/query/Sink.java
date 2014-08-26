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
package com.example.query;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.NodeManagerCommunication;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.DistributedApi;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Message;

import com.example.android_seep.MainActivity;
public class Sink implements StatelessOperator {
	Logger LOG = LoggerFactory.getLogger(Sink.class);	
	DistributedApi api = new DistributedApi();

	public void setUp() {
		LOG.info(">>>>>>>>>>>>>>>>>>>Sink set up");
	}

	// time control variables
	long currentTime;

	public void processData(DataTuple dt) {
		int result = dt.getInt("value1");

		currentTime = System.currentTimeMillis();
		long pastTimeMillis = currentTime - Source.sendTime;

		LOG.info(">>>Sink receive: "+result+ " ("+pastTimeMillis+"ms)");

	}

	public void processData(List<DataTuple> arg0) {
	}

	public void setCallbackOp(Operator op){
		this.api.setCallbackObject(op);
	}
}
