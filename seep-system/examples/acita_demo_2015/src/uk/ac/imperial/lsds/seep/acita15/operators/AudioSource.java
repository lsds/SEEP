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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import edu.cmu.sphinx.result.WordResult;


import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.comm.serialization.messages.TuplePayload;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;


public class AudioSource implements StatelessOperator {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(AudioSource.class);
	private static String[] audioFiles = new String[]{"my_name_is_dan.wav", "my_name_is_peter.wav", "my_name_is_theodoros.wav", "my_name_is_bob.wav", "testing_one_two.wav"};
	private static final String audioDir = "/home/dan/dev/seep-ita/seep-system/examples/acita_demo_2015/core-emane/resources";
	private int audioIndex = 0;
	
	public void setUp() {
		System.out.println("Setting up AUDIO_SOURCE operator with id="+api.getOperatorId());
		
	}

	public void processData(DataTuple dt) {
		Map<String, Integer> mapper = api.getDataMapper();
		DataTuple data = new DataTuple(mapper, new TuplePayload());
	
		testSpeechRec(audioIndex);
		
		byte[] audioClip = null;
		//InputStream stream = new FileInputStream(audio);
		//InputStream stream = GLOBALS.class
				//.getResourceAsStream("10001-90210-01803.wav");
				//.getResourceAsStream("/edu/cmu/sphinx/demo/aligner/10001-90210-01803.wav");
		
		long tupleId = 0;
		
		boolean sendIndefinitely = Boolean.parseBoolean(GLOBALS.valueFor("sendIndefinitely"));
		long numTuples = Long.parseLong(GLOBALS.valueFor("numTuples"));
		int tupleSizeChars = Integer.parseInt(GLOBALS.valueFor("tupleSizeChars"));
		boolean rateLimitSrc = Boolean.parseBoolean(GLOBALS.valueFor("rateLimitSrc"));
		long frameRate = Long.parseLong(GLOBALS.valueFor("frameRate"));
		long interFrameDelay = 1000 / frameRate;
		logger.info("Source inter-frame delay="+interFrameDelay);
		
		//final String value = generateFrame(tupleSizeChars);
		final long tStart = System.currentTimeMillis();
		logger.info("Source sending started at t="+tStart);
		logger.info("Source sending started at t="+tStart);
		logger.info("Source sending started at t="+tStart);
		while(sendIndefinitely || tupleId < numTuples){
			try
			{
				audioClip = Files.readAllBytes(Paths.get(audioDir + "/" + audioFiles[audioIndex]));
				audioIndex = (audioIndex + 1) % audioFiles.length;
			} catch(IOException e)
			{
				logger.error("Problem opening audio file: ", e);
				System.exit(1);
			}
			
			DataTuple output = data.newTuple(tupleId, audioClip);
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
	
	private void testSpeechRec(int audioIndex)
	{
		Configuration configuration = new Configuration();
		// Load model from the jar
		configuration
		.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		// You can also load model from folder
		// configuration.setAcousticModelPath("file:en-us");
		configuration
		.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration
		.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.dmp");
		
		try
		{
			StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(
					configuration);
			File audio = new File(audioDir + "/" + audioFiles[audioIndex]);
			InputStream stream = new FileInputStream(audio);
			//InputStream stream = new ByteArrayInputStream(audioClip);
					//.getResourceAsStream("/edu/cmu/sphinx/demo/aligner/10001-90210-01803.wav");
					
					
			stream.skip(44);
			// Simple recognition with generic model
			recognizer.startRecognition(stream);
			SpeechResult result;
			while ((result = recognizer.getResult()) != null) {
				System.out.format("Hypothesis: %s\n", result.getHypothesis());
				System.out.println("List of recognized words and their times:");
				for (WordResult r : result.getWords()) {
					System.out.println(r);
				}
				System.out.println("Best 4 hypothesis:");
				for (String s : result.getNbest(4))
				{
					System.out.println(s);
				}
			}
			recognizer.stopRecognition();
		} catch(IOException e) 
		{
			logger.error("Problem with speech recognition: ", e);
			System.exit(1);
		}
	}
}
