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

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.graphics.Bitmap;
import android.os.Message;

import com.example.android_seep_master.MainActivity;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.DistributedApi;
import uk.ac.imperial.lsds.seep.operator.Operator;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Processor implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	Logger LOG = LoggerFactory.getLogger(Processor.class);
	Bitmap mBitmap;
	Message message;

	DistributedApi api = new DistributedApi();


	public void processData(DataTuple data) {

		int index = data.getInt("value0");
		byte[] bytes = (byte[])data.getValue("value1");

		int rows = data.getInt("value2");
		int cols = data.getInt("value3");
		int type = data.getInt("value4");
		String name = data.getString("value5");
		long timeStamp = data.getLong("value6");
		int x = data.getInt("value7");
		int y = data.getInt("value8");
		int width = data.getInt("value9");
		int height = data.getInt("value10");
		//LOG.info(">>>>>>>>>>>>>>>>>>>> Processor receive "+name+" at "+(System.currentTimeMillis()-Source.sendTime));

		if(bytes!=null){
			Mat m = new Mat(rows,cols,type);
			//	byte[] bytes = new byte[buffer.capacity()];
			//	buffer.get(bytes);
			m.put(0, 0, bytes);

			//LOG.info(">>>Processor receive: "+m.toString());


			mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(m, mBitmap);	

			if (MainActivity.fr.canPredict()){
				name=MainActivity.fr.predict(m);
			}	

			DataTuple output = data.setValues(index,null,0,0,0,name,timeStamp,x,y,width,height);
			api.send(output);
			//LOG.info(">>>Processor sent "+name);
		} else {
			DataTuple output = data.setValues(index,null,0,0,0,"",timeStamp,x,y,width,height);
			api.send(output);
		}
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
