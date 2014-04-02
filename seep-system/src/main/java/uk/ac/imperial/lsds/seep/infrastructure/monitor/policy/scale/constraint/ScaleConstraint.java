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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint;

/**
 * Base class for scaling constraints imposed on policy rules. The class defines
 * static factory methods that allow rules to specify whether a constraint should be
 * relative (i.e.: don't scale over a factor of X of the initial physical query
 * plan for an operator) or absolute (e.g.: the number of nodes allocated to a
 * particular operator should not exceed X). The class delegates constraint enforcement
 * logic to subclasses. 
 * 
 * @author mrouaux
 */
public abstract class ScaleConstraint {

    /**
     * Factory method. Creates a relative scaling constraint.
     * @param constraint Constraint for relative scaling.
     * @return RelativeScaleConstraint instance
     */
    public static ScaleConstraint factor(double constraint) {
        return new RelativeScaleConstraint(constraint);
    }
    
    /**
     * Factory method. Creates an absolute scaling constraint.
     * @param constraint Constraint for absolute scaling.
     * @return AbsoluteScaleConstraint instance
     */
    public static ScaleConstraint nodes(int constraint) {
        return new AbsoluteScaleConstraint(constraint);
    }
    
	private double value;

    /**
     * Convenience constructor.
     * @param constraintValue Scaling constraint
     */
    public ScaleConstraint(double constraintValue) {
        this.value = constraintValue;
    }

    /**
     * @return Scaling constraint
     */
	public double getValue() {
		return value;
	}

    /**
     * @param constraintValue Scaling constraint
     */
	public void setValue(double constraintValue) {
		this.value = constraintValue;
	}

    /**
     * Subclasses need to provide concrete implementations of this method based 
     * on the type of constraint they represent.
     * 
     * @param scaledSize New scaling size for a given operator. If there are 8
     * nodes in the cluster (or VMs) allocated to a given operator, then the scaling
     * size for such operator is 8. 
     * @return True if current scaling exceeds constraint.
     */
    public abstract boolean evaluate(int scaledSize);

    @Override
    public String toString() {
        String className = this.getClass().getSimpleName();
        return className + "{" + "value=" + value + '}';
    }
}
