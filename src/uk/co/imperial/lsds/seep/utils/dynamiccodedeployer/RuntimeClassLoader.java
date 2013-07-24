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
package uk.co.imperial.lsds.seep.utils.dynamiccodedeployer;

import java.net.URL;
import java.net.URLClassLoader;

import uk.co.imperial.lsds.seep.infrastructure.NodeManager;

public class RuntimeClassLoader extends URLClassLoader{
	
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
				NodeManager.nLogger.severe("URL: "+u);
			}
			NodeManager.nLogger.severe("When trying to load: "+name);
			e.printStackTrace();
		}
		return null;
	}
}
