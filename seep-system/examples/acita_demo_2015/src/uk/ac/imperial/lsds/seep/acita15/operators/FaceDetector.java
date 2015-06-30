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

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class FaceDetector implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(FaceDetector.class);
	private int processed = 0;
	
	//private CascadeClassifier faceDetector = null;
	private CvHaarClassifierCascade faceDetector = null;
	private Java2DFrameConverter frameConverter = null;
	private OpenCVFrameConverter matConverter = null;
	private OpenCVFrameConverter iplConverter = null;
	
	private final double SCALE_FACTOR = 1.1;
	private final double RELATIVE_FACE_SIZE = 0.2;
	
	public void processData(DataTuple data) {
		
		long tProcessStart = System.currentTimeMillis();
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

		IplImage img = parseBufferedImage(value);
		IplImage imgBW = prepareBWImage(img);
		int[] bbox = detectFirstFace(imgBW);
		
		DataTuple outputTuple = null;
		
		if (bbox != null)
		{
			logger.info("Found face at ("+bbox[0]+","+bbox[1]+"),("+bbox[2]+","+bbox[3]+")");
			outputTuple = data.setValues(tupleId, value, rows, cols, type, bbox[0], bbox[1], bbox[2], bbox[3]);
		}
		else	
		{
			outputTuple = data.setValues(tupleId, value, rows, cols, type, 0, 0, 0, 0);
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
		
		logger.info("Face detector processed "+cols+"x"+rows+" tuple in " + (System.currentTimeMillis() - tProcessStart) + "ms");
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
			faceDetector = loadFaceCascade();
		}
		catch(Exception e) { throw new RuntimeException(e); }

        frameConverter = new Java2DFrameConverter();
        //matConverter = new OpenCVFrameConverter.ToMat();
        iplConverter = new OpenCVFrameConverter.ToIplImage();
	}

	
	public int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}
	
	
	public static CvHaarClassifierCascade loadFaceCascade() throws IOException {
		
		String filepath = "cascades/haarcascade_frontalface_alt.xml";
		File tmpImgFile = new File("resources/"+filepath);
		tmpImgFile.deleteOnExit();
		tmpImgFile.mkdirs();
		InputStream fileInJar = FaceDetector.class.getClassLoader().getResourceAsStream(filepath);
		Files.copy(fileInJar, tmpImgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		//getClass().getClassLoader().getResourceAsStream("cascades/haarcascade_frontalface_alt.xml");
		//URL url = new URL("file:///home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/cascades/haarcascade_frontalface_alt.xml");
		
        //File file = Loader.extractResource(url, null, "classifier", ".xml");
        //file.deleteOnExit();
        String classifierName = tmpImgFile.getAbsolutePath();
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

	public IplImage parseBufferedImage(byte[] bytes)
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
	
	public IplImage prepareBWImage(IplImage image)
	{
		final IplImage imageBW = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
		cvCvtColor(image, imageBW, CV_BGR2GRAY);
		return imageBW;
	}

	public int[] detectFirstFace(IplImage bwImg)
	{
		CvMemStorage storage = cvCreateMemStorage(0);
		cvClearMemStorage(storage);
		try {
			double search_scale_factor = 1.1;
			int flags = CV_HAAR_DO_CANNY_PRUNING;
			CvSize minFeatureSize = cvSize(40, 40);
			CvSeq rects = cvHaarDetectObjects(bwImg, faceDetector, storage, search_scale_factor, 2, flags, minFeatureSize, cvSize(0, 0));
			int nFaces = rects.total();
			if (nFaces == 0) {
				return null;
			}
			else
			{
				logger.info("Detected faces: "+nFaces);
				BytePointer elem = cvGetSeqElem(rects, 0);
				CvRect rect = new CvRect(elem);
				int bbox[] = new int[4];
				bbox[0] = rect.x();
				bbox[1] = rect.y();
				bbox[2] = rect.x() + rect.width();
				bbox[3] = rect.y() + rect.height();
				logger.info("Face bounding box=("+bbox[0]+","+bbox[1]+"),("+bbox[2]+","+bbox[3]+")");
				return bbox;
			}
		}
		finally 
		{
			cvReleaseMemStorage(storage);
		}
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
