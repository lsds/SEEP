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
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

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

import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class SEEPFaceRecognizer implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(SEEPFaceRecognizer.class);
	private int processed = 0;
	private PersonRecognizer personRecognizer = null;
	
	public void processData(DataTuple data) {
		long tProcessStart = System.currentTimeMillis();
		long tupleId = data.getLong("tupleId");
		byte[] value = data.getByteArray("value");
		int x = data.getInt("x");
		int y = data.getInt("y");
		int height = data.getInt("height");
		int width = data.getInt("width");
		
		int prediction = personRecognizer.recognize(value, x, y, height, width);
		
		DataTuple outputTuple = data.setValues(tupleId, value, 0, 0, 0, x, y, height, width);
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
		
		logger.info("Face recognizer processed "+width+"x"+height+" tuple in " + (System.currentTimeMillis() - tProcessStart) + "ms");
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
				logger.info("Face recognizer "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
			}
			else
			{
				logger.debug("Face recognizer "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
				recordTuple(outputTuple);
			}
		}
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
		//String trainingDir = "/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/training";
		String trainingDir = "training";
		String testImageFilename = "/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/resources/images/barack.jpg";
		personRecognizer = new PersonRecognizer(api.getOperatorId(), trainingDir, testImageFilename);
		//recognizer.testSample();
		//personRecognizer.testATT();
	}

	
	public static class PersonRecognizer
	{
		private final int opId;
		private final String trainingDir;
		private final String testImageFilename;
		private final FaceRecognizer faceRecognizer;
        private final Java2DFrameConverter frameConverter = new Java2DFrameConverter();
        private final OpenCVFrameConverter matConverter = new OpenCVFrameConverter.ToMat();
        private final OpenCVFrameConverter iplConverter = new OpenCVFrameConverter.ToIplImage();
		
		public PersonRecognizer(int opId, String trainingDir, String testImageFilename)
		{
			this.opId = opId;
			this.trainingDir = trainingDir;
			this.testImageFilename = testImageFilename;
			String[] imgFormats = ImageIO.getReaderFormatNames();
			logger.info("Image io supported image formats: ");
			for (int i = 0; i < imgFormats.length; i++) { logger.info(""+imgFormats[i]); }
			this.faceRecognizer = trainATT();
		}
		
		public int recognize(byte[] value, int x, int y, int width, int height)
		{
			if (x <= 0 && y <=0 && width <= 0 && height <= 0) { return -1; }
			IplImage img = parseBufferedImage(value);
			IplImage imgBW = prepareBWImage(img);
			cvSetImageROI(imgBW, cvRect(x, y, width, height));
			Mat imgBWMat = matConverter.convertToMat(iplConverter.convert(imgBW));
			int predictedLabel = faceRecognizer.predict(imgBWMat);
			logger.info("Predicted label for received image: " + predictedLabel);
			return predictedLabel;
		}
		
		public FaceRecognizer trainATT()
		{
			//File csv = new File(trainingDir+"/at.txt");
			logger.info("Loading training list from:"+trainingDir+"/at.txt");
			InputStream csv = this.getClass().getClassLoader().getResourceAsStream(trainingDir+"/at.txt");
			Map<String, Integer> trainingFiles = new HashMap<>();
			
			try
			{
				try (BufferedReader br = new BufferedReader(new InputStreamReader(csv))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				    	String[] values = line.split(",");
				    	trainingFiles.put(values[0], Integer.parseInt(values[1]));
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
				//Mat img = imread(imgFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
				Mat img = loadBWMatImage(filename);
				logger.info("Read training image from "+ filename);
				images.put(counter, img);
				int label = trainingFiles.get(filename);
				
				labelsBuf.put(counter, label);
				counter++;
			}
			
			//FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
			// FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
			FaceRecognizer lbphRecognizer = createLBPHFaceRecognizer();
	
			lbphRecognizer.train(images, labels);
			return lbphRecognizer;
		}
		
		public void testATT()
		{
			Mat testImage = imread(testImageFilename, CV_LOAD_IMAGE_GRAYSCALE);
			int predictedLabel = faceRecognizer.predict(testImage);
	
			logger.info("Predicted label for test image: " + predictedLabel);
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
				Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
	
				int label = Integer.parseInt(image.getName().split("\\-")[0]);
	
				images.put(counter, img);
	
				labelsBuf.put(counter, label);
	
				counter++;
			}
	
			//FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
			// FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
			FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();
	
			faceRecognizer.train(images, labels);
	
			Mat testImage = imread(testImageFilename, CV_LOAD_IMAGE_GRAYSCALE);
			int predictedLabel = faceRecognizer.predict(testImage);
	
			logger.info("Predicted label: " + predictedLabel);
		}
		
		public Mat loadBWMatImage(String filename)
		{
			try
			{
				String filepath = trainingDir+"/"+filename;
				logger.info("Loading training image from: "+ filepath);
				File tmpImgFile = new File("/tmp/resources/"+opId+filepath);
				tmpImgFile.deleteOnExit();
				tmpImgFile.mkdirs();
				InputStream fileInJar = this.getClass().getClassLoader().getResourceAsStream(filepath);
				Files.copy(fileInJar, tmpImgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				Mat img = imread(tmpImgFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
				return img;
				//BufferedImage bufImg = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(filepath));
				//logger.info("Loaded "+bufImg.getWidth()+"x"+bufImg.getHeight()+" training image");
				
				//IplImage img = iplConverter.convertToIplImage(frameConverter.convert(bufImg));
				//IplImage bwImg = prepareBWImage(img);
				//return matConverter.convertToMat(iplConverter.convert(bwImg));
			} catch (IOException e) { throw new RuntimeException(e); }
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
			IplImage imageBW = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
			cvCvtColor(image, imageBW, CV_BGR2GRAY);
			return imageBW;
		}
	}
}
