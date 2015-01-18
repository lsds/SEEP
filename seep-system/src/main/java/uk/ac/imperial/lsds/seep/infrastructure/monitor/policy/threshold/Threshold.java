/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold;

/**
 * Abstract base class both for metric (e.g.: CPU utilisation is above 50%) and 
 * time-based thresholds (e.g.: for at least 30 seconds but less than 3 minutes).
 * Subclasses need to provide a concrete implementation capable of determining
 * if the threshold has been exceeded or not.
 * 
 * @author mrouaux
 */
public abstract class Threshold<T> {
    
    /**
     * @param value Value to evaluate given the threshold context.
     * @return True if the threshold has been exceeded (above or below depending
     * on the semantics of the concrete subclass implementing the method).
     */
    public abstract boolean evaluate(T value);
    
}
