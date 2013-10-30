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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class P {
	//Properties object
	private static Properties globals = new Properties();
	
	//Method to get value doing: Main.valueFor(key) instead of Main.globals.getProperty(key)
	public static String valueFor(String key){
		return globals.getProperty(key);
	}
	
	//Load properties from file
	public boolean loadProperties(){
		
		boolean success = false;
		File f = null;
		try {
			InputStream fis = (InputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
			globals.load(fis);
			success = true;
		}
		catch (FileNotFoundException e1) {
			System.out.println("Properties file not found "+e1.getMessage()+" SEARCH PATH: "+f.getAbsolutePath());
			e1.printStackTrace();
		}
		catch (IOException e1) {
			System.out.println("While loading properties file "+e1.getMessage());
			e1.printStackTrace();
		}
		return success;
	}
}
