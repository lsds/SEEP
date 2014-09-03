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

import org.opencv.android.Utils;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.android_seep_master.MainActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class Source implements StatelessOperator  {
	Logger LOG = LoggerFactory.getLogger(Source.class);
	private static final long serialVersionUID = 1L;

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
		Bitmap bitmap = null;
		Mat mGray = new Mat();
		int i = 1;
		Handler myHandler = MainActivity.getImageViewHandler();

		@Override
		public void run() {
			while(MainActivity.isSystemRunning){	
				bitmap = getFrame(i);	
				Message msg = myHandler.obtainMessage();
				msg.obj = bitmap;
				msg.what = 1;
				myHandler.sendMessage(msg);

				Utils.bitmapToMat(bitmap, mGray);
				Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_BGR2GRAY);

				if(mGray != null){
					byte[] bytes = new byte[(safeLongToInt(mGray.total())) * mGray.channels()];		
					mGray.get(0, 0, bytes);

					DataTuple output = data.newTuple(i, 
							bytes, 
							mGray.rows(), 
							mGray.cols(), 
							mGray.type(), 
							"", 
							System.currentTimeMillis(),
							0,
							0,
							0,
							0);
					api.send(output);
					sendTime = System.currentTimeMillis();
					LOG.info(">>>Source sent ["+i+"] at "+sendTime);
				}
				
				i++;

				if (i>164)
					i = 1;
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
					// System.gc();
				
			}

		}
	}

	public void processData(DataTuple dt) {	
		sendOutputTupleThread.run();
	}

	public void setCallbackOp(Operator op){
		this.api.setCallbackObject(op);
	}

	public int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	private Bitmap getFrame(int i) {
		try {

			String filename = "/sdcard/frames/scene00";
			if (i<10){
				filename = filename + "00" + i + ".jpg";
			} else if (i<100){
				filename = filename + "0" + i + ".jpg";
			} else
				filename = filename + i + ".jpg";

			return BitmapFactory.decodeFile(filename);

		} catch (Exception e) { 
			return null; 
		}
	}

	public void processData(List<DataTuple> arg0) {
		// TODO Auto-generated method stub

	}

}
