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

import java.io.File;
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
			
			globals.put("useCoreAddr", globals.getProperty("useCoreAddr", ""));
			globals.put("net-routing", globals.getProperty("net-routing", "OLSRETX"));
			globals.put("replicationFactor", System.getProperty("replicationFactor", globals.getProperty("replicationFactor", "1")));
			globals.put("chainLength", globals.getProperty("chainLength", "1"));
			globals.put("queryType", globals.getProperty("queryType", "chain"));
			globals.put("sources", globals.getProperty("sources", "1"));
			globals.put("sinks", globals.getProperty("sinks", "1"));
			globals.put("fanin", globals.getProperty("fanin", "2"));
			globals.put("sinkScaleFactor", globals.getProperty("sinkScaleFactor", "1"));

			String sessionParamsPath = "../session_params.txt";
			File f = new File(sessionParamsPath);	
			if (f.exists() && !f.isDirectory())
			{
				LOG.info("Loading session props from: " + sessionParamsPath); 
				globals.load(new FileReader(sessionParamsPath));
			}

			String extraPropsPath = System.getProperty("extraProps");
			if (extraPropsPath == null) { extraPropsPath = globals.getProperty("extraProps"); }
			if (extraPropsPath != null)
			{
				f = new File(extraPropsPath);	
				if (f.exists() && !f.isDirectory())
				{
					LOG.info("Loading extra props from: " + extraPropsPath); 
					globals.load(new FileReader(extraPropsPath));
				}
			}

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
