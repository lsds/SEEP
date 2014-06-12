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
 * Just a marker interface for all scalable operators referenced by scaling rules.
 * The interface defines a simple equals() method that allows matching scalable
 * abstract operators in the scaling rules with concrete operators from the stream
 * query. Matching should always be done purely on the basis of contextual unique 
 * identifiers (i.e.: uniqueness is guaranteed only with one query).
 * 
 * @author mrouaux
 */
public abstract class Operator {

    /**
	 * Convenience static factory method that creates an instance of
	 * the OneOperator class, named as indicated by the name parameter.
	 * @param name Name for the operator
     * @param id Identifier for the operator in the context of the current query
	 * @return Instance of OneOperator.
	 */
	public static OneOperator operator(String name, int id) {
		return new OneOperator(name, id);
	}
    
	/**
	 * Convenience static factory method that creates an instance of 
	 * the AllOperators class.
	 * @return AllOperators instance
	 */
	public static AllOperators allOperators() {
		return new AllOperators();
	}
    
    /**
     * @param queryOperatorId Unique identifier for a concrete operator in the
     * current query.
     * @return True if the identifier matches the scalable operator. False otherwise.
     */
    public abstract boolean equals(int queryOperatorId);
    
}
