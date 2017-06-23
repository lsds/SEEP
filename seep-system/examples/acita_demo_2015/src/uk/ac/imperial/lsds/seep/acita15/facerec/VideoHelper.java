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
package uk.ac.imperial.lsds.seep.acita15.facerec;

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
import java.nio.ByteBuffer;
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

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.Mat;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.nio.file.Files;

import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
//import static org.bytedeco.javacpp.opencv_legacy.*;
//

public class VideoHelper {

	private static final Logger logger = LoggerFactory.getLogger(VideoHelper.class);
	private final boolean resizeImages = Boolean.parseBoolean(GLOBALS.valueFor("resizeImages"));

	public VideoHelper()
	{
	}

	public byte[][] loadImages(String imgDirname)
	{
		boolean loadIplImages = Boolean.parseBoolean(GLOBALS.valueFor("loadIplImages"));
		if (Boolean.parseBoolean(GLOBALS.valueFor("resourcesInJar")))
		{
			return loadImagesFromJar(imgDirname, loadIplImages);	
		}
		else
		{
			return loadImagesFromDisk(imgDirname, loadIplImages);	
		}
	}

	public byte[][] loadImagesFromJar(String imgDirname) { return loadImagesFromJar(imgDirname, false); }

	public byte[][] loadImagesFromJar(String imgDirname, boolean loadIplImages)
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
				buffer.close();
				is.close();

				if (resizeImages)
				{
					int originalSize = frameArr[i].length;
					is = new ByteArrayInputStream(frameArr[i]);
					buffer = new ByteArrayOutputStream();

					BufferedImage original = ImageIO.read(is);	
					if (original == null) { throw new RuntimeException("Error reading byte array to buffered image."); }

					ImageIO.write(Scalr.resize(original, original.getWidth() / 2), "jpg", buffer);
					buffer.flush();
					frameArr[i] = buffer.toByteArray();	
					logger.debug("Reduced image from "+originalSize+" to "+frameArr[i].length+" bytes");
					buffer.close();
					is.close();
				}

				if (loadIplImages)
				{
					Java2DFrameConverter frameConverter = new Java2DFrameConverter();
					OpenCVFrameConverter iplConverter = new OpenCVFrameConverter.ToIplImage();
					is = new ByteArrayInputStream(frameArr[i]);
					IplImage iplImage = iplConverter.convertToIplImage(frameConverter.convert(ImageIO.read(is)));
					//byte iplImageBytes = new byte[...];
					//frameArr[i] = iplImage.imageData().get(iplImageBytes);
					if (!iplImage.imageData().asBuffer().hasArray()) { throw new RuntimeException("TODO"); }
					frameArr[i] = iplImage.imageData().asBuffer().array();
					is.close();
				}
			}
			catch (IOException e) { throw new RuntimeException(e); }
			if (frameArr[i] == null) {
				throw new RuntimeException("Can't load image from " + filename);
			}
			i++;
		}	 
		
		return frameArr;

	}

	private Set<String> getJarImageFilenames(String imgDirname)
	{
		final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

		Set<String> filenames = new TreeSet<>();
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
	
	private static String onDiskResourceRoot()
	{
		//return GLOBALS.valueFor("onDiskResourceRoot");
		return new File(GLOBALS.valueFor("repoDir")+"/seep-system/examples/acita_demo_2015/resources").getAbsolutePath();
	}
	
	public byte[][] loadImagesFromDisk(String imgDirname) { return loadImagesFromDisk(imgDirname, false); }

	public byte[][] loadImagesFromDisk(String imgDirname, boolean loadIplImages)	
	{		
		if (loadIplImages) { throw new RuntimeException("TODO: Change the below to use ImageIO.read and then convert?"); }
		File dir = new File(onDiskResourceRoot() + "/" + imgDirname);
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
					IMREAD_GRAYSCALE); // isColor
			/*
			Mat img = imread(
					imgFile.getAbsolutePath(), // filename
					IMREAD_GRAYSCALE); // isColor
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
	
	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	public static IplImage getIplImage(byte[] bytes, Java2DFrameConverter frameConverter, OpenCVFrameConverter iplConverter)
	{
		boolean loadIplImages = Boolean.parseBoolean(GLOBALS.valueFor("loadIplImages"));
		if (loadIplImages) { return new IplImage().imageData(new BytePointer(bytes)); }
		else { return VideoHelper.parseBufferedImage(bytes, frameConverter, iplConverter); }
	}

	public static IplImage parseBufferedImage(byte[] bytes, Java2DFrameConverter frameConverter, OpenCVFrameConverter iplConverter)
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try
		{
			IplImage img = iplConverter.convertToIplImage(frameConverter.convert(ImageIO.read(bais)));
			return img;
		} 
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static IplImage prepareBWImage(IplImage image, int type)
	{
		if (type > 0)
		{
			final IplImage imageBW = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
			cvCvtColor(image, imageBW, CV_BGR2GRAY);
			return imageBW;
		}
		else
		{
			return image; //Already bw
		}
	}
}
