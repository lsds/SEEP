package uk.ac.imperial.lsds.seep.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Utils {

	final static private Logger LOG = LoggerFactory.getLogger(Utils.class);
	
	public static byte[] readDataFromFile(String path){
		FileInputStream fis = null;
		long fileSize = 0;
		byte[] data = null;
		try {
			//Open stream to file
			LOG.debug("Opening stream to file: {}", path);
			File f = new File(path);
			fis = new FileInputStream(f);
			fileSize = f.length();
			//Read file data
			data = new byte[(int)fileSize];
			int readBytesFromFile = fis.read(data);
			//Check if we have read correctly
			if(readBytesFromFile != fileSize){
				LOG.warn("Mismatch between read bytes and file size");
			}
			//Close the stream
			fis.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				fis.close();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return data;
	}
	
}
