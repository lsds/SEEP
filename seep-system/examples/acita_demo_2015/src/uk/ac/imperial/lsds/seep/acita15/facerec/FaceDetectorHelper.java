package uk.ac.imperial.lsds.seep.acita15.facerec;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

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

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import uk.ac.imperial.lsds.seep.GLOBALS;

public class FaceDetectorHelper
{
	private final static Logger logger = LoggerFactory.getLogger(FaceDetectorHelper.class);
	private final static boolean resourcesInJar = Boolean.parseBoolean(GLOBALS.valueFor("resourcesInJar"));
	private final static double SCALE_FACTOR = 1.1;
	private final static int MIN_FEATURE_DIM = 40;
	private final static double RELATIVE_FACE_SIZE = 0.2;
	private final static String classifierName = "cascades/haarcascade_frontalface_alt.xml";
	//private final static String lbpClassifierName = "cascades/lbpcascade_frontalface_improved.xml";
	private final static String lbpClassifierName = "cascades/lbpcascade_frontalface.xml";

	private CvHaarClassifierCascade faceDetector = null;
	private CascadeClassifier lbpFaceDetector = null;
	private Java2DFrameConverter frameConverter = null;
	private OpenCVFrameConverter matConverter = null;
	private OpenCVFrameConverter iplConverter = null;
	
	private boolean recordImages = false;


	public FaceDetectorHelper()
	{
		frameConverter = new Java2DFrameConverter();
		matConverter = new OpenCVFrameConverter.ToMat();
		iplConverter = new OpenCVFrameConverter.ToIplImage();

		try
		{
			//this.faceDetector = loadFaceCascade();
			this.faceDetector = null; 
			this.lbpFaceDetector = loadLBPFaceCascade(); 
		}
		catch(Exception e) { throw new RuntimeException(e); }
	}

	public static CvHaarClassifierCascade loadFaceCascade() throws IOException 
	{
		String classifierPath = null;
		if (resourcesInJar)
		{	
			classifierPath = extractFaceCascadeFromJar();
		}
		else
		{
			classifierPath = onDiskResourceRoot() + "/" + classifierName;
		}

		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		// We can "cast" Pointer objects by instantiating a new object of the desired class.
		CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(classifierPath));
		if (classifier.isNull()) {
		    logger.error("Error loading classifier file \"" + classifierPath + "\".");
		    System.exit(1);
		}
		return classifier;
	}

	public static CascadeClassifier loadLBPFaceCascade() throws IOException 
	{
		String classifierPath = null;
		if (resourcesInJar)
		{	
			classifierPath = extractFaceCascadeFromJar();
		}
		else
		{
			classifierPath = onDiskResourceRoot() + "/" + lbpClassifierName;
		}

		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		// We can "cast" Pointer objects by instantiating a new object of the desired class.
		//CascadeClassifier classifier = new CascadeClassifier(cvLoad(classifierPath));
		CascadeClassifier classifier = new CascadeClassifier(classifierPath);
		if (classifier.isNull()) {
		    logger.error("Error loading classifier file \"" + classifierPath + "\".");
		    System.exit(1);
		}
		return classifier;
	}

	private static String extractFaceCascadeFromJar() throws IOException
	{
		File tmpImgFile = new File("resources/"+classifierName);
		tmpImgFile.deleteOnExit();
		tmpImgFile.mkdirs();
		InputStream fileInJar = FaceDetectorHelper.class.getClassLoader().getResourceAsStream(classifierName);
		Files.copy(fileInJar, tmpImgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return tmpImgFile.getAbsolutePath();
	}

	private static String onDiskResourceRoot()
	{
		//return GLOBALS.valueFor("onDiskResourceRoot");
		return new File(GLOBALS.valueFor("repoDir")+"/seep-system/examples/acita_demo_2015/resources").getAbsolutePath();
	}

	public int[] detectFirstFace(IplImage bwImg, int rows)
	{
		//return detectFirstFaceHaar(bwImg, rows);
		return detectFirstFaceMultiScale(bwImg, rows);
	}

	public int[] detectFirstFaceMultiScale(IplImage bwImg, int rows)
	{
		int absoluteFaceSize = VideoHelper.safeLongToInt(Math.round(rows * RELATIVE_FACE_SIZE));
		//CvMemStorage storage = cvCreateMemStorage(0);
		//cvClearMemStorage(storage);
		try {
			//int flags = CV_HAAR_DO_CANNY_PRUNING;
			int flags = 0;
			//CvSize minFeatureSize = cvSize(MIN_FEATURE_DIM, MIN_FEATURE_DIM);
			//CvSize maxFeatureSize = cvSize(0,0); //No max
			Size minFeatureSize = new Size(MIN_FEATURE_DIM, MIN_FEATURE_DIM);
			Size maxFeatureSize = new Size(0,0); //No max
			int minNeighbours = 2;

			Mat bwMat = matConverter.convertToMat(iplConverter.convert(bwImg));

			RectVector rects = new RectVector();
			//CvSeq rects = cvHaarDetectObjects(bwImg, faceDetector, storage, SCALE_FACTOR, minNeighbours, flags, minFeatureSize, maxFeatureSize);
			lbpFaceDetector.detectMultiScale(bwMat, rects, SCALE_FACTOR, minNeighbours, flags, minFeatureSize, maxFeatureSize);
			//int nFaces = rects.total();
			long nFaces = rects.size();
			if (nFaces == 0) {
				return null;
			}
			else
			{
				logger.debug("Detected faces: "+nFaces);
				//BytePointer elem = cvGetSeqElem(rects, 0);
				//CvRect rect = new CvRect(elem);
				Rect rect = rects.get(0);
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
			//cvReleaseMemStorage(storage);
		}




	}

	//public int[] detectFirstFace(IplImage bwImg, int absoluteFaceSize)
	public int[] detectFirstFaceHaar(IplImage bwImg, int rows)
	{
		int absoluteFaceSize = VideoHelper.safeLongToInt(Math.round(rows * RELATIVE_FACE_SIZE));
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

	public void recordFaceDetection(long tupleId, IplImage bwImg, int[] bbox)
	{
		cvRectangle(bwImg, cvPoint(bbox[0], bbox[1]), cvPoint(bbox[2], bbox[3]), CvScalar.RED, 1, CV_AA, 0);
	
		BufferedImage outputImg = frameConverter.convert(iplConverter.convert(bwImg));
		File outputFile = new File("imgout/detected/"+tupleId+".jpg");
		outputFile.mkdirs();
		try
		{
			ImageIO.write(outputImg, "jpg", outputFile);			
		}
		catch(IOException e) { throw new RuntimeException(e); }
	}
	
	public IplImage getIplImage(byte[] bytes)
	{
		return VideoHelper.getIplImage(bytes, frameConverter, iplConverter);
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
