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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator;

/**
 * Multiple non-identifiable wildcard operator representing all concrete operators
 * within a query. Allows defining scaling rules that apply across the board to
 * all the operators defined for a given query.
 * 
 * @author mrouaux
 */
public class AllOperators extends Operator {
	
    /**
     * Default constructor
     */
	public AllOperators() {
	}
    
    /**
     * @param queryOperatorId Unique identifier for a concrete operator in the
     * current query.
     * @return Always true. Any operator in a query matches against this wildcard
     * scalable operator object.
     */    
    @Override
    public boolean equals(int queryOperatorId) {
        return true;
    }
    
    /**
     * @return String representation of the object
     */
	@Override
	public String toString() {
		return "AllOperators []";
	}
}
