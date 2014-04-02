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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete metrics reporter that output metrics tuples as binary streams to an 
 * OutputStream object. This reporter can be used to periodically push metrics 
 * tuples to a master node via a binary TCP socket. 
 * 
 * @author mrouaux
 */
public class BinaryMetricsDeserializer implements MetricsDeserializer<InputStream> {

    final private Logger logger = LoggerFactory.getLogger(BinaryMetricsDeserializer.class);

    private Kryo serializer = null;
    private Input input = null;

    @Override
    public void initialize(final InputStream is) {
        if (serializer == null) {
            serializer = new Kryo();
            serializer.register(MetricsTuple.class);
        }

        if (input == null) {
            input = new Input(is);
        }
        
        // Make sure that we reset the serialiser for this thread
        serializer.reset();
    }

    @Override
    public MetricsTuple deserialize() {
        MetricsTuple tuple = null;
        
        try {
            tuple = serializer.readObject(input, MetricsTuple.class);
        } catch(KryoException ex) {
            logger.error("Exception deserializing a tuple: " + ex.getMessage());
        }
        
        return tuple;
    }

    @Override
    public String toString() {
        return "BinaryMetricsDeserializer{" + '}';
    }
}
