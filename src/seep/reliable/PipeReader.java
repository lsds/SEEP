package seep.reliable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;

import seep.infrastructure.NodeManager;

public class PipeReader implements Runnable{

	private PipedInputStream pis = null;
	private boolean goOn = true;
	private File backupFile = null;
	
	private BufferedOutputStream fos = null;
	
	private long ts;
	
	public PipeReader(PipedInputStream pis, File backupFile, long ts){
		this.pis = pis;
		this.backupFile = backupFile;
		this.ts = ts;
	}
	
	@Override
	public void run() {
		byte[] buffer = new byte[10000000];
		int bytesRead = 0;
		int totalBytesWritten = 0;
		try {
			fos = new BufferedOutputStream(new FileOutputStream(backupFile), 5000000);
		
//			while(goOn){
				while ((bytesRead = pis.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
					totalBytesWritten += bytesRead;
				}
				NodeManager.nLogger.info(totalBytesWritten+" bytes written in: "+(System.currentTimeMillis() - ts)+" ms");
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
