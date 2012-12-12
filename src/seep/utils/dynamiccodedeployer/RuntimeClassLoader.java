package seep.utils.dynamiccodedeployer;

import java.net.URL;
import java.net.URLClassLoader;

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
		} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
