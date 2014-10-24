package uk.ac.imperial.lsds.seep.infrastructure;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		} 
		catch (ClassNotFoundException e) {
			URL[] loadURLs = super.getURLs();
			for(URL u : loadURLs){
				LOG.error("This URL: "+u);
			}
			LOG.error("When trying to load: "+name);
			try {
				return Class.forName(name);
			} 
			catch (ClassNotFoundException e1) {
				LOG.error("MSG: "+e1.getMessage());
				e1.printStackTrace();
			}
		}
		return null;
	}
}