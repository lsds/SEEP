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
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

import java.awt.image.BufferedImage;

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
import static org.bytedeco.javacpp.opencv_legacy.*;

public class VideoSource implements StatelessOperator {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(VideoSource.class);
	
	private IplImage[] testIplFrames = null;
	private byte[][] testRawFrames = null;
	//private final String testFramesDir = "images";
	private final String testFramesDir = "images/chokepoint";
	private final String extractedFilesDir = "resources/source";
	private final boolean loadIplImages = false; 	//TODO: Figure out how to convert between iplimage and byte array.
	
	public void setUp() {
		System.out.println("Setting up VIDEO_SOURCE operator with id="+api.getOperatorId());
		
		//Set<String> jarImageFilenames = getJarImageFilenames(testFramesDir);
		//Set<File> imgFiles = extractJarImages(jarImageFilenames, extractedFilesDir);
		//testIplFrames = loadGreyImages(extractedFilesDir);
		testRawFrames = loadImagesFromJar(testFramesDir);
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
		//String testFramesDir = "images";
		//TODO: Load resources from jar.
		//String testFramesDir = "/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/images";
		//String testFramesDir = "images";
		
		//logger.info("Loading test images...");
		//byte[][] testFrames = loadImagesFromJar(testFramesDir);
		//IplImage[] testFrames = loadGreyImages(testFramesDir);
		
		//logger.info("Loaded "+testFrames.length+" test images.");
		int currentFrame = 0;
		
		while(sendIndefinitely || tupleId < numTuples)
		{
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
				output = data.newTuple(tupleId, testRawFrames[currentFrame], 0, 0, 0, 0, 0, 0, 0, "");
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
		System.exit(0);
	}
	
	public void processData(List<DataTuple> arg0) {
		// TODO Auto-generated method stub
		
	}

	private Set<String> getJarImageFilenames(String imgDirname)
	{
		final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

		Set<String> filenames = new HashSet<>();
		try
		{
		    final JarFile jar = new JarFile(jarFile);
		    final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
		    while(entries.hasMoreElements()) {
		        final String name = entries.nextElement().getName();
		        if (name.startsWith(imgDirname + "/") && 
		        		//(name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".pgm"))) { //filter according to the path
		        		
		        		(name.endsWith(".jpg") || name.endsWith(".pgm"))) { //filter according to the path
		        	logger.info("Source adding filename: "+name);
		        	filenames.add(name);
		        }
		    }
		    jar.close();
		} catch (IOException e) { throw new RuntimeException(e); }
		logger.info("Found the following jar image filenames:"+filenames);
		return filenames;
	}
	
	private Set<File> extractJarImages(Set<String> jarImagePaths, String extractionDir)
	{
		Set<File> extractedImages = new HashSet<>();
		for (String jarImagePath: jarImagePaths)
		{
			try
			{
				String imgFilename = new File(jarImagePath).getName();
				InputStream is = getClass().getClassLoader().getResourceAsStream(jarImagePath);
				
				Path dest = Paths.get(extractionDir + "/" + imgFilename);
				Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
				extractedImages.add(dest.toFile());
			}
			catch (IOException e) { throw new RuntimeException(e); }
		}
		return extractedImages;
	}
	
	private byte[][] loadImagesFromJar(String imgDirname)
	{

		Set<String> filenames = getJarImageFilenames(imgDirname);
	    
		byte[][] frameArr = new byte[filenames.size()][];

		logger.info("Source loading "+filenames.size()+" images.");
		int i = 0;
		for (String filename : filenames)
		{
			try
			{
				InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	
				int nRead;
				byte[] data = new byte[16384];
	
				while ((nRead = is.read(data, 0, data.length)) != -1) {
				  buffer.write(data, 0, nRead);
				}
	
				buffer.flush();
				frameArr[i] = buffer.toByteArray();
			}
			catch (IOException e) { throw new RuntimeException(e); }
			if (frameArr[i] == null) {
				throw new RuntimeException("Can't load image from " + filename);
			}
			i++;
		}	 
		
		return frameArr;

	}
	
	private byte[][] loadImages(String imgDirname)
	{		
		File dir = new File(imgDirname);
		File [] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".pgm");
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
	
	private IplImage[] loadGreyImages(String imgDirname)
	{
		File dir = new File(imgDirname);
		File [] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".pgm");
			}
		});	

		// allocate the face-image array and person number matrix
		IplImage[] frameArr = new IplImage[files.length];			

		int i = 0;
		for (File imgFile : files)
		{
			// load the face image
			IplImage img = cvLoadImage(
					imgFile.getAbsolutePath(), // filename
					CV_LOAD_IMAGE_GRAYSCALE); // isColor
			/*
			Mat img = imread(
					imgFile.getAbsolutePath(), // filename
					CV_LOAD_IMAGE_GRAYSCALE); // isColor
			*/

			if (img == null) {
				throw new RuntimeException("Can't load image from " + imgFile.getAbsolutePath());
			}
			
			//byte[] data = new byte[safeLongToInt(img.total())*img.channels()];
			//img.data().get(data);

		    //byte[] iplBytes = new byte[img.imageSize()];
		    //iplimage.getByteBuffer().get(iplBytes); 
			
			frameArr[i] = img;
			i++;
		}	 
		
		return frameArr;
	}
	
	public int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}
}
