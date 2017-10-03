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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.acita15.facerec.VideoHelper;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;

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
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.nio.file.Files;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
//import static org.bytedeco.javacpp.opencv_legacy.*;

public class VideoSource2 implements StatelessOperator {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(VideoSource2.class);
	
	private IplImage[] testIplFrames = null;
	private byte[][] testRawFrames = null;
	private final String testFramesDir = GLOBALS.valueFor("testFramesDir");
	//private final String testFramesDir = "images2";
	//private final String testFramesDir = "images/chokepoint";
	private final String extractedFilesDir = "resources/source2";
	private final boolean loadIplImages = false; 	//TODO: Figure out how to convert between iplimage and byte array.

	private VideoHelper videoHelper = null;

	public void setUp() {
		System.out.println("Setting up VIDEO_SOURCE2 operator with id="+api.getOperatorId());
		videoHelper = new VideoHelper();
		//Set<String> jarImageFilenames = getJarImageFilenames(testFramesDir);
		//Set<File> imgFiles = extractJarImages(jarImageFilenames, extractedFilesDir);
		//testIplFrames = loadGreyImages(extractedFilesDir);
		//testRawFrames = videoHelper.loadImagesFromJar(testFramesDir);
		testRawFrames = videoHelper.loadImages(testFramesDir);
	}

	public void processData(DataTuple dt) {

		try
		{

			Map<String, Integer> mapper = api.getDataMapper();
			DataTuple data = new DataTuple(mapper, new TuplePayload());
			
			long tupleId = 0;
			
			boolean sendIndefinitely = Boolean.parseBoolean(GLOBALS.valueFor("sendIndefinitely"));
			long numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
			long warmUpTuples = Long.parseLong(GLOBALS.valueFor("warmUpTuples"));
			//int tupleSizeChars = Integer.parseInt(GLOBALS.valueFor("tupleSizeChars"));
			boolean rateLimitSrc = Boolean.parseBoolean(GLOBALS.valueFor("rateLimitSrc"));
			long frameRate = Long.parseLong(GLOBALS.valueFor("frameRate"));
			long interFrameDelay = 1000 / frameRate;
			logger.info("Source inter-frame delay="+interFrameDelay);
			
			final long tStart = System.currentTimeMillis();
			
			//String testFramesDir = GLOBALS.valueFor("testFramesDir");
			//String imgFileExt = GLOBALS.valueFor("imgFileExt");
			//String testFramesDir = "images";
			//TODO: Load resources from jar.
			//String testFramesDir = "/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/images";
			//String testFramesDir = "images";
			
			//logger.info("Loading test images...");
			//byte[][] testFrames = loadImagesFromJar(testFramesDir);
			//IplImage[] testFrames = loadGreyImages(testFramesDir);
			
			//logger.info("Loaded "+testFrames.length+" test images.");
			int currentFrame = 0;
			
			while(sendIndefinitely || tupleId < numTuples + warmUpTuples)
			{
				if (tupleId == warmUpTuples)
				{ 
					long tWarmedUp = System.currentTimeMillis();
					logger.info("Source sending started at t="+tWarmedUp);
					logger.info("Source sending started at t="+tWarmedUp);
					logger.info("Source sending started at t="+tWarmedUp);
				}
				/*
				Mat img = testFrames[currentFrame];
				byte[] matBytes = new byte[safeLongToInt(img.total())*img.channels()];
				img.data().get(matBytes);
				DataTuple output = data.newTuple(tupleId, matBytes, img.rows(), img.cols(), img.type(), 0, 0, 0, 0);
				*/
				DataTuple output = null;

				if (loadIplImages)
				{
					IplImage iplImage = testIplFrames[currentFrame];
					byte[] iplBytes = new byte[iplImage.imageSize()];
					iplImage.getByteBuffer().get(iplBytes);
					//TODO: Rows, cols, type?
					//output = data.newTuple(tupleId, iplBytes, iplImage.rows(), iplImage.cols(), iplImage.type(), 0, 0, 0, 0);
					currentFrame = (currentFrame + 1) % testIplFrames.length;
				}
				else
				{
					
					output = data.newTuple(tupleId, testRawFrames[currentFrame], 0, 0, 1, 0, 0, 0, 0, "");
					//output = data.newTuple(tupleId, testRawFrames[51], 0, 0, 1, 0, 0, 0, 0, "");
					currentFrame = (currentFrame + 1) % testRawFrames.length;
				}
	 
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
			}
		}
		catch(Exception e)
		{
			logger.error("Exception in video source: "+e);		
			e.printStackTrace();
		}
		finally
		{
			System.exit(0);
		}
	}
	
	public void processData(List<DataTuple> arg0) {
		// TODO Auto-generated method stub
		
	}

}
