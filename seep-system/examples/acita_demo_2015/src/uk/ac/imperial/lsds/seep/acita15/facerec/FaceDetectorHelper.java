package uk.ac.imperial.lsds.seep.acita15.facerec;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;

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

public class FaceDetectorHelper
{
	private final static Logger logger = LoggerFactory.getLogger(FaceDetectorHelper.class);

	//private CascadeClassifier faceDetector = null;
	private CvHaarClassifierCascade faceDetector = null;
	private Java2DFrameConverter frameConverter = null;
	private OpenCVFrameConverter matConverter = null;
	private OpenCVFrameConverter iplConverter = null;
	
	private final static double SCALE_FACTOR = 1.1;
	private final static int MIN_FEATURE_DIM = 40;
	private final double RELATIVE_FACE_SIZE = 0.2;
	private boolean recordImages = false;


	public FaceDetectorHelper()
	{
		try
		{
			this.faceDetector = loadFaceCascade();
		}
		catch(Exception e) { throw new RuntimeException(e); }
	}

	public static CvHaarClassifierCascade loadFaceCascade() throws IOException {
		
		String filepath = "cascades/haarcascade_frontalface_alt.xml";
		File tmpImgFile = new File("resources/"+filepath);
		tmpImgFile.deleteOnExit();
		tmpImgFile.mkdirs();
		InputStream fileInJar = FaceDetectorHelper.class.getClassLoader().getResourceAsStream(filepath);
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
            logger.error("Error loading classifier file \"" + classifierName + "\".");
            System.exit(1);
        }
		return classifier;
	}

	//public int[] detectFirstFace(IplImage bwImg, int absoluteFaceSize)
	public int[] detectFirstFace(IplImage bwImg, int rows)
	{
		int absoluteFaceSize = safeLongToInt(Math.round(rows * RELATIVE_FACE_SIZE));
		CvMemStorage storage = cvCreateMemStorage(0);
		cvClearMemStorage(storage);
		try {
			int flags = CV_HAAR_DO_CANNY_PRUNING;
			CvSize minFeatureSize = cvSize(MIN_FEATURE_DIM, MIN_FEATURE_DIM);
			CvSize maxFeatureSize = cvSize(0,0); //No max
			int minNeighbours = 2;

			CvSeq rects = cvHaarDetectObjects(bwImg, faceDetector, storage, SCALE_FACTOR, minNeighbours, flags, minFeatureSize, maxFeatureSize);
			int nFaces = rects.total();
			if (nFaces == 0) {
				return null;
			}
			else
			{
				logger.debug("Detected faces: "+nFaces);
				BytePointer elem = cvGetSeqElem(rects, 0);
				CvRect rect = new CvRect(elem);
				int bbox[] = new int[4];
				bbox[0] = rect.x();
				bbox[1] = rect.y();
				bbox[2] = rect.x() + rect.width();
				bbox[3] = rect.y() + rect.height();
				
				logger.debug("Face bounding box=("+bbox[0]+","+bbox[1]+"),("+bbox[2]+","+bbox[3]+")");
				return bbox;
			}
		}
		finally 
		{
			cvReleaseMemStorage(storage);
		}
	}

	public int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException
			(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	/*
	public static void testFaceDetection()
	{

		//final File faceCascadeFile = new File("haarcascade_frontalface_alt.xml");
		//File faceCascadeFile = new File("/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/cascades/haarcascade_frontalface_alt.xml");

		//final File inputImage = new File("face.jpg");
		try {
			final File[] inputImages = loadFiles();
			final CvHaarClassifierCascade faceCascade = loadFaceCascade();
			logger.debug("Count: " + faceCascade.count());
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

	private static void detectFaceInImage(final IplImage orig,
			final IplImage input,
			final CvHaarClassifierCascade cascade,
			final int[] bbox) throws Exception {
		CvMemStorage storage = cvCreateMemStorage(0);
		cvClearMemStorage(storage);
		try {
			int flags = CV_HAAR_DO_CANNY_PRUNING;
			CvSize minFeatureSize = cvSize(MIN_FEATURE_DIM, MIN_FEATURE_DIM);
			CvSeq rects = cvHaarDetectObjects(input, cascade, storage, SCALE_FACTOR, 2, flags, minFeatureSize, cvSize(0, 0));
			int nFaces = rects.total();
			if (nFaces == 0) {
				throw new Exception("No faces detected");
			}
			else
			{
				logger.debug("Detected faces: "+nFaces);
			}
			for (int iface = 0; iface < nFaces; ++iface) {
				BytePointer elem = cvGetSeqElem(rects, iface);
				CvRect rect = new CvRect(elem);
				bbox[0] = rect.x();
				bbox[1] = rect.y();
				bbox[2] = rect.x() + rect.width();
				bbox[3] = rect.y() + rect.height();
				logger.debug("Face bounding box=("+bbox[0]+","+bbox[1]+"),("+bbox[2]+","+bbox[3]+")");
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
	*/	
}
