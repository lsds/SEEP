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
package uk.ac.imperial.lsds.seep.acita15.operators;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class FaceDetector implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(FaceDetector.class);
	private int processed = 0;
	
	private CascadeClassifier faceDetector = null;
	private Java2DFrameConverter frameConverter = null;
	private OpenCVFrameConverter matConverter = null;
	
	public void processData(DataTuple data) {
		
		
		long tupleId = data.getLong("tupleId");
		//String value = data.getString("value") + "," + api.getOperatorId();
		byte[] value = data.getByteArray("value");
				
		/*
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		*/


		Rect faceDetections = new Rect();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(value);
		
		try
		{
			Mat frame = matConverter.convertToMat(frameConverter.convert(ImageIO.read(bais)));
			faceDetector.detectMultiScale(frame, faceDetections);
		}
		catch(IOException e)
		{
			throw new RuntimeException("Error reading image: ", e);
		}
		int numFaces = faceDetections.limit();
		
		DataTuple outputTuple = null;
		
		if (numFaces > 0)
		{
			//Just take the first face for now
			int x = faceDetections.position(0).x();
			int y = faceDetections.position(0).x();
			int height = faceDetections.position(0).height();
			int width = faceDetections.position(0).width();
			outputTuple = data.setValues(tupleId, value, x, y, height, width);
		}
		else
		{
			outputTuple = data.setValues(tupleId, value, 0, 0, 0, 0);
		}
		
		//DataTuple outputTuple = data.setValues(tupleId, value);
		processed++;
		if (processed % 1000 == 0)
		{
			logger.info("Face detector "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
		}
		else
		{
			logger.debug("Face detector "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
			if (logger.isDebugEnabled())
			{
				recordTuple(outputTuple);
			}
		}
		
		api.send_highestWeight(outputTuple);
	}

	
	public void processData(List<DataTuple> arg0) {
		for (DataTuple data : arg0)
		{
			long tupleId = data.getLong("tupleId");
			String value = data.getString("value") + "," + api.getOperatorId();
			
			DataTuple outputTuple = data.setValues(tupleId, value);
			processed++;
			if (processed % 1000 == 0)
			{
				logger.info("Face detector "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
			}
			else
			{
				logger.debug("Face detector "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
				recordTuple(outputTuple);
			}
		}
		throw new RuntimeException("TODO"); 
	}

	private void recordTuple(DataTuple dt)
	{
		long rxts = System.currentTimeMillis();
		logger.debug("FACE_DETECTOR: "+api.getOperatorId()+" received tuple with id="+dt.getLong("tupleId")
				+",ts="+dt.getPayload().timestamp
				+",txts="+dt.getPayload().instrumentation_ts
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().instrumentation_ts));
	}
	
	public void setUp() {
		System.out.println("Setting up FACE_DETECTOR operator with id="+api.getOperatorId());
		
		try
		{
			String classifierName = Paths.get(
                FaceDetector.class.getResource("/haarcascade_frontalface_default.xml")
                        .toURI()).toString();
	        faceDetector = new CascadeClassifier(classifierName);
		}
		catch(URISyntaxException e) { throw new RuntimeException(e); }

        frameConverter = new Java2DFrameConverter();
        matConverter = new OpenCVFrameConverter.ToMat();
	}

}
