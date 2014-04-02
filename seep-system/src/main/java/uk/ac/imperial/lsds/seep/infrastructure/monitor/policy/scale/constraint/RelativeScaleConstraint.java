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
 * Relative scaling constraint for a policy rule. This class represents a relative
 * constraint on the scaling-out permitted for a given operator or set of operators.
 * The scaling-in constraint is implicit and assumed to be 1 (e.g.: no operator is 
 * allowed to scale below its initial allocation in the physical query plan). 
 * 
 * @author mrouaux
 */
public class RelativeScaleConstraint extends ScaleConstraint {

    private int originalSize;
    
    /**
     * Convenience constructor.
     * @param constraint Relative scaling constraint for a policy rule. This parameter 
     * represent a scaling factor and not the actual number of nodes/VMs currently
     * allocated for a given operator.
     */
    RelativeScaleConstraint(double constraint) {
        super(constraint);
    }

    public int getOriginalSize() {
        return originalSize;
    }

    public void setOriginalSize(int size) {
        this.originalSize = size;
    }
    
    /**
     * Builder method to specify original size for the operator to which the
     * current constraint applies.
     * @param size Original size for the operator.
     * @return Reference to the relative scaling constraint object.
     */
    public RelativeScaleConstraint withOriginalSize(int size) {
        this.originalSize = size;
        return this;
    }
    
    /**
     * @param scaledSize Current scaling size for a given operator. 
     * @return True if current scaling exceeds relative constraint (based on original
     * size for the operator).
     */
    @Override
    public boolean evaluate(int scaledSize) {
        return (new Integer(scaledSize).doubleValue() / 
                new Integer(originalSize).doubleValue() > getValue());
    }
}
