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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

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

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class FaceDetector implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(FaceDetector.class);
	private int processed = 0;
	
	private CascadeClassifier faceDetector = null;
	private Java2DFrameConverter frameConverter = null;
	private OpenCVFrameConverter matConverter = null;
	
	private final double SCALE_FACTOR = 1.1;
	private final double RELATIVE_FACE_SIZE = 0.2;
	
	public void processData(DataTuple data) {
		
		
		long tupleId = data.getLong("tupleId");
		//String value = data.getString("value") + "," + api.getOperatorId();
		byte[] value = data.getByteArray("value");
		int cols = data.getInt("cols");
		int rows = data.getInt("rows");
		int type = data.getInt("type");
			
		int absoluteFaceSize = safeLongToInt(Math.round(rows * RELATIVE_FACE_SIZE));
		
		logger.info("Received "+cols+"x"+rows+" frame of type "+type);
		/*
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		*/


		Rect faceDetections = new Rect();
		
		logger.info("Limit before detection="+faceDetections.limit());
		
		ByteArrayInputStream bais = new ByteArrayInputStream(value);
		
		try
		{
			Mat frameOld = matConverter.convertToMat(frameConverter.convert(ImageIO.read(bais)));
			//Mat emptyFrame = new Mat(rows, cols, type);
			//Mat frame = new Mat(value, false); //TODO: Is it really signed?
			//TODO: Copy could be slow here.
			Mat frame = new Mat(rows, cols, type, new BytePointer(value));
			if (frame.rows() != rows) { throw new RuntimeException("Logic error, row mismatch."); }
			
			BufferedImage bufImage = frameConverter.convert(matConverter.convert(frame));
			File imgFile = new File("/tmp/"+tupleId+".jpg");
			ImageIO.write(bufImage, "jpg", imgFile);
			
			logger.info("Frame total size="+frame.total()+",cols="+frame.cols()+",rows="+frame.rows()+",type="+frame.type());
			faceDetector.detectMultiScale(frame, faceDetections, SCALE_FACTOR, 2, 0, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
		}
		catch(IOException e)
		{
			throw new RuntimeException("Error reading image: ", e);
		}
		int numFaces = faceDetections.limit();
		
		logger.info("Num faces detected = "+numFaces);
		DataTuple outputTuple = null;
		
		if (numFaces > 0)
		{
			//Just take the first face for now
			
			int x = faceDetections.position(0).x();
			int y = faceDetections.position(0).y();
			int height = faceDetections.position(0).height();
			int width = faceDetections.position(0).width();
			/*
			int x = faceDetections.x();
			int y = faceDetections.y();
			int height = faceDetections.height();
			int width = faceDetections.width();
			*/
			logger.info("Detected face: x="+x+",y="+y+",height="+height+",width="+width);
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
		testFaceDetection();
		try
		{
			/*
			String classifierName = Paths.get(
                FaceDetector.class.getResource("/cascades/haarcascade_frontalface_default.xml")
                        .toURI()).toString();
                        */
			//File classifierFile = new File("file:///homes/dan/")
			logger.info("Thread cl:");
			ClassLoader cl = Thread.currentThread().getContextClassLoader();

			URL[] urls = ((URLClassLoader)cl).getURLs();

			for(URL url: urls){
				System.out.println(url.getFile());
			}
			
		       
			logger.info("System cl:");
			cl = ClassLoader.getSystemClassLoader();

			urls = ((URLClassLoader)cl).getURLs();

			for(URL url: urls){
				System.out.println(url.getFile());
			}
			
			//File classifierFile = new File("file:///home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/cascades/haarcascade_frontalface_alt.xml");
			//String classifierName = classifierFile.toURI().toString();
	        //faceDetector = new CascadeClassifier(classifierName);
			faceDetector = loadFaceCascade();
		}
		catch(Exception e) { throw new RuntimeException(e); }

        frameConverter = new Java2DFrameConverter();
        matConverter = new OpenCVFrameConverter.ToMat();
        
        
        //Test it works.
        String testFramesDir = "/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/images";
    	File dir = new File(testFramesDir);
		File [] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".pgm");
			}
		});	
		
		for (int i = 0; i < files.length; i++)
		{
			Mat testFrame = imread(
				files[0].getAbsolutePath(), // filename
				CV_LOAD_IMAGE_GRAYSCALE); // isColor 
		
			int absoluteFaceSize = safeLongToInt(Math.round(testFrame.rows() * RELATIVE_FACE_SIZE));
			Rect faceDetections = new Rect();
			
			faceDetector.detectMultiScale(testFrame, faceDetections, SCALE_FACTOR, 2, 0, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
			
			int x = faceDetections.position(0).x();
			int y = faceDetections.position(0).y();
			int height = faceDetections.position(0).height();
			int width = faceDetections.position(0).width();
			/*
			int x = faceDetections.x();
			int y = faceDetections.y();
			int height = faceDetections.height();
			int width = faceDetections.width();
			*/
			logger.info("Setup detect rectangle: x="+x+",y="+y+",height="+height+",width="+width);
		}
	}

	
	public int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}
	
	
	public static CvHaarClassifierCascade loadFaceCascade() throws IOException {
		URL url = new URL("file:///home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/cascades/haarcascade_frontalface_alt.xml");
		
        File file = Loader.extractResource(url, null, "classifier", ".xml");
        file.deleteOnExit();
        String classifierName = file.getAbsolutePath();
        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);

        // We can "cast" Pointer objects by instantiating a new object of the desired class.
        CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(classifierName));
        if (classifier.isNull()) {
            System.err.println("Error loading classifier file \"" + classifierName + "\".");
            System.exit(1);
        }
		return classifier;
	}

	public static IplImage loadImage(File file) throws IOException {
		// Verify file
		if (!file.exists()) {
			throw new FileNotFoundException("Image file does not exist: " + file.getAbsolutePath());
		}
		// Read input image
		IplImage image = cvLoadImage(file.getAbsolutePath());
		if (image == null) {
			throw new IOException("Couldn't load image: " + file.getAbsolutePath());
		}
		return image;
	}
	
	public static void detectFaceInImage(final IplImage orig,
			final IplImage input,
			final CvHaarClassifierCascade cascade,
			final int[] bbox) throws Exception {
		CvMemStorage storage = cvCreateMemStorage(0);
		cvClearMemStorage(storage);
		try {
			double search_scale_factor = 1.1;
			int flags = CV_HAAR_DO_CANNY_PRUNING;
			CvSize minFeatureSize = cvSize(40, 40);
			CvSeq rects = cvHaarDetectObjects(input, cascade, storage, search_scale_factor, 2, flags, minFeatureSize, cvSize(0, 0));
			int nFaces = rects.total();
			if (nFaces == 0) {
				throw new Exception("No faces detected");
			}
			else
			{
				logger.info("Detected faces: "+nFaces);
			}
			for (int iface = 0; iface < nFaces; ++iface) {
				BytePointer elem = cvGetSeqElem(rects, iface);
				CvRect rect = new CvRect(elem);
				bbox[0] = rect.x();
				bbox[1] = rect.y();
				bbox[2] = rect.x() + rect.width();
				bbox[3] = rect.y() + rect.height();
				logger.info("Face bounding box=("+bbox[0]+","+bbox[1]+"),("+bbox[2]+","+bbox[3]+")");
				// display landmarks
				cvRectangle(orig, cvPoint(bbox[0], bbox[1]), cvPoint(bbox[2], bbox[3]), CV_RGB(255, 0, 0));
			}
		} finally {
			cvReleaseMemStorage(storage);
		}
	}
	
	public static File[] loadFiles()
	{
        //Test it works.
        String testFramesDir = "/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/images";
    	File dir = new File(testFramesDir);
		File [] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".pgm");
			}
		});	
		
		return files;
	}

	public static void testFaceDetection()
	{

		//final File faceCascadeFile = new File("haarcascade_frontalface_alt.xml");
		//File faceCascadeFile = new File("/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/cascades/haarcascade_frontalface_alt.xml");

		//final File inputImage = new File("face.jpg");
		try {
			final File[] inputImages = loadFiles();
			final CvHaarClassifierCascade faceCascade = loadFaceCascade();
			System.out.println("Count: " + faceCascade.count());
			for (File inputImage : inputImages)
			{
				final IplImage image = loadImage(inputImage);
				final IplImage imageBW = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
				cvCvtColor(image, imageBW, CV_BGR2GRAY);
				final int[] bbox = new int[4];
				detectFaceInImage(image, imageBW, faceCascade, bbox);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
