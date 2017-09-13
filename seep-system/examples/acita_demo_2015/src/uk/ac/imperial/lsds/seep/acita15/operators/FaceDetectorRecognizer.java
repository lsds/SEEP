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

import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

import uk.ac.imperial.lsds.seep.manet.stats.Stats;
import uk.ac.imperial.lsds.seep.acita15.facerec.FaceDetectorHelper;
import uk.ac.imperial.lsds.seep.acita15.facerec.FaceRecognizerHelper;
import uk.ac.imperial.lsds.seep.acita15.facerec.VideoHelper;

public class FaceDetectorRecognizer implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(FaceDetectorRecognizer.class);
	
	private int processed = 0;
	private boolean recordImages = false;

	//Detection
	private FaceDetectorHelper faceDetectorHelper = null;

	//Recognition
	private FaceRecognizerHelper faceRecognizerHelper = null;
	private static final String repoDir = GLOBALS.valueFor("repoDir");	

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

		IplImage img = faceDetectorHelper.getIplImage(value);
		logger.debug("Received "+img.width()+"x"+img.height()+" frame.");
		IplImage imgBW = VideoHelper.prepareBWImage(img, type);
		int[] bbox = faceDetectorHelper.detectFirstFace(imgBW, rows);
		
		DataTuple outputTuple = null;
		
		if (bbox != null)
		{
			logger.debug("Found face for "+ data.getLong("tupleId") + " at ("+bbox[0]+","+bbox[1]+"),("+bbox[2]+","+bbox[3]+")");

			if (recordImages)
			{
				faceDetectorHelper.recordFaceDetection(tupleId, imgBW, bbox);
			}

			int x = bbox[0];
			int y = bbox[1];
			int x2 = bbox[2];
			int y2 = bbox[3];
			int width = x2 - x;
			int height = y2 - y;
			
			int prediction = faceRecognizerHelper.recognize(value, x, y, width, height, type);
			String labelExample = faceRecognizerHelper.getLabelExample(prediction);
			
			outputTuple = data.setValues(tupleId, value, 0, 0, type, x, y, x2, y2, labelExample);
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
			logger.info("Face detector recognizer "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
		}
		else
		{
			logger.debug("Face detector recognizer "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
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
		logger.debug("FACE_DETECTOR_RECOGNIZER: "+api.getOperatorId()+" received tuple with id="+dt.getLong("tupleId")
				+",ts="+dt.getPayload().timestamp
				+",txts="+dt.getPayload().instrumentation_ts
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().instrumentation_ts));
	}
	
	public void setUp() {
		System.out.println("Setting up FACE_DETECTOR_RECOGNIZER operator with id="+api.getOperatorId());
		stats = new Stats(api.getOperatorId());
		utilStats = new Stats(api.getOperatorId());

		faceDetectorHelper = new FaceDetectorHelper();

		recordImages = Boolean.parseBoolean(GLOBALS.valueFor("recordImages"));

		String trainingDir = "training";
		//String trainingList = "at.txt";
		String trainingList = "chokepoint.txt";
		String testImageFilename = repoDir + "/seep-system/examples/acita_demo_2015/resources/images/test/barack2.jpg";
		faceRecognizerHelper = new FaceRecognizerHelper(api.getOperatorId(), trainingDir, trainingList, testImageFilename);
	}

}
