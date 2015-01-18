/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor;

/**
 * Interface for classes that can be observed. 
 * 
 * @author mrouaux
 */
public interface Observable<T extends Listener> {
    
    void addListener(T listener);
    
}
