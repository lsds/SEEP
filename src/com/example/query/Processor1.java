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
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Processor1 implements StatelessOperator  {
	Logger LOG = LoggerFactory.getLogger(Processor1.class);
	private static final long serialVersionUID = 1L;
	private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

	public static long sendTime;

	private static float x,y,z;
	private Mat                    mGray;
	private float                  mRelativeFaceSize   = 0.2f;
	private int                    mAbsoluteFaceSize   = 0;

	DistributedApi api = new DistributedApi();
	Handler myHandler = null;

	public void setUp() {		
		LOG.info(">>>>>>>>>>>>>>>>>>>>Processor1 set up");	

	}


	public void processData(DataTuple data) {		
		int index = data.getInt("value0");
		byte[] bytes = (byte[])data.getValue("value1");

		int rows = data.getInt("value2");
		int cols = data.getInt("value3");
		int type = data.getInt("value4");
		String name = data.getString("value5");

		long timeStamp = data.getLong("value6");
		
		if(bytes!=null){
			Mat mGray = new Mat(rows,cols,type);
			mGray.put(0, 0, bytes);

			//LOG.info(">>>Processor1 receive: "+mGray.toString());

			if (mAbsoluteFaceSize == 0) {
				int height = mGray.rows();
				if (Math.round(height * mRelativeFaceSize) > 0) {
					mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
				}
			}

			MatOfRect faces = new MatOfRect();

			if (MainActivity.mJavaDetector != null)
				MainActivity.mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
						new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());


			Rect[] facesArray = faces.toArray();
			
			if ((facesArray.length>0)){
				Mat m=new Mat();
				m=mGray.submat(facesArray[0]);
				byte[] bytes2 = new byte[(safeLongToInt(m.total())) * m.channels()];		
				m.get(0, 0, bytes2);

				DataTuple output = data.setValues(index,
						bytes2, 
						m.rows(), 
						m.cols(), 
						m.type(), 
						"", 
						timeStamp,
						facesArray[0].x,
						facesArray[0].y,
						facesArray[0].width,
						facesArray[0].height);
				api.sendLowestCost(output);
				//LOG.info(">>>Processor1 sent: "+m.toString());
				

			} else {
				//LOG.info(">>>Processor1 detects no face. :(");
				DataTuple output = data.setValues(index,null, 0, 0, 0, "", 0, 0, 0, 0);
			}

		}
	}


	public int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}



	public void processData(List<DataTuple> arg0) {
		// TODO Auto-generated method stub

	}

	public void setCallbackOp(Operator op){
		this.api.setCallbackObject(op);
	}
}
