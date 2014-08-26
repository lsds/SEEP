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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action;

/**
 * Class representing a scale-in action. This class is used simply as a marker
 * class, to indicate if a scaling factor needs to be applied growing or reducing
 * the number of nodes/VMs allocated to a particular operator.
 * 
 * @author mrouaux
 */
public class ScaleInAction implements Action {

    @Override
    public String toString() {
        return "ScaleInAction{" + '}';
    }
}
