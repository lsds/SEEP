package seep.reliable;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class BackupHandlerWorker implements Runnable{

	private int opId = -1;
	private Socket incomingSocket = null;
	private BackupHandler owner = null;
	private boolean goOn = true;
	private String sessionName = null;
	private int transNumber = -1;

	
	private MappedByteBuffer mbb1;
	
	public BackupHandlerWorker(int opId, Socket incomingSocket, BackupHandler owner, String sessionName, int transNumber) {
		this.opId = opId;
		this.incomingSocket = incomingSocket;
		this.owner = owner;
		this.sessionName = sessionName;
		this.transNumber = transNumber;
	}

	@Override
	public void run() {
		memoryMappedFile();
	}
	
	public void memoryMappedFile(){
		BufferedInputStream bis;
		DataInputStream dis;
		try {
			// Read the partition number
			dis = new DataInputStream(incomingSocket.getInputStream());
			int numPartition = dis.readInt();
			
			bis = new BufferedInputStream(incomingSocket.getInputStream());
			// Create the memory map file
			RandomAccessFile raf = null;
			try {
				// file format: PX_Y_Z.bk, where X is the partition number, Y the sessionName and Z the sequence number
				// so, P1_a_0.bk and P1_a_1.bk are consecutive files but P1_a_1.bk and P2_a_2.bk are not (different partitions).
				String fileName = "backup/P"+numPartition+"_"+sessionName+"_"+this.transNumber+".bk";
//				String fileName = sessionName+"_"+this.transNumber+".bk";
				raf = new RandomAccessFile(fileName, "rw");
				
				FileChannel fc = raf.getChannel();
				File f = new File(fileName);
				
				mbb1 = fc.map(FileChannel.MapMode.READ_WRITE, 0, 1000000000);
				owner.addBackupHandler(opId, fc, f);
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
}
