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

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

/*
import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacpp.PointerPointer;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.apache.log4j.Logger;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_legacy.*;
*/
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.Pointer;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_legacy.*;

public class VideoSource implements StatelessOperator {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(VideoSource.class);
	
	public void setUp() {
		System.out.println("Setting up VIDEO_SOURCE operator with id="+api.getOperatorId());
	}

	public void processData(DataTuple dt) {
		Map<String, Integer> mapper = api.getDataMapper();
		DataTuple data = new DataTuple(mapper, new TuplePayload());
		
		long tupleId = 0;
		
		boolean sendIndefinitely = Boolean.parseBoolean(GLOBALS.valueFor("sendIndefinitely"));
		long numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
		//int tupleSizeChars = Integer.parseInt(GLOBALS.valueFor("tupleSizeChars"));
		boolean rateLimitSrc = Boolean.parseBoolean(GLOBALS.valueFor("rateLimitSrc"));
		long frameRate = Long.parseLong(GLOBALS.valueFor("frameRate"));
		long interFrameDelay = 1000 / frameRate;
		logger.info("Source inter-frame delay="+interFrameDelay);
		
		final long tStart = System.currentTimeMillis();
		logger.info("Source sending started at t="+tStart);
		logger.info("Source sending started at t="+tStart);
		logger.info("Source sending started at t="+tStart);
		
		//String testFramesDir = GLOBALS.valueFor("testFramesDir");
		//String imgFileExt = GLOBALS.valueFor("imgFileExt");
		String testFramesDir = "images";
		String imgFileExt = "jpg";
		
		byte[][] testFrames = loadImages(testFramesDir, imgFileExt);
		int currentFrame = 0;
		
		while(sendIndefinitely || tupleId < numTuples){
			
			DataTuple output = data.newTuple(tupleId, testFrames[currentFrame], 0, 0, 0, 0);
			output.getPayload().timestamp = tupleId;
			if (tupleId % 1000 == 0)
			{
				logger.info("Source sending tuple id="+tupleId+",t="+output.getPayload().instrumentation_ts);
			}
			else
			{
				logger.debug("Source sending tuple id="+tupleId+",t="+output.getPayload().instrumentation_ts);
			}
			api.send_highestWeight(output);
			
			tupleId++;
			
			long tNext = tStart + (tupleId * interFrameDelay);
			long tNow = System.currentTimeMillis();
			if (tNext > tNow && rateLimitSrc)
			{
				logger.debug("Source wait to send next frame="+(tNext-tNow));
				try {
					Thread.sleep(tNext - tNow);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
			
			currentFrame = (currentFrame + 1) % testFrames.length;
		}
		System.exit(0);
	}
	
	public void processData(List<DataTuple> arg0) {
		// TODO Auto-generated method stub
		
	}

	private byte[][] loadImages(String imgDirname, final String imgSuffix)
	{
		File dir = new File(imgDirname);
		File [] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(imgSuffix);
			}
		});	
		
		byte[][] frameArr = new byte[files.length][];

		int i = 0;
		for (File imgFile : files)
		{
			//ByteArrayOutputStream
			//ImageIO.read(new File(filepath))
			// load the face image
			try
			{
				frameArr[i] = java.nio.file.Files.readAllBytes(imgFile.toPath());
			}
			catch (IOException e) { throw new RuntimeException(e); }
			if (frameArr[i] == null) {
				throw new RuntimeException("Can't load image from " + imgFile.getAbsolutePath());
			}
			i++;
		}	 
		
		return frameArr;
	}
	
	private IplImage[] loadIplImages(String imgDirname, final String imgSuffix)
	{
		File dir = new File(imgDirname);
		File [] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(imgSuffix);
			}
		});	

		// allocate the face-image array and person number matrix
		IplImage[] frameArr = new IplImage[files.length];			

		int i = 0;
		for (File imgFile : files)
		{
			// load the face image
			frameArr[i] = cvLoadImage(
					imgFile.getAbsolutePath(), // filename
					CV_LOAD_IMAGE_GRAYSCALE); // isColor

			if (frameArr[i] == null) {
				throw new RuntimeException("Can't load image from " + imgFile.getAbsolutePath());
			}
			i++;
		}	 
		
		return frameArr;
	}
}
