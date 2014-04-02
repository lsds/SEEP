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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization;

import java.io.PrintStream;

/**
 * Concrete metrics reporter that output metrics tuples as strings to a PrintStream 
 * object. This reporter can be used to periodically log metrics on each node running 
 * a SEEP query. The output stream can be anything really: standard output,
 * standard error or a ByteArraOutputStream. 
 * 
 * @author mrouaux
 */
public class StringMetricsSerializer implements MetricsSerializer<PrintStream> {

    private PrintStream os;
    
    @Override
    public void initialize(PrintStream os) {
        this.os = os;
    }

    @Override
    public void serialize(MetricsTuple tuple) {
        os.print(tuple.toString());
    }

    @Override
    public String toString() {
        return "StringMetricsSerializer{" + '}';
    }
}
