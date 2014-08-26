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

import java.nio.ByteBuffer;
import java.util.List;


import android.graphics.Bitmap;
import android.os.Message;

import com.example.android_seep.MainActivity;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.DistributedApi;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Processor implements StatelessOperator{

	Logger LOG = LoggerFactory.getLogger(Processor.class);
	DistributedApi api = new DistributedApi();

	public void processData(DataTuple data) {

			int letter = data.getInt("value1");
			int result = letter+letter;
			LOG.info(">>>Processor receive "+letter+" at "+(System.currentTimeMillis()-Source.sendTime));

			DataTuple output = data.setValues(result);
			api.send(output);
			LOG.info(">>>Processor sent: "+result);	
		
	}

	public void processData(List<DataTuple> arg0) {
		// TODO Auto-generated method stub
	}

	public void setUp() {
		// TODO Auto-generated method stub
		LOG.info(">>>>>>>>>>>>>>>>>>>>Processor set up");

	}

	public void setCallbackOp(Operator op){
		this.api.setCallbackObject(op);
	}

}
