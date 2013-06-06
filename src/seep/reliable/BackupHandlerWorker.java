package seep.reliable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import seep.infrastructure.NodeManager;
import seep.runtimeengine.CoreRE;

public class BackupHandlerWorker implements Runnable{

	private Socket incomingSocket = null;
	private CoreRE owner = null;
	private boolean goOn = true;
	private String fileName = null;
	private File backupFile = null;
	
	private PipedOutputStream pos = null;
	private PipedInputStream pis = null;
	private PipeReader pr = null;
	
	private Thread pipeRH = null;
	
	private MappedByteBuffer mbb1;
	
	public BackupHandlerWorker(Socket incomingSocket, CoreRE owner, String fileName) {
		this.incomingSocket = incomingSocket;
		this.owner = owner;
		// Create pipedStreams
		this.pos = new PipedOutputStream();
		this.pis = new PipedInputStream(10000000);
		this.fileName = fileName;
		
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile("memorymaptest.dat", "rw");
		
			FileChannel fc = raf.getChannel();
		
			mbb1 = fc.map(FileChannel.MapMode.READ_WRITE, 0, 1000000000);
		}
		catch(Exception e){
			
		}
		
	}
	
	public void init(long ts){
		try {
			// Create backupFile
			if(fileName != null){
				backupFile = new File(fileName);
			}
			else{
				fileName = "_backup_"+incomingSocket.getInetAddress().getHostAddress();
				backupFile = new File(fileName);
			}
			// Connect pipes
			pis.connect(pos);
			
			pr = new PipeReader(pis, backupFile, ts);
			pipeRH = new Thread(pr);
			pipeRH.start();
		} 
		catch (IOException e) {
			NodeManager.nLogger.severe("While connecting PIPE: "+e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		NodeManager.nLogger.info("New Backup received");
//		pipedWrite();
		memoryMappedFile();
//		noop();
		long stop = System.currentTimeMillis();
		System.out.println("TOTAL TIME TO WRITE TO DISK: "+(stop-start)+" ms");
	}
	
	
	
	
	public void noop(){
		byte[] buffer = new byte[10000000];
		System.out.println("PELOTAS");
		int bytesRead = 0;
		BufferedInputStream bis;
		try {
			int total = 0;
			bis = new BufferedInputStream(incomingSocket.getInputStream());
			 while ((bytesRead = bis.read(buffer)) != -1) {
		            total += bytesRead;
		     }
			 System.out.println("Total read bytes: "+total);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
	}
	
	public void pipedWrite(){
		this.init(System.currentTimeMillis());
//		while(goOn){
			byte[] buffer = new byte[10000000];
			
			try {
				BufferedInputStream bis = new BufferedInputStream(incomingSocket.getInputStream());
				int bytesRead = 0;
	            while ((bytesRead = bis.read(buffer)) != -1) {
	                pos.write(buffer, 0, bytesRead);
	            }
	            pos.flush();
	            pos.close();
	            bis.close();
	            incomingSocket.close();
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		}
	}
	
	public void memoryMappedFile(){
			BufferedInputStream bis;
			try {
				bis = new BufferedInputStream(incomingSocket.getInputStream());
				int bytesRead = 0;
				byte[] buffer = new byte[10000000];
				while ((bytesRead = bis.read(buffer)) != -1) {
					//pos.write(buffer, 0, bytesRead);
            		mbb1.put(buffer, 0, bytesRead);
           	 	}
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
