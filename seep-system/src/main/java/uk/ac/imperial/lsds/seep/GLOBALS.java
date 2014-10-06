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
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
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
	}
	
	private GLOBALS(){}

	//Method to get value doing: Main.valueFor(key) instead of Main.globals.getProperty(key)
	public static String valueFor(String key){
		return globals.getProperty(key);
	}
	
	//Load properties from file
	private static void loadProperties(){
		try {
			InputStream fis;
			String filename = "/mnt/data/cccad3/akolious/SEEP3/seep-system/src/main/resources/config.properties";
			if (new File(filename).isFile()) {
				LOG.debug("Loading properties from " + filename);
				fis = (InputStream) new FileInputStream(filename);
				globals.load(fis);
			} else {
				fis = (InputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
				globals.load(fis);
			}
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
