/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GLOBALS {
	
	private final static Logger LOG = LoggerFactory.getLogger(GLOBALS.class);
	private static Properties globals = null;
	
	static{
		globals = new Properties();
		LOG.debug("Loading GLOBALS...");
		GLOBALS.loadProperties();
		LOG.debug("Loading GLOBALS...Done");
		LOG.info("java.library.path="+System.getProperty("java.library.path"));
	}
	
	private GLOBALS(){}

	//Method to get value doing: Main.valueFor(key) instead of Main.globals.getProperty(key)
	public static String valueFor(String key){
		return globals.getProperty(key);
	}
	
	public static String propsToString()
	{
		return globals.toString();
	}
	
	//Load properties from file
	private static void loadProperties(){
		try {
			InputStream fis = (InputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
			globals.load(fis);
			globals.put("useCoreAddr", System.getProperty("useCoreAddr", ""));
			globals.put("replicationFactor", System.getProperty("replicationFactor", "1"));	//TODO: Bit of a hack.
			globals.put("chainLength", System.getProperty("chainLength", "1"));
			globals.put("queryType", System.getProperty("queryType", "chain"));
			globals.put("sources", System.getProperty("sources", "1"));
			globals.put("sinks", System.getProperty("sinks", "1"));
			globals.put("fanin", System.getProperty("fanin", "2"));
			globals.put("sinkScaleFactor", System.getProperty("sinkScaleFactor", "1"));
			
			globals.load(new FileReader("../session_params.txt"));
			LOG.info("Loaded global properties="+propsToString());
		}
		catch (FileNotFoundException e1) {
			System.out.println("Properties file not found "+e1.getMessage());
			e1.printStackTrace();
		}
		catch (IOException e1) {
			System.out.println("While loading properties file "+e1.getMessage());
			e1.printStackTrace();
		}
	}
}
