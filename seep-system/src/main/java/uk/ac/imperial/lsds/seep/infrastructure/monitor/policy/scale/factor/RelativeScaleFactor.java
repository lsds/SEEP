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
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.ScaleInAction;

/**
 * Relative scaling factor for a policy rule. This class represents a relative
 * increase in the number of physical nodes/VMs allocated to a particular operator.
 * E.g.: if the factor is 4 and the current size is 2, then the new operator size 
 * after applying the scaling factor will be 8.
 * 
 * @author mrouaux
 */
public class RelativeScaleFactor extends ScaleFactor {

    /**
     * Convenience constructor.
     * @param factor Scaling factor
     */
    public RelativeScaleFactor(double factor) {
        super(factor);
    }

    /**
     * Applies the relative scaling factor by multiplying the current operator
     * side by the scaling factor. The method will round to obtain the new 
     * operator size.
     * 
     * @param currentSize Current number of machines allocated.
     * @param action Action indicating how the scaling factor needs to be applied
     * (increase or reduce the current size by the scaling factor).
     * @return Scaled number of machines to allocate.
     */
    @Override
    public int apply(int currentSize,  Action action) {
        // Assume a scale-out action
        int newSize = Math.round(currentSize 
                                    * new Double(getFactor()).floatValue());
        
        if(action instanceof ScaleInAction) {
            newSize = Math.round(currentSize 
                        * new Double(Math.pow(getFactor(), -1)).floatValue());
            if(newSize < 1) {
                newSize = 1;
            }
        }
        
        return newSize;
    }
}
