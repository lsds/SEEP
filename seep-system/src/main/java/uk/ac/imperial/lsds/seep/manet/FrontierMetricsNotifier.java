/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.manet;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;

/**
 * Notifies operator related events that need to be measured and reported as metrics
 * back to MonitorMaster from MonitorSlave instances.
 * 
 * @author mrouaux
 */
public class FrontierMetricsNotifier {

    private final static MetricRegistry metricRegistry 
        = SharedMetricRegistries.getOrCreate("uk.ac.imperial.lsds.seep.manet");
    private final static JmxReporter jmxReporter = JmxReporter
    		.forRegistry(metricRegistry)
    		.convertRatesTo(TimeUnit.SECONDS)
    		.convertDurationsTo(TimeUnit.MILLISECONDS)
    		.build();
    private final static Slf4jReporter sl4jReporter = Slf4jReporter
    		.forRegistry(metricRegistry)
    		.outputTo(LoggerFactory.getLogger(FrontierMetricsNotifier.class))
    		.convertRatesTo(TimeUnit.SECONDS)
    		.convertDurationsTo(TimeUnit.MILLISECONDS)
    		.build();
    
    static
    {
    	jmxReporter.start();
    	sl4jReporter.start(1, TimeUnit.SECONDS);
    }

    public static FrontierMetricsNotifier notifyThat(int operatorId) {
        return new FrontierMetricsNotifier(operatorId);
    }
    
    private int operatorId;

    FrontierMetricsNotifier(int operatorId) {
        this.operatorId = operatorId;
    }
    
    public void triedSend()
    {
    	final Meter trySendRequests = metricRegistry.meter(MetricRegistry.name(FrontierMetricsNotifier.class, "dispatcherMain", "trySendRequests"));
    	trySendRequests.mark();
    }
    
    public void sendSucceeded()
    {
    	final Meter sendSucceeded = metricRegistry.meter(MetricRegistry.name(FrontierMetricsNotifier.class, "dispatcherMain", "sendSucceeded"));
    	sendSucceeded.mark();
    }
    
    public void missedSwitch()
    {
    	final Meter missedSwitch = metricRegistry.meter(MetricRegistry.name(FrontierMetricsNotifier.class, "dispatcherMain", "missedSwitch"));
    	missedSwitch.mark();
    }
    
    //TODO: Would like to know the downOpId here.
    public void savedBatch()
    {
    	final Counter bufferLength = metricRegistry.counter(MetricRegistry.name(FrontierMetricsNotifier.class, "OutOfOrderBuffer.size", ""+operatorId));
    	bufferLength.inc();
    }
    
    public void trimmedBuffer(int trimmed)
    {
      	final Counter bufferLength = metricRegistry.counter(MetricRegistry.name(FrontierMetricsNotifier.class, "OutOfOrderBuffer.size", ""+operatorId));
    	bufferLength.dec(trimmed);
    }
    
    public void clearedBuffer()
    {
      	final Counter bufferLength = metricRegistry.counter(MetricRegistry.name(FrontierMetricsNotifier.class, "OutOfOrderBuffer.size", ""+operatorId));
    	bufferLength.dec(bufferLength.getCount());	//approx
    }
}
