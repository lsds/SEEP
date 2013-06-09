package seep.reliable;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import seep.infrastructure.NodeManager;
import seep.runtimeengine.CoreRE;

public class BackupHandlerWorker implements Runnable{

	private Socket incomingSocket = null;
	private BackupHandler owner = null;
	private boolean goOn = true;
	private String sessionName = null;
	private int transNumber = -1;
		
//	private File backupFile = null;
	
//	private PipedOutputStream pos = null;
//	private PipedInputStream pis = null;
//	private PipeReader pr = null;
//	
//	private Thread pipeRH = null;
	
	private MappedByteBuffer mbb1;
	
	public BackupHandlerWorker(Socket incomingSocket, BackupHandler owner, String sessionName, int transNumber) {
		this.incomingSocket = incomingSocket;
		this.owner = owner;
		this.sessionName = sessionName;
		this.transNumber = transNumber;
		// Create pipedStreams
//		this.pos = new PipedOutputStream();
//		this.pis = new PipedInputStream(10000);
//		this.fileName = fileName;
	}

	@Override
	public void run() {
//		pipedWrite();
		memoryMappedFile();
//		noop();
	}
	
	public void memoryMappedFile(){
		BufferedInputStream bis;
		DataInputStream dis;
		try {
			bis = new BufferedInputStream(incomingSocket.getInputStream());
			
			// Read the partition number
//			dis = new DataInputStream(incomingSocket.getInputStream());
//			int numPartition = dis.readInt();
			// Create the memory map file
			RandomAccessFile raf = null;
			try {
				// file format: PX_Y_Z.bk, where X is the partition number, Y the sessionName and Z the sequence number
				// so, P1_a_0.bk and P1_a_1.bk are consecutive files but P1_a_1.bk and P2_a_2.bk are not (different partitions).
//				String fileName = "P"+numPartition+"_"+sessionName+"_"+this.transNumber+".bk";
				String fileName = sessionName+"_"+this.transNumber+".bk";
				raf = new RandomAccessFile(fileName, "rw");

				FileChannel fc = raf.getChannel();

				mbb1 = fc.map(FileChannel.MapMode.READ_WRITE, 0, 1000000000);
				owner.addBackupHandler(mbb1);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			// Read the raw data and map to file
			int bytesRead = 0;
			byte[] buffer = new byte[10000];
			
			while ((bytesRead = bis.read(buffer)) != -1) {
        		mbb1.put(buffer, 0, bytesRead);
       	 	}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public void init(long ts){
//	try {
//		// Create backupFile
//		if(fileName != null){
//			backupFile = new File(fileName);
//		}
//		else{
//			fileName = "_backup_"+incomingSocket.getInetAddress().getHostAddress();
//			backupFile = new File(fileName);
//		}
//		// Connect pipes
//		pis.connect(pos);
//		
//		pr = new PipeReader(pis, backupFile, ts);
//		pipeRH = new Thread(pr);
//		pipeRH.start();
//	} 
//	catch (IOException e) {
//		NodeManager.nLogger.severe("While connecting PIPE: "+e.getMessage());
//		e.printStackTrace();
//	}
//}
	
//	public void noop(){
//		byte[] buffer = new byte[10000];
//		System.out.println("PELOTAS");
//		int bytesRead = 0;
//		BufferedInputStream bis;
//		try {
//			int total = 0;
//			bis = new BufferedInputStream(incomingSocket.getInputStream());
//			 while ((bytesRead = bis.read(buf)fer)) != -1) {
//		            total += bytesRead;
//		     }
//			 System.out.println("Total read bytes: "+total);
//		} 
//		catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//       
//	}
	
//	public void pipedWrite(){
//		this.init(System.currentTimeMillis());
////		while(goOn){
//			byte[] buffer = new byte[10000];
//			
//			try {
//				BufferedInputStream bis = new BufferedInputStream(incomingSocket.getInputStream());
//				int bytesRead = 0;
//	            while ((bytesRead = bis.read(buffer)) != -1) {
//	                pos.write(buffer, 0, bytesRead);
//	            }
//	            pos.flush();
//	            pos.close();
//	            bis.close();
//	            incomingSocket.close();
//			}
//			catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
////		}
//	}
}
