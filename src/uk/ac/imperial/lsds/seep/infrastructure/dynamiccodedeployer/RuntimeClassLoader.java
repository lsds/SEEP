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
package uk.ac.imperial.lsds.seep.infrastructure.dynamiccodedeployer;

import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;

public class RuntimeClassLoader extends URLClassLoader{
	
	final private Logger LOG = LoggerFactory.getLogger(RuntimeClassLoader.class);
	
	public RuntimeClassLoader(URL[] urls, ClassLoader cl) {
		super(urls, cl);
	}
	
	public void addURL(URL url){
		super.addURL(url);
	}
	
	public Class<?> loadClass(String name){
		try {
			return super.loadClass(name);
//			return Class.forName(name);
		} 
		catch (ClassNotFoundException e) {
			URL[] loadURLs = super.getURLs();
			for(URL u : loadURLs){
				LOG.info("This URL: "+u);
			}
			LOG.info("When trying to load: "+name);
			try {
				return Class.forName(name);
			} 
			catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				LOG.info("MSG: "+e1.getMessage());
				e1.printStackTrace();
			}
			//e.printStackTrace();
		}
		return null;
	}
}
