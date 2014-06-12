/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Martin Rouaux - Added monitoring framework on top of SEEP
 *     Created this class to support SCALE_IN control messages sent to operators.
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

/**
 *
 * @author mrouaux
 */
public class ScaleInInfo {
    
    private int operatorId;
    private int victimOperatorId;
    private boolean isStateful;

    public ScaleInInfo() {
    }

    public ScaleInInfo(int operatorId, int victimOperatorId, boolean isStateful) {
        this.operatorId = operatorId;
        this.victimOperatorId = victimOperatorId;
        this.isStateful = isStateful;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }

    public int getVictimOperatorId() {
        return victimOperatorId;
    }

    public void setVictimOperatorId(int victimOperatorId) {
        this.victimOperatorId = victimOperatorId;
    }
    
	public boolean isStatefulScaleOut(){
		return isStateful;
	}
}