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
 * Single-identifiable query operator to which a scaling rule is applied.
 * @author mrouaux
 */
public class OneOperator extends Operator {
	
	private String name;
	private int id;

    /**
     * Default constructor
     */
	public OneOperator() {
		this.name = null;
        this.id = 0;
	}
	
    /**
     * Convenience constructor.
     * @param name User-friendly name for the operator (simply to facilitate
     * identification of operators in debug output).
     * @param id Unique identifier for the operator to which the rule applies.
     */
	public OneOperator(String name, int id) {
		this.name = name;
        this.id = id;
	}

    /**
     * @return Operator name
     */
	public String getName() {
		return name;
	}

    /**
     * @param name Operator name
     */
    public void setName(String name) {
		this.name = name;
	}

    /**
     * @return Unique identifier for the operator
     */
    public int getId() {
        return id;
    }

    /**
     * @param id Unique identifier for the operatorUnique identifier for the operator
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @param queryOperatorId Unique identifier for a concrete operator in the
     * current query.
     * @return True if the identifier matches the scalable operator. False otherwise.
     */
    @Override
    public boolean equals(int queryOperatorId) {
        return (id == queryOperatorId);
    }

    /**
     * @return String representation of the current Operator object.
     */
    @Override
    public String toString() {
        return "OneOperator{" + "name=" + name + ", id=" + id + '}';
    }
}
