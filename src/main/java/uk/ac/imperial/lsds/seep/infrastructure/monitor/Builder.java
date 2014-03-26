package uk.ac.imperial.lsds.seep.infrastructure.monitor;

/**
 * Base interface for all builders created in the monitoring package for SEEP.
 * Typically, a builder exposes several methods to client code in order to specify 
 * the different parts that constitute the object being built.
 * 
 * @author mrouaux
 * @param <T> Type of objects built by this builder.
 */
public interface Builder<T> {

	T build();
	
}
