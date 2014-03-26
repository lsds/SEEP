package uk.ac.imperial.lsds.seep.infrastructure.monitor;

/**
 * Base interface for all factories created in the monitoring package for SEEP.
 * Typically, a factory exposes a few methods to client code in order to create
 * instances of predetermined classes. In general, client code has no or little 
 * control over how these objects are created.
 *
 * @author mrouaux
 * @param <T> Type of objects created by this factory.
 */
public interface Factory<T> {
    
    T create();
    
}
