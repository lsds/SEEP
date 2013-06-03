package seep.reliable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;

public class PipeReader implements Runnable{

	private PipedInputStream pis = null;
	private boolean goOn = true;
	private File backupFile = null;
	
	private BufferedOutputStream fos = null;
	
	public PipeReader(PipedInputStream pis, File backupFile){
		this.pis = pis;
		this.backupFile = backupFile;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		try {
			fos = new BufferedOutputStream(new FileOutputStream(backupFile));
		
//			while(goOn){
				while ((bytesRead = pis.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}
				fos.flush();
				fos.close();
				pis.close();
//			}
		
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
