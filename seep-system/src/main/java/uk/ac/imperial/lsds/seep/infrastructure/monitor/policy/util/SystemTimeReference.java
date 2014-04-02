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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util;

import org.joda.time.Instant;

/**
 * Provides a time reference for rule evaluation, based on the current system time.
 * 
 * @author mrouaux
 */
public class SystemTimeReference implements TimeReference {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
