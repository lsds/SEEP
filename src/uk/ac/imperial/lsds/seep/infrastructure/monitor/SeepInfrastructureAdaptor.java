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
package uk.ac.imperial.lsds.seep.infrastructure.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.InfrastructureAdaptor;
import uk.ac.imperial.lsds.seep.operator.Operator;

/**
 * Adaptor class to allow the monitoring framework to interact with SEEP, maintaining
 * a relatively loose coupling between the two. The monitoring layer will always
 * call SEEP by means of this adaptor and never directly. There is a single point
 * of integration for the interface framework -> SEEP. 
 * 
 * @author mrouaux
 */
public class SeepInfrastructureAdaptor implements InfrastructureAdaptor {

    final private Logger logger = LoggerFactory.getLogger(SeepInfrastructureAdaptor.class);

    private Infrastructure infrastructure;
    private Map<Integer, Integer> originalSizes;
    
    /**
     * Convenience constructor.
     * @param infrastructure 
     */
    public SeepInfrastructureAdaptor(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
    }
    
    /**
     * Returns list of logical operator identifiers in the current query.
     * @return 
     */
    @Override
    public List<Integer> getOperatorIds() {
        List<Integer> operatorIds = new ArrayList<Integer>();
        for(Operator op : infrastructure.getOps()) {
            operatorIds.add(op.getOperatorId());
        }
        
        return operatorIds;
    }

    /**
     * Returns current size for a given logical operator identifier in the current
     * query (counts physical instances of logical operator).
     * @param operatorId Operator identifier from the logical query.
     * @return Number of physical instances executing the operator.
     */
    @Override
    public int getOperatorCurrentSize(int operatorId) {
        List<Integer> nodeIds = infrastructure.getEiu().getNodeIdsForOperatorId(operatorId);
        int currSize = 0;
        
        if (nodeIds != null) {
            for(Integer nodeId : nodeIds) {
                // logger.debug("operatorId=" + operatorId + " running at node nodeId=" + nodeId);
            }
        
            currSize = nodeIds.size();
        }
        
        return currSize;
    }

    /**
     * Sets the new size for a given logical operator identifier.
     * @param operatorId Operator identifier in the logical query.
     * @param size Size to achieve for the operator (as number of physical 
     * instances of the logical operator).
     */
    @Override
    public void setOperatorScaledSize(int operatorId, int size) {
        // Get the current size for the operator
        int currSize = getOperatorCurrentSize(operatorId);
        logger.info("operatorId=" + operatorId + " currSize=" + currSize);
        logger.info("operatorId=" + operatorId + " newSize=" + size);
        
        // If the desired size is greater than the current operator size, there
        // are fewer nodes that necessary and we need to scale out. The elastic
        // tools only provide a method to scale operators by one, we might need 
        // to scale a few times to achieve the desired size.
        if (size > currSize) {
            logger.info("Scaling out operatorId=" + operatorId + " from currentSize=" 
                    + currSize + " to newSize=" + size);
            
            for(int i = 0; i < size - currSize; i++) {
                infrastructure.getEiu().alert(operatorId);
            }
        } else if (size < currSize) {
            logger.info("Scaling in operatorId=" + operatorId + " from currentSize=" 
                    + currSize + " to newSize=" + size);
            
            for(int i = 0; i < currSize - size; i++) {
                infrastructure.getEiu().unalertCPU(operatorId);
            }
        }
    }

    /**
     * Original size of a given operator identifier in the logical query. In most
     * cases this is just going to be 1.
     * @param operatorId Operator identifier in the logical query.
     * @return Typically, the value 1.
     */
    @Override
    public int getOperatorOriginalSize(int operatorId) {
        if(!originalSizes.containsKey(operatorId)) {
            // Use the current size as the original size if we haven't recorded this yet
            originalSizes.put(operatorId, getOperatorCurrentSize(operatorId));
        }
        
        return originalSizes.get(operatorId);
    }
}
