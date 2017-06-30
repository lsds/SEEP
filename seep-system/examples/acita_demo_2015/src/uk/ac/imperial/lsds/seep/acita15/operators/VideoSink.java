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
import uk.ac.imperial.lsds.seep.manet.stats.Stats;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import uk.ac.imperial.lsds.seep.acita15.reorder.ReorderBuffer;

public class VideoSink implements StatelessOperator {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Sink.class);
	private long numTuples;
	private long warmUpTuples;
	private long tupleSize;
	private long tStart;
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
	private ReorderBuffer reorderBuffer = null;
	private boolean reorder = Boolean.parseBoolean(GLOBALS.valueFor("reorderImages"));
	private final String targetMatch = "chokepoint/P2E_S1_C3/0024";
	private int matches = 0;
    
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
			api.ack(dt);
			return;
		}

		if (tuplesReceived == 0)
		{
			tStart = System.currentTimeMillis();
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
			System.out.println("SNK: Received initial tuple at t="+System.currentTimeMillis());
		}
		
		tuplesReceived++;
		byte[] value = dt.getByteArray("value");
		totalBytes += value.length;
		recordTuple(dt, value.length);
		long tupleId = dt.getLong("tupleId");
		int[] bbox = new int[]{dt.getInt("x"), dt.getInt("y"), dt.getInt("x2"), dt.getInt("y2")};
		String label = dt.getString("label");
		boolean match = label.startsWith(targetMatch);
		logger.info("Label for image: "+label + ", match="+match);
		if (match) { matches++; }

		if (tupleId != warmUpTuples + tuplesReceived -1)
		{
			logger.info("SNK: Received tuple " + tuplesReceived + " out of order, id="+tupleId);
		}
		
		if (recordImages)
		{
			recordImage(tupleId, value, bbox);
		}
		
		displayImage(tupleId, value, bbox, dt.getString("label"));
		
		if (tuplesReceived >= numTuples)
		{
			logger.info("SNK: FINISHED with total tuples="+tuplesReceived
					+",total bytes="+totalBytes
					+",t="+System.currentTimeMillis()
					+",tuple size bytes="+tupleSize);
			long duration = System.currentTimeMillis() - tStart;
			double kbTput = ((8 * totalBytes) / 1024.0) / (duration / 1000.0);
			double framesTput = tuplesReceived / (duration / 1000.0); 
			logger.info("SNK: TPUT=" + kbTput + " Kb/s, "+ framesTput + " frames/s");
			double matchRate = (100.0 * matches) / tuplesReceived;
			logger.info("SNK: MATCHES=" + matches + " / " + tuplesReceived + "(" +matchRate+ ")");
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

		if (reorder) { reorderBuffer = new ReorderBuffer(); }
	}
	
	//TODO: Do this in the background.
	private void displayImage(long tupleId, byte[] value, int[] bbox, String label)
	{	
		if (Boolean.parseBoolean(GLOBALS.valueFor("loadIplImages"))) { throw new RuntimeException("TODO"); }
		if (enableSinkDisplay)
		{
			if (reorder)
			{
				//N.B. Reorder buffer won't work with replicated sinks!
				reorderBuffer.add(tupleId, new VideoEntry(value, bbox, label));
				for (Object o : reorderBuffer.flush())
				{
					VideoEntry entry = (VideoEntry)o;
					sendImage(entry.value, entry.bbox, entry.label);
				}
			}
			else
			{
				sendImage(value, bbox, label);
			}
		}
	}



	private void sendImage(byte[] value, int[] bbox, String label)
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
	
	private void recordImage(long tupleId, byte[] bytes, int[] bbox)
	{
		if (Boolean.parseBoolean(GLOBALS.valueFor("loadIplImages"))) { throw new RuntimeException("TODO"); }
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

	private static class VideoEntry
	{
		public final byte[] value; 
		public final int[] bbox;
		public final String label;

		public VideoEntry(byte[] value, int[] bbox, String label)
		{
			this.value = value;
			this.bbox = bbox;
			this.label = label;
		}
	}

}
