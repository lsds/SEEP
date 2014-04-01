package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util;

/**
 * Interface for providers of metric readings, capable of reporting the value 
 * for a metric. Subclasses implementing this interface need to adapt other
 * data structures (e.g.: tuples) used in the system to handle metrics and 
 * their corresponding values to a representation that is suitable for the 
 * monitoring and scaling layer, i.e.: MetricReading instances.
 * 
 * @author mrouaux
 */
public interface MetricReadingProvider {
 
    int getOperatorId();
    
    MetricReading nextReading();
    
}
