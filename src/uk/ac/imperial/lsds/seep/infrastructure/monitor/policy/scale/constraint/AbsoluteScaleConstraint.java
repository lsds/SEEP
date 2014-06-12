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
 * Absolute scaling constraint for a policy rule. This class represents an absolute 
 * constraint on the scaling-out permitted for a given operator or set of operators.
 * The scaling-in constraint is implicit and assumed to be 1 (e.g.: each operator is 
 * mapped to at least one node/VM in the physical query plan). 
 * 
 * @author mrouaux
 */
public class AbsoluteScaleConstraint extends ScaleConstraint {

    /**
     * Convenience constructor.
     * @param constraint Absolute scaling constraint for a policy rule.
     */
    AbsoluteScaleConstraint(int constraint) {
        super(constraint);
    }
    
    /**
     * @param scaledSize Size after scaling for a given operator. 
     * @return True if current scaling exceeds absolute constraint.
     */
    public boolean evaluate(int scaledSize) {
        return (scaledSize > getValue());
    }
}
