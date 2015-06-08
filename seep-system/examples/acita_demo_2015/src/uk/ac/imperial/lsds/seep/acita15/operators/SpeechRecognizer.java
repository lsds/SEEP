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
import java.io.InputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import edu.cmu.sphinx.result.WordResult;

import uk.ac.imperial.lsds.seep.comm.serialization.DataTuple;
import uk.ac.imperial.lsds.seep.operator.StatelessOperator;

public class SpeechRecognizer implements StatelessOperator{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(SpeechRecognizer.class);
	private int processed = 0;
	//private Configuration configuration = null;
	//private StreamSpeechRecognizer recognizer = null;
	
	public void processData(DataTuple data) {
		long tupleId = data.getLong("tupleId");
		String value = data.getString("value") + "," + api.getOperatorId();
		
		DataTuple outputTuple = data.setValues(tupleId, value);
		processed++;
		if (processed % 1000 == 0)
		{
			logger.info("Speech recognizer "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
		}
		else
		{
			logger.debug("Speech recognizer "+api.getOperatorId()+ " processed "+data.getLong("tupleId")+"->"+outputTuple.getLong("tupleId"));
			if (logger.isDebugEnabled())
			{
				recordTuple(outputTuple);
			}
		}
		
		long tRecStart = System.currentTimeMillis();
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
			InputStream stream = SpeechRecognizer.class
					.getResourceAsStream("/edu/cmu/sphinx/demo/aligner/10001-90210-01803.wav");
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
				System.out.println("Best 3 hypothesis:");
				for (String s : result.getNbest(3))
					System.out.println(s);
			}
			recognizer.stopRecognition();
		} catch(IOException e) 
		{
			logger.error("Problem with speech recognition: ", e);
			System.exit(1);
		}
		
		long tRecEnd = System.currentTimeMillis();
		logger.info("Recognized speech in "+ (tRecEnd - tRecStart)+ " ms");
		
		api.send_highestWeight(outputTuple);
	}

	
	public void processData(List<DataTuple> arg0) {
		throw new RuntimeException("Ignore"); 
	}

	private void recordTuple(DataTuple dt)
	{
		long rxts = System.currentTimeMillis();
		logger.debug("SPEECH_RECOGNIZER: "+api.getOperatorId()+" received tuple with id="+dt.getLong("tupleId")
				+",ts="+dt.getPayload().timestamp
				+",txts="+dt.getPayload().instrumentation_ts
				+",rxts="+rxts
				+",latency="+ (rxts - dt.getPayload().instrumentation_ts));
	}
	
	public void setUp() {
		System.out.println("Setting up SPEECH RECOGNIZER operator with id="+api.getOperatorId());
	}

}
