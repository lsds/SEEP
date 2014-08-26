/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util;

import java.util.List;

/**
 * Interface for infrastructure adaptor, capable of reporting and setting the 
 * number of nodes/VMs allocated for a particular operator. We use an adaptor
 * to decouple policy evaluation from the actual infrastructure supporting
 * stream queries in SEEP. The intention is also to facilitate testing of the
 * evaluation logic for scaling rules separately from the rest of the system. 
 * 
 * @author mrouaux
 */
public interface InfrastructureAdaptor {
 
    /**
     * Returns the list of operator identifiers for the current query.
     * @return List of identifiers for all the operators in the query
     */
    List<Integer> getOperatorIds();
    
    /**
     * Returns the size for a given operator.
     * @param operatorId Identifier for a given operator.
     * @return Number of cluster nodes or VMs allocated to the operator.
     */
    int getOperatorCurrentSize(int operatorId);
    
    /**
     * Sets the new size for a given operator.
     * @param operatorId Identifier for a given operator.
     * @param size New number of cluster nodes or VMs to allocate for the operator.
     */
    void setOperatorScaledSize(int operatorId, int size);
    
    /**
     * Returns the original size (i.e.: number of nodes or VMs) allocated to a 
     * given operator. Some rules need to know the original size in order to 
     * apply the appropriate constraints when scaling up.
     * 
     * @param operatorId Identifier for a given operator.
     * @return Number of cluster nodes or VMs originally allocated to the operator.
     */
    int getOperatorOriginalSize(int operatorId);
    
}
