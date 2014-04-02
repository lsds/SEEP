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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.Action;

/**
 * Base abstract class for scaling factors to be applied by policies. The class 
 * defines static factory methods that allow rules to specify whether a factor 
 * should be relative (i.e.: scale this operator by a factor of X) or absolute 
 * (e.g.: the number of nodes allocated to this operator should be scaled by exactly 
 * X nodes or VMs). The class delegates the calculation of the new scaling size 
 * for an operator to subclasses.
 * 
 * @author mrouaux
 */
public abstract class ScaleFactor {

    /**
     * Factory method. Creates a relative scaling factor.
     * @param factor Relative scaling factor.
     * @return RelativeScaleFactor instance
     */
    public static ScaleFactor relative(double factor) {
        return new RelativeScaleFactor(factor);
    }
    
    /**
     * Factory method. Creates an absolute scaling factor.
     * @param factor Factor for absolute scaling.
     * @return AbsoluteScaleFactor instance
     */    
    public static ScaleFactor absolute(int factor) {
        return new AbsoluteScaleFactor(factor);
    }
    
    private double factor;
    
    /**
     * Convenience constructor.
     * @param factor Scaling factor
     */
    public ScaleFactor(double factor) {
        this.factor = factor;
    }

    /**
     * @return Scaling factor
     */
	public double getFactor() {
		return factor;
	}

    /**
     * @param factor Scaling factor
     */
	public void setFactor(double factor) {
		this.factor = factor;
	}
    
    /**
     * Applies the scaling factor to the current number of nodes/VMs for a 
     * particular operator and returns the scaled number. Subclasses implement
     * this method depending on the type of scaling to be applied.
     * 
     * @param currentSize Current number of machines allocated.
     * @param action Action indicating how the scaling factor needs to be applied
     * (increase or reduce the current size by the scaling factor).
     * 
     * @return Scaled number of machines to allocate.
     */
    public abstract int apply(int currentSize, Action action);

    @Override
    public String toString() {
        String className = this.getClass().getSimpleName();
        return className + "{" + "factor=" + factor + '}';
    }    
}
