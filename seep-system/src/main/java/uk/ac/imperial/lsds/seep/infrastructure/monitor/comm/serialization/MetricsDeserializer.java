package uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization;

import java.io.InputStream;

/**
 * Base interface for all metrics deserializers. Subclasses implementing this interface
 * are responsible for converting/de-serializing the appropriate data representation
 * from the underlying channel into a tuple.
 * 
 * @author mrouaux
 */
public interface MetricsDeserializer<T extends InputStream> {
    
    void initialize(T is);
    
    MetricsTuple  deserialize();
    
}
