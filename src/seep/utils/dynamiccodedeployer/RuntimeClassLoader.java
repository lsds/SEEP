package seep.utils.dynamiccodedeployer;

import java.net.URL;
import java.net.URLClassLoader;

import seep.infrastructure.NodeManager;

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
