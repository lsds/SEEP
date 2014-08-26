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

import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class BackupSessionInfo {
	private int opId;
	private ArrayList<FileChannel> lastBackupHandlers;
	private BackupHandler owner;
	private String sessionName;
	private int transNumber;
	private boolean work = true;
	
	public BackupSessionInfo(int opId,
			ArrayList<FileChannel> lastBackupHandlers,
			BackupHandler backupHandler, String sessionName, int transNumber) {
		this.opId = opId;
		this.lastBackupHandlers = lastBackupHandlers;
		this.owner = backupHandler;
		this.sessionName = sessionName;
		this.transNumber = transNumber;
	}
	
	public int getOpId(){
		return opId;
	}
	
	public String getSessionName(){
		return sessionName;
	}
	
	public int getTransNumber(){
		return transNumber;
	}
	
	public void incrementTransNumber(){
		transNumber++;
	}
}
