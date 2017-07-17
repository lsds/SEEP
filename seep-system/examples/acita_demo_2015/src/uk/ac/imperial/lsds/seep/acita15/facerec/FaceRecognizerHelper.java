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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.bytedeco.javacpp.Loader;
//import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
//import org.bytedeco.javacpp.PointerPointer;
//import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_face.*;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class FaceRecognizerHelper
{
	private final static Logger logger = LoggerFactory.getLogger(FaceRecognizerHelper.class);
	private final static boolean resourcesInJar = Boolean.parseBoolean(GLOBALS.valueFor("resourcesInJar"));
	private final int opId;
	private final String trainingDir;
	private final String trainingList;
	private final String testImageFilename;
	private final Map<Integer, String> labelExamples = new HashMap<>();
	private final FaceRecognizer faceRecognizer;
	private final Java2DFrameConverter frameConverter = new Java2DFrameConverter();
	private final OpenCVFrameConverter matConverter = new OpenCVFrameConverter.ToMat();
	private final OpenCVFrameConverter iplConverter = new OpenCVFrameConverter.ToIplImage();
	private final String existingFaceRecModel = GLOBALS.valueFor("preTrainedFaceRecModel");
	
	public FaceRecognizerHelper(int opId, String trainingDir, String trainingList, String testImageFilename)
	{
		this.opId = opId;
		this.trainingDir = trainingDir;
		this.trainingList = trainingList;
		this.testImageFilename = testImageFilename;
		String[] imgFormats = ImageIO.getReaderFormatNames();
		logger.info("Image io supported image formats: ");
		for (int i = 0; i < imgFormats.length; i++) { logger.info(""+imgFormats[i]); }
		//this.faceRecognizer = trainRecognizer();
		this.faceRecognizer = createRecognizer();
		logger.info("FACE_RECOGNIZER setup complete.");
	}
	
	public int recognize(byte[] value, int x, int y, int width, int height, int type)
	{
		if (x <= 0 && y <=0 && width <= 0 && height <= 0) { return -1; }
		IplImage img = VideoHelper.getIplImage(value, frameConverter, iplConverter);
		IplImage imgBW = VideoHelper.prepareBWImage(img, type);
		cvSetImageROI(imgBW, cvRect(x, y, width, height));

		IplImage cropped = cvCreateImage(cvGetSize(imgBW), imgBW.depth(), imgBW.nChannels());
		cvCopy(imgBW, cropped);
		//Mat imgBWMat = matConverter.convertToMat(iplConverter.convert(imgBW));
		Mat imgBWMat = matConverter.convertToMat(iplConverter.convert(cropped));
		logger.debug("Converted roi to "+imgBWMat.cols()+"x"+imgBWMat.rows()+" mat");
		
		IntPointer predictedLabelChannels = new IntPointer(1);
		DoublePointer confidenceChannels = new DoublePointer(1);
		faceRecognizer.predict(imgBWMat, predictedLabelChannels, confidenceChannels);
		logger.debug("Predicted label for received image: " + predictedLabelChannels.get()+ " (ROI="+width+"x"+height+") with confidence "+confidenceChannels.get());

		if (labelExamples.containsKey(predictedLabelChannels.get()))
		{
			logger.debug("Example of matching face: "+labelExamples.get(predictedLabelChannels.get()));
		}
		return predictedLabelChannels.get();
	}
	
	public String getLabelExample(int label)
	{
		if (labelExamples.containsKey(label)) { return labelExamples.get(label); }
		else { return ""; }
	}
	public FaceRecognizer createRecognizer()
	{
		//FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
		// FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		FaceRecognizer lbphRecognizer = createLBPHFaceRecognizer();
		//FaceRecognizer lbphRecognizer = createEigenFaceRecognizer();
		//FaceRecognizer lbphRecognizer = createFisherFaceRecognizer();

		if (existingFaceRecModel != null && !existingFaceRecModel.trim().isEmpty())
		{  	
			logger.info("Loading existing pre-trained model from:"+existingFaceRecModel);
			lbphRecognizer.load(existingFaceRecModel);
		}
		else
		{
			trainRecognizer(lbphRecognizer);
			lbphRecognizer.save("trainedFaceRecModel.xml");
		}


		return lbphRecognizer;
	}

	public void trainRecognizer(FaceRecognizer untrainedRecognizer)
	{

		//File csv = new File(trainingDir+"/at.txt");
		logger.info("Loading training list from:"+trainingDir+"/"+trainingList);
		InputStream csv = null;
		if (resourcesInJar)
		{
			csv = this.getClass().getClassLoader().getResourceAsStream(trainingDir+"/"+trainingList);
		}
		else
		{
			try
			{
				csv = new FileInputStream(new File(onDiskResourceRoot() + "/" + trainingDir + "/"+trainingList));
			}catch(FileNotFoundException e) { throw new RuntimeException(e); }
		}

		Map<String, Integer> trainingFiles = new HashMap<>();
		
		try
		{
			try (BufferedReader br = new BufferedReader(new InputStreamReader(csv))) {
			    String line;
			    while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				int label = Integer.parseInt(values[1]);
				trainingFiles.put(values[0], label);
				if (!labelExamples.containsKey(label)) { labelExamples.put(label, values[0]); }
			    }
			}
		} catch(Exception e) { throw new RuntimeException(e); }
		
		logger.info("Training list contained "+trainingFiles.size()+" training images.");
		MatVector images = new MatVector(trainingFiles.size());
		
		Mat labels = new Mat(trainingFiles.size(), 1, CV_32SC1);
		IntBuffer labelsBuf = labels.getIntBuffer();
		
		int counter = 0;
		for (String filename : trainingFiles.keySet())
		{
			//File imgFile = new File(trainingDir+"/"+filename);
			//Mat img = imread(imgFile.getAbsolutePath(), IMREAD_GRAYSCALE);
			Mat img = loadBWMatImage(filename);
			logger.info("Read "+img.rows()+"x"+img.cols()+" training image from "+ filename);
			images.put(counter, img);
			int label = trainingFiles.get(filename);
			
			labelsBuf.put(counter, label);
			counter++;
		}
		
		untrainedRecognizer.train(images, labels);
	}
	
	/*
	public void testATT()
	{
		Mat testImage = imread(testImageFilename, IMREAD_GRAYSCALE);
		int predictedLabel = faceRecognizer.predict(testImage);

		logger.debug("Predicted label for test image: " + predictedLabel);
	}
	
	public void testSample()
	{
		File root = new File(trainingDir);

		FilenameFilter imgFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
			}
		};

		File[] imageFiles = root.listFiles(imgFilter);

		MatVector images = new MatVector(imageFiles.length);

		Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
		IntBuffer labelsBuf = labels.getIntBuffer();

		int counter = 0;

		for (File image : imageFiles) {
			Mat img = imread(image.getAbsolutePath(), IMREAD_GRAYSCALE);

			int label = Integer.parseInt(image.getName().split("\\-")[0]);

			images.put(counter, img);

			labelsBuf.put(counter, label);

			counter++;
		}

		//FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
		// FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
		FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

		faceRecognizer.train(images, labels);

		Mat testImage = imread(testImageFilename, IMREAD_GRAYSCALE);
		int predictedLabel = faceRecognizer.predict(testImage);

		logger.debug("Predicted label: " + predictedLabel);
	}
	*/
	
	public Mat loadBWMatImage(String filename)
	{
		try
		{
			String filepath = trainingDir+"/"+filename;
			logger.debug("Loading training image from: "+ filepath);
			String absPath = null;
			if (resourcesInJar)
			{
				File tmpImgFile = new File("/tmp/resources/"+opId+filepath);
				tmpImgFile.deleteOnExit();
				tmpImgFile.mkdirs();
				InputStream fileInJar = this.getClass().getClassLoader().getResourceAsStream(filepath);
				Files.copy(fileInJar, tmpImgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				absPath = tmpImgFile.getAbsolutePath();
			}
			else
			{
				absPath = onDiskResourceRoot()+"/"+filepath;
			}

			Mat img = imread(absPath, IMREAD_GRAYSCALE);
			return img;

			//BufferedImage bufImg = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(filepath));
			//logger.info("Loaded "+bufImg.getWidth()+"x"+bufImg.getHeight()+" training image");
			
			//IplImage img = iplConverter.convertToIplImage(frameConverter.convert(bufImg));
			//IplImage bwImg = prepareBWImage(img);
			//return matConverter.convertToMat(iplConverter.convert(bwImg));
		} catch (IOException e) { throw new RuntimeException(e); }
	}

	private static String onDiskResourceRoot()
	{
		return new File(GLOBALS.valueFor("repoDir")+"/seep-system/examples/acita_demo_2015/resources").getAbsolutePath();
	}
	
}
