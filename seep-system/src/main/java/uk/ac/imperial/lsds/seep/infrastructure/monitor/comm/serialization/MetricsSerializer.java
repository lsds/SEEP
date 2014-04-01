package uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization;

import java.io.OutputStream;

/**
 * Base interface for all metrics serializers. Subclasses implementing this interface
 * are responsible for converting/serializing the reported tuple to an appropriate representation
 * for the underlying channel.
 * 
 * @author mrouaux
 */
public interface MetricsSerializer<T extends OutputStream> {
    
    void initialize(T os);
    
    void serialize(MetricsTuple tuple);
    
}
