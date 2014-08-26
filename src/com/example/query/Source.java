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
import java.util.Map;
import java.util.concurrent.Callable;

import uk.ac.imperial.lsds.seep.comm.NodeManagerCommunication;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.DistributedApi;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.android_seep.MainActivity;

import android.graphics.Bitmap;
import android.os.Message;

public class Source implements StatelessOperator  {
	Logger LOG = LoggerFactory.getLogger(Source.class);

	public static long sendTime;

	sendOutTuples sendOutputTupleThread;

	DistributedApi api = new DistributedApi();

	public void setUp() {
		LOG.info(">>>>>>>>>>>>>>>>>>>>Source set up");
		sendOutputTupleThread = new sendOutTuples();
	}

	class sendOutTuples implements Runnable {

		Map<String, Integer> mapper = api.getDataMapper();
		DataTuple data = new DataTuple(mapper, new TuplePayload());
		int letter = 0;
		
		@Override
		public void run() {
			while(MainActivity.isSystemRunning){
				DataTuple output = data.newTuple(letter);
				api.send(output);
				sendTime = System.currentTimeMillis();
				LOG.info(">>>Source sent: "+letter);
				try {
					Thread.sleep(250);
				} 
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				letter++;
			}
		}
	}

	public void processData(DataTuple dt) {	
		sendOutputTupleThread.run();
	}

	public void setCallbackOp(Operator op){
		this.api.setCallbackObject(op);
	}

	public void processData(List<DataTuple> arg0) {
		// TODO Auto-generated method stub

	}
}
