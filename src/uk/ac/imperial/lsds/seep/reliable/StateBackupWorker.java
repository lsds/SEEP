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

import java.io.Serializable;

import uk.ac.imperial.lsds.seep.P;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.operator.State;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;

/**
* StateBackupWorker. This class is in charge of checking when the associated operator has a state to do backup and doing the backup of such state. This is operator dependant.
*/

public class StateBackupWorker implements Runnable, Serializable{

	private static final long serialVersionUID = 1L;
	
	private long initTime = 0;
	
	private StatefulProcessingUnit processingUnit;
	private boolean goOn = true;
	private int checkpointInterval = 0;
	private State state;
	
	private final int CHECKPOINTMODE;
	
	public void stop(){
		this.goOn = false;
	}

	public StateBackupWorker(StatefulProcessingUnit processingUnit, State s){
		this.processingUnit = processingUnit;
		this.state = s;
		if(P.valueFor("checkpointMode").equals("large-state")){
			this.CHECKPOINTMODE = 0;
		}
		else if(P.valueFor("checkpointMode").equals("light-state")){
			this.CHECKPOINTMODE = 1;
		}
		else{
			// safe default
			this.CHECKPOINTMODE = 0;
		}
	}

	public void run(){
		initTime = System.currentTimeMillis();
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if(CHECKPOINTMODE == 0){
			executeLargeStateMechanism();
		}
		else if(CHECKPOINTMODE == 1){
			executeLightStateMechanism();
		}
		else{
			NodeManager.nLogger.severe("-> Not defined checkpoint mode");
		}
	}
	
	public void executeLightStateMechanism(){
		processingUnit.checkpointAndBackupState();
		checkpointInterval = state.getCheckpointInterval();
		while(goOn){
			long elapsedTime = System.currentTimeMillis() - initTime;
			if(elapsedTime > checkpointInterval){
				//synch this call
				if(P.valueFor("eftMechanismEnabled").equals("true")){
					//if not initialisin state...
					if(!processingUnit.getSystemStatus().equals(StatefulProcessingUnit.SystemStatus.INITIALISING_STATE)){
						long startCheckpoint = System.currentTimeMillis();
						
						// Blocking call
						processingUnit.checkpointAndBackupState();

						long stopCheckpoint = System.currentTimeMillis();
						System.out.println("%% Total Checkpoint: "+(stopCheckpoint-startCheckpoint));
					}
				}
				initTime = System.currentTimeMillis();
			}
			else{
				try {
					int sleep = (int) (checkpointInterval - (System.currentTimeMillis() - initTime));
					if(sleep > 0){
						Thread.sleep(sleep);
					}
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void executeLargeStateMechanism(){
		processingUnit.lockFreeParallelCheckpointAndBackupState();
		checkpointInterval = state.getCheckpointInterval();
		while(goOn){
			long elapsedTime = System.currentTimeMillis() - initTime;
			if(elapsedTime > checkpointInterval){
				//synch this call
				if(P.valueFor("eftMechanismEnabled").equals("true")){
					//if not initialisin state...
					if(!processingUnit.getSystemStatus().equals(StatefulProcessingUnit.SystemStatus.INITIALISING_STATE)){
						long startCheckpoint = System.currentTimeMillis();
						
						// Blocking call
						processingUnit.getOwner().signalOpenBackupSession();
						processingUnit.lockFreeParallelCheckpointAndBackupState();
						processingUnit.getOwner().signalCloseBackupSession();

						long stopCheckpoint = System.currentTimeMillis();
						System.out.println("%% Total Checkpoint: "+(stopCheckpoint-startCheckpoint));
					}
				}
				initTime = System.currentTimeMillis();
			}
			else{
				try {
					int sleep = (int) (checkpointInterval - (System.currentTimeMillis() - initTime));
					if(sleep > 0){
						Thread.sleep(sleep);
					}
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
