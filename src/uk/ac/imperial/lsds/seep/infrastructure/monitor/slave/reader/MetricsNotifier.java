/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader;

import com.codahale.metrics.Timer;

/**
 *
 * @author martinrouaux
 */
public interface MetricsNotifier {
    
    Timer.Context operatorStart();
    
    void operatorEnd(Timer.Context context);
    
    void inputQueuePut();
    
    void inputQueueTake();
    
    void inputQueueReset();
    
}
