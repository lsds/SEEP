package uk.ac.imperial.lsds.seepmaster;

import java.io.FileNotFoundException;
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
	}
	
	private GLOBALS(){}

	//Method to get value doing: Main.valueFor(key) instead of Main.globals.getProperty(key)
	public static String valueFor(String key){
		return globals.getProperty(key);
	}
	
	//Load properties from file
	private static void loadProperties(){
		try {
			InputStream fis = (InputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
			globals.load(fis);
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
