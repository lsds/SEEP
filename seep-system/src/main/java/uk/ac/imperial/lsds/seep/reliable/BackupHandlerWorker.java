/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.reliable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupHandlerWorker implements Runnable{
	
	final private Logger LOG = LoggerFactory.getLogger(BackupHandlerWorker.class);

	private int opId = -1;
	private Socket incomingSocket = null;
	private BackupHandler owner = null;
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
		try {		
			bis = new BufferedInputStream(incomingSocket.getInputStream());
			// Create the memory map file and store the channels to manage these files later
			RandomAccessFile raf = null;
			String fileName = null;
			try {
				// file format: OP_X_Y_Z.bk, where X is the opId, Y the sessionName and Z the sequence number
				// so, OP_1_a_0.bk and OP_1_a_1.bk are consecutive files but OP_1_a_1.bk and OP_2_a_2.bk are not (different ops).
				fileName = "backup/OP_"+opId+"_"+sessionName+"_"+this.transNumber+".bk";
				raf = new RandomAccessFile(fileName, "rw");
				
				FileChannel fc = raf.getChannel();
				File f = new File(fileName);
				
				mbb1 = fc.map(FileChannel.MapMode.READ_WRITE, 0, 10000000);
				owner.addBackupHandler(opId, fc, f);
			}
			catch(Exception e){
				LOG.error("-> While writing bk: "+fileName+" to disk: "+e.getMessage());
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
			LOG.error("-> While managing backup chunk = "+e.getMessage());
			e.printStackTrace();
		}
	}
}
