/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.master;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.Listener;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTuple;

/**
 * Listener for the monitor master. This is useful when we want to add observers
 * that listen for tuples for specific operator identifiers. In particular, this
 * helps supporting the functionality to reset the system stable time via the
 * monitoring infrastructure.
 * 
 * @author mrouaux
 */
public interface MonitorMasterListener extends Listener {
    
    int getOperatorId();
    
    void onTupleReceived(MetricsTuple tuple);
    
}
