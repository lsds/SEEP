/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;

/**
 * Notifies operator related events that need to be measured and reported as metrics
 * back to MonitorMaster from MonitorSlave instances.
 * 
 * @author mrouaux
 */
public class DefaultMetricsNotifier implements MetricsNotifier {

    private final static MetricRegistry metricRegistry 
        = SharedMetricRegistries.getOrCreate("uk.ac.imperial.lsds.seep.infrastructure.monitor");

    public static DefaultMetricsNotifier notifyThat(int operatorId) {
        return new DefaultMetricsNotifier(operatorId);
    }
    
    private int operatorId;

    DefaultMetricsNotifier(int operatorId) {
        this.operatorId = operatorId;
    }
    
    /**
     * Starts measuring the time/latency of a given event affecting a data tuple. 
     * @param operatorId Identifier of the operator handling the data tuple.
     * @return Timer context object.
     */
    @Override
    public Timer.Context operatorStart() {
        // TODO: we need something here to differentiate operators, in order to
        // allow multiple operator running per JVM. Is that supported anyway?
        Timer.Context context = null;
        
        Timer operatorLatency = metricRegistry
                .timer(MetricName.OPERATOR_LATENCY.getName());
        if (operatorLatency != null) {
            context = operatorLatency.time();
        }
        
        return context;
    }
    
    /**
     * Stops measuring the time/latency of a given event affecting a data tuple.
     * @param operatorId Operator identifier.
     * @param context Timer context object.
     */
    @Override
    public void operatorEnd(Timer.Context context) {
        if (context != null) {
            context.stop();
        }
    }

    /**
     * Notify arrival of data tuple for processing.
     * @param operatorId Operator identifier.
     */
    @Override
    public void inputQueuePut() {
        Counter queueLength = metricRegistry.counter(
                MetricName.QUEUE_LENGTH.getName());
        
        if (queueLength != null) {
            queueLength.inc();
        }
    }

    /**
     * Notify departure of data tuple after processing.
     * @param operatorId Operator identifier.
     */
    @Override
    public void inputQueueTake() {
        Counter queueLength = metricRegistry.counter(
                MetricName.QUEUE_LENGTH.getName());
        
        if (queueLength != null) {
            queueLength.dec();
        }
    }
    
    /**
     * Notify departure of data tuple after processing.
     * @param operatorId Operator identifier.
     */
    @Override
    public void inputQueueReset() {
        Counter queueLength = metricRegistry.counter(
                MetricName.QUEUE_LENGTH.getName());
        
        if (queueLength != null) {
            queueLength.dec(queueLength.getCount());
        }
    }    
}
