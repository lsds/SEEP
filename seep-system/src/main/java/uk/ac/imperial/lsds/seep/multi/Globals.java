package uk.ac.imperial.lsds.seep.multi;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.FileInputStream;
import java.io.InputStream;

import java.io.File;
import java.util.Properties;

public class Globals {
	
	private static Properties globals = null;
	static
	{
		globals = new Properties ();
		System.out.println("Loading globals...");
		Globals.loadProperties ();
		System.out.println("OK");
	}
	
	private Globals () {
	}
	
	public static String valueFor (String key) {
		
		return globals.getProperty(key);
	}
	
	private static void loadProperties () {
		
		try 
		{
			InputStream input;
			String filename = "/home/akolious/SEEP/seep-system/src/main/resources/multioperator.properties";
			if (new File(filename).isFile()) 
			{
				System.out.println(filename);
				input = (InputStream) new FileInputStream (filename);
				globals.load (input);
			} 
			else
			{
				input = (InputStream) 
						Thread.currentThread().getContextClassLoader().getResourceAsStream("multioperator.properties");
				globals.load(input);
			}
		}
		catch (FileNotFoundException e) 
		{
			System.err.println("error: file not found");
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			System.err.println("error: cannot load file");
			e.printStackTrace();
		}
	}
}
