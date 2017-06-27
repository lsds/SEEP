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
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.manet.stats.Stats;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;

//import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.*;
//import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import uk.ac.imperial.lsds.seep.acita15.facerec.FaceRecognizerHelper;
import uk.ac.imperial.lsds.seep.acita15.facerec.VideoHelper;

public class SEEPFaceRecognizer implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(SEEPFaceRecognizer.class);
	private int processed = 0;
	private FaceRecognizerHelper faceRecognizerHelper = null;
	private static final String repoDir = GLOBALS.valueFor("repoDir");	
	private Stats stats;
	private Stats utilStats;
	
	public void processData(DataTuple data) {
		long tProcessStart = System.currentTimeMillis();
		long tupleId = data.getLong("tupleId");
		byte[] value = data.getByteArray("value");
		int type = data.getInt("type");
		int x = data.getInt("x");
		int y = data.getInt("y");
		int x2 = data.getInt("x2");
		int y2 = data.getInt("y2"); //Should really rename to x1 y1 or something
		int width = x2 - x;
		int height = y2 - y; //Should really rename to x1 y1 or something
		
		int prediction = faceRecognizerHelper.recognize(value, x, y, width, height, type);
		String labelExample = faceRecognizerHelper.getLabelExample(prediction);
		
		DataTuple outputTuple = data.setValues(tupleId, value, 0, 0, type, x, y, x2, y2, labelExample);
			
		processed++;
		if (processed % 1000 == 0)
		{
			logger.info("Face recognizer "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
		}
		else
		{
			logger.debug("Face recognizer "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
			if (logger.isDebugEnabled())
			{
				recordTuple(outputTuple);
			}
		}
		
		//stats.add(System.currentTimeMillis(), data.getPayload().toString().length());
		long tProcessEnd = System.currentTimeMillis();
		logger.debug("Face recognizer processed "+width+"x"+height+" face ("+x+","+y+"),("+x2+","+y2+"),ts="+data.getLong("tupleId")+" in " + (System.currentTimeMillis() - tProcessStart) + "ms");
		stats.add(tProcessEnd, value.length);
		utilStats.addWorkDone(tProcessEnd, tProcessEnd - tProcessStart);
		api.send_highestWeight(outputTuple);
	}

	
	public void processData(List<DataTuple> arg0) {
		throw new RuntimeException("TODO"); 
	}

	private void recordTuple(DataTuple dt)
	{
		long rxts = System.currentTimeMillis();
		logger.debug("FACE_RECOGNIZER: "+api.getOperatorId()+" received tuple with id="+dt.getLong("tupleId")
				+",ts="+dt.getPayload().timestamp
				+",txts="+dt.getPayload().instrumentation_ts
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().instrumentation_ts));
	}
	
	public void setUp() {
		System.out.println("Setting up FACE_RECOGNIZER operator with id="+api.getOperatorId());
		stats = new Stats(api.getOperatorId());
		utilStats = new Stats(api.getOperatorId());
		//String trainingDir = repoDir + "/seep-system/examples/acita_demo_2015/resources/training";
		String trainingDir = "training";
		//String trainingList = "at.txt";
		String trainingList = "chokepoint.txt";
		String testImageFilename = repoDir + "/seep-system/examples/acita_demo_2015/resources/images/test/barack2.jpg";
		faceRecognizerHelper = new FaceRecognizerHelper(api.getOperatorId(), trainingDir, trainingList, testImageFilename);
		//recognizer.testSample();
		//faceRecognizerHelper.testATT();
	}

	
}
