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

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import uk.ac.imperial.lsds.seep.manet.stats.Stats;

import uk.ac.imperial.lsds.seep.acita15.facerec.FaceDetectorHelper;
import uk.ac.imperial.lsds.seep.acita15.facerec.VideoHelper;

public class FaceDetector implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(FaceDetector.class);

	private FaceDetectorHelper faceDetector = null;
	
	private int processed = 0;
	private boolean recordImages = false;
	private Stats stats;
	private Stats utilStats;
	
	public void processData(DataTuple data) {
		
		long tProcessStart = System.currentTimeMillis();
		long tupleId = data.getLong("tupleId");
		//String value = data.getString("value") + "," + api.getOperatorId();
		byte[] value = data.getByteArray("value");
		int cols = data.getInt("cols");
		int rows = data.getInt("rows");
		int type = data.getInt("type");
			
		logger.debug("Received "+cols+"x"+rows+" frame of type "+type);
		IplImage img = faceDetector.getIplImage(value);
		logger.debug("Received "+img.width()+"x"+img.height()+" frame.");
		IplImage imgBW = VideoHelper.prepareBWImage(img, type);
		int[] bbox = faceDetector.detectFirstFace(imgBW, rows);
		
		DataTuple outputTuple = null;
		
		if (bbox != null)
		{
			logger.debug("Found face for "+ data.getLong("tupleId") + " at ("+bbox[0]+","+bbox[1]+"),("+bbox[2]+","+bbox[3]+")");
			outputTuple = data.setValues(tupleId, value, rows, cols, type, bbox[0], bbox[1], bbox[2], bbox[3], "");
			if (recordImages)
			{
				faceDetector.recordFaceDetection(tupleId, imgBW, bbox);
			}
		}
		else	
		{
			logger.debug("No face found for "+data.getLong("tupleId"));
			outputTuple = data.setValues(tupleId, value, rows, cols, type, 0, 0, 0, 0, "");
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
		
		long tProcessEnd = System.currentTimeMillis();
		logger.debug("Face detector processed "+cols+"x"+rows+" tuple in " + (tProcessEnd - tProcessStart) + "ms");
		stats.add(tProcessEnd, value.length);
		utilStats.addWorkDone(tProcessEnd, tProcessEnd - tProcessStart);
		//stats.add(System.currentTimeMillis(), data.getPayload().toString().length());
		api.send_highestWeight(outputTuple);
	}

	
	public void processData(List<DataTuple> arg0) {
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
		stats = new Stats(api.getOperatorId());
		utilStats = new Stats(api.getOperatorId());
		//testFaceDetection();
		faceDetector = new FaceDetectorHelper();

		recordImages = Boolean.parseBoolean(GLOBALS.valueFor("recordImages"));
	}
}
