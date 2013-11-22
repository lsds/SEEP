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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.infrastructure.NodeManager;
import uk.ac.imperial.lsds.seep.operator.EndPoint;
import uk.ac.imperial.lsds.seep.processingunit.StatefulProcessingUnit;
import uk.ac.imperial.lsds.seep.runtimeengine.DisposableCommunicationChannel;
import uk.ac.imperial.lsds.seep.state.StateWrapper;

/**
* StateBackupWorker. This class is in charge of checking when the associated operator has a state to do backup and doing the backup of such state. This is operator dependant.
*/

public class StateBackupWorker implements Runnable, Serializable{

	final private Logger LOG = LoggerFactory.getLogger(StateBackupWorker.class);
	private static final long serialVersionUID = 1L;
	
	private long initTime = 0;
	
	private StatefulProcessingUnit processingUnit;
	private boolean goOn = true;
	private int checkpointInterval = 0;
	private StateWrapper state;
	
	public enum CheckpointMode{
		LARGE_STATE, LIGHT_STATE
	}
	private final CheckpointMode CHECKPOINTMODE;
	
	public void stop(){
		this.goOn = false;
	}

	public StateBackupWorker(StatefulProcessingUnit processingUnit, StateWrapper s){
		this.processingUnit = processingUnit;
		this.state = s;
		if(GLOBALS.valueFor("checkpointMode").equals("large-state")){
			LOG.info("Checkpoint mode of this operator is LARGE-STATE");
			this.CHECKPOINTMODE = CheckpointMode.LARGE_STATE;
		}
		else if(GLOBALS.valueFor("checkpointMode").equals("light-state")){
			LOG.info("Checkpoint mode of this operator is LIGHT-STATE");
			this.CHECKPOINTMODE = CheckpointMode.LIGHT_STATE;
		}
		else{
			// safe default
			this.CHECKPOINTMODE = CheckpointMode.LARGE_STATE;
		}
	}
	
	public CheckpointMode getCheckpointMode(){
		return CHECKPOINTMODE;
	}

	public void run(){
		initTime = System.currentTimeMillis();
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if(CHECKPOINTMODE.equals(CheckpointMode.LARGE_STATE)){
			executeLargeStateMechanism();
		}
		else if(CHECKPOINTMODE.equals(CheckpointMode.LIGHT_STATE)){
			executeLightStateMechanism();
		}
		else{
			LOG.error("-> Not defined checkpoint mode");
		}
	}
	
	public void executeLightStateMechanism(){
		//processingUnit.checkpointAndBackupState();
		checkpointInterval = state.getCheckpointInterval();
		while(goOn){
			long elapsedTime = System.currentTimeMillis() - initTime;
			if(elapsedTime > checkpointInterval){
				//synch this call
				if(GLOBALS.valueFor("eftMechanismEnabled").equals("true")){
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
		// First filter starTopology by removing this own operator
		processingUnit.getPuContext().filterStarTopology(processingUnit.getOperator().getOperatorId());
//		// Blocking call
//		int starTopologySize = processingUnit.getPuContext().getStarTopology().size();
//		processingUnit.getOwner().signalOpenBackupSession(starTopologySize);
//		processingUnit.lockFreeParallelCheckpointAndBackupState();
//		processingUnit.getOwner().signalCloseBackupSession(starTopologySize);
		checkpointInterval = state.getCheckpointInterval();
		int starTopologySize = 0;
		while(goOn){
			long elapsedTime = System.currentTimeMillis() - initTime;
			if(elapsedTime > checkpointInterval){
				//synch this call
				if(GLOBALS.valueFor("eftMechanismEnabled").equals("true")){
					//if not initialisin state...
					if(!processingUnit.getSystemStatus().equals(StatefulProcessingUnit.SystemStatus.INITIALISING_STATE)){
						long startCheckpoint = System.currentTimeMillis();
						
						starTopologySize = processingUnit.getPuContext().getStarTopology().size();
						if(starTopologySize <= 0){
							System.out.println("no star-topology");
							continue;
						}
						///\todo{may do some filtering out of the startopology to avoid shuffling. Not important now}
						
						System.out.println("STAR TOPOLOGY");
						System.out.println("##############");
						for(EndPoint dcc : processingUnit.getPuContext().getStarTopology()){
							System.out.println((DisposableCommunicationChannel)dcc);
						}
						System.out.println("##############");
						
						// Blocking call
						processingUnit.getOwner().signalOpenBackupSession(starTopologySize);
						processingUnit.lockFreeParallelCheckpointAndBackupState();
						processingUnit.getOwner().signalCloseBackupSession(starTopologySize);

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
