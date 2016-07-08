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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;
import uk.ac.imperial.lsds.seep.acita15.stats.Stats;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class VideoSink implements StatelessOperator {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Sink.class);
	private long numTuples;
	private long warmUpTuples;
	private long tupleSize;
	private long tuplesReceived = 0;
	private long totalBytes = 0;
	private final int displayPort = 20150;
	private final String displayAddr = "172.16.0.254";
	private final boolean enableSinkDisplay = Boolean.parseBoolean(GLOBALS.valueFor("enableSinkDisplay"));
	private Socket displaySocket = null;
	private ObjectOutputStream output = null;
	private Stats stats = null;
	private boolean recordImages = false;
    private Java2DFrameConverter frameConverter = null;
    private OpenCVFrameConverter iplConverter = null; 
	private boolean exitOnFinished = true;
    
	public void setUp() {
		logger.info("Setting up SINK operator with id="+api.getOperatorId());
		stats = new Stats(api.getOperatorId());
		numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
		warmUpTuples = Long.parseLong(GLOBALS.valueFor("warmUpTuples"));
		tupleSize = Long.parseLong(GLOBALS.valueFor("tupleSizeChars"));
		logger.info("SINK expecting "+numTuples+" tuples.");
		frameConverter = new Java2DFrameConverter();
		iplConverter = new OpenCVFrameConverter.ToIplImage();
		recordImages = Boolean.parseBoolean(GLOBALS.valueFor("recordImages"));
		connectToDisplay();
	}
	
	public void processData(DataTuple dt) {
		if (dt.getPayload().timestamp != dt.getLong("tupleId"))
		{
			throw new RuntimeException("Logic error: ts " + dt.getPayload().timestamp+ "!= tupleId "+dt.getLong("tupleId"));
		}

		if (dt.getLong("tupleId") < warmUpTuples) 
		{ 
			logger.debug("Ignoring warm up tuple "+dt.getLong("tupleId")); 
			return;
		}

		if (tuplesReceived == 0)
		{
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
		}
		
		tuplesReceived++;
		byte[] value = dt.getByteArray("value");
		totalBytes += value.length;
		recordTuple(dt, value.length);
		long tupleId = dt.getLong("tupleId");
		int[] bbox = new int[]{dt.getInt("x"), dt.getInt("y"), dt.getInt("height"), dt.getInt("width")};
		if (tupleId != warmUpTuples + tuplesReceived -1)
		{
			logger.info("SNK: Received tuple " + tuplesReceived + " out of order, id="+tupleId);
		}
		
		if (recordImages)
		{
			recordImage(tupleId, value, bbox);
		}
		
		displayImage(value, bbox, dt.getString("label"));
		
		if (tuplesReceived >= numTuples)
		{
			logger.info("SNK: FINISHED with total tuples="+tuplesReceived
					+",total bytes="+totalBytes
					+",t="+System.currentTimeMillis()
					+",tuple size bytes="+tupleSize);
			System.exit(0);
		}
		//stats.add(System.currentTimeMillis(), dt.getPayload().toString().length());
		stats.add(System.currentTimeMillis(), value.length);
		api.ack(dt);
	}
	
	private void recordTuple(DataTuple dt, int bytes)
	{
		long rxts = System.currentTimeMillis();
		logger.info("SNK: Received tuple with cnt="+tuplesReceived 
				+",id="+dt.getLong("tupleId")
				+",ts="+dt.getPayload().timestamp
				+",txts="+dt.getPayload().instrumentation_ts
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().instrumentation_ts)
				+",bytes="+ bytes
				+",latencyBreakdown=0;0");
	}
	
	public void processData(List<DataTuple> arg0) {
		throw new RuntimeException("TODO");
	}
	
	private void connectToDisplay()
	{
		if (!enableSinkDisplay) { return; }
		try
		{
			displaySocket = new Socket(displayAddr, displayPort);
			output = new ObjectOutputStream(displaySocket.getOutputStream());
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	//TODO: Do this in the background.
	private void displayImage(byte[] value, int[] bbox, String label)
	{	
		if (enableSinkDisplay)
		{
			
			BufferedImage img = bytesToBufferedImage(value, bbox);
			byte[] imgBytes = bufferedImageToBytes(img);
			
			try
			{
				output.writeObject(imgBytes);
				output.writeObject(label);
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	private void recordImage(long tupleId, byte[] bytes, int[] bbox)
	{
		//cvRectangle(bwImg, cvPoint(bbox[0], bbox[1]), cvPoint(bbox[2], bbox[3]), CvScalar.RED, 1, CV_AA, 0);
		//ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		BufferedImage outputImg = bytesToBufferedImage(bytes, bbox);
		try
		{
			//IplImage img = iplConverter.convertToIplImage(frameConverter.convert(ImageIO.read(bais)));
			//BufferedImage outputImg = frameConverter.convert(iplConverter.convert(img));
			//BufferedImage outputImg = ImageIO.read(bais);
			File outputFile = new File("imgout/detected/"+tupleId+".jpg");
			outputFile.mkdirs();
			ImageIO.write(outputImg, "jpg", outputFile);			
		}
		catch(IOException e) { throw new RuntimeException(e); }
	}
	
	private BufferedImage bytesToBufferedImage(byte[] value, int[] bbox)
	{
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(value);
			IplImage img = iplConverter.convertToIplImage(frameConverter.convert(ImageIO.read(bais)));
			if (bbox != null)
			{
				cvRectangle(img, cvPoint(bbox[0], bbox[1]), cvPoint(bbox[2], bbox[3]), CvScalar.RED, 1, CV_AA, 0);
			}
			BufferedImage outputImg = frameConverter.convert(iplConverter.convert(img));
			return outputImg;
		}
		catch(IOException e) { throw new RuntimeException(e); }
	}
		
	private byte[] bufferedImageToBytes(BufferedImage img)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "jpg", baos);
			baos.flush();
			byte[] bytes = baos.toByteArray();
			baos.close();
			return bytes;
		}
		catch(IOException e) { throw new RuntimeException(e); }
	}
}
