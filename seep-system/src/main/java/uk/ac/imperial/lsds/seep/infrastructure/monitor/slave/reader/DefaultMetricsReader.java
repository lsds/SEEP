/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;

/**
 * Default concrete implementation of a metric reader for slaves. There might
 * be other readers that provide more detailed or application specific metrics.
 * A single slave node can read values from multiple readers.
 * 
 * @author mrouaux
 */
public class DefaultMetricsReader implements MetricsReader {

    private final static MetricRegistry metricRegistry 
            = SharedMetricRegistries.getOrCreate("uk.ac.imperial.lsds.seep.infrastructure.monitor");
    
    static {
        metricRegistry.register(MetricName.QUEUE_LENGTH.getName(), new Counter());
        metricRegistry.register(MetricName.OPERATOR_LATENCY.getName(), new Timer());
    }
    
    private static final String MEMORY_HEAP_SIZE_KEY = "heap.used";
    private static final String MEMORY_HEAP_UTIL_KEY = "heap.usage";
    
    // Initially, we will only support CPU utilisation on slave nodes and grow
    // from there. Typically, other metrics we might be interested on are heap
    // side/utilisation, processing delay and input queue length for an operator. 
    private MetricName[] readableNames = new MetricName[]{
        MetricName.CPU_UTILIZATION,
        MetricName.HEAP_SIZE,
        MetricName.HEAP_UTILIZATION,
        MetricName.QUEUE_LENGTH,
        MetricName.OPERATOR_LATENCY
    };
    
    private OperatingSystemMXBean operatingSystemMXBean;
    private RuntimeMXBean runtimeMXBean;
    private MemoryUsageGaugeSet memoryMetricSet;
    
    /**
     * Default constructor
     */
    public DefaultMetricsReader() {
        // Get management beans to obtain JVM and OS runtime details
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        this.operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) 
            ManagementFactory.getOperatingSystemMXBean();
        
        this.memoryMetricSet = new MemoryUsageGaugeSet();
    }
    
    /**
     * @return List of metric names returned by this reader.
     */
    @Override
    public List<MetricName> readableNames() {
        return Arrays.asList(readableNames);
    }

    /**
     * Read a metric value for a given name.
     * @param name Name of the metric to read.
     * @return Metric value.
     */
    @Override
    public MetricValue readValue(MetricName name) {
        MetricValue value = null;

        switch (name) {
            case CPU_UTILIZATION:
                value = readCpuUtilization();
                break;

            case HEAP_SIZE:
                value = readHeapSize();
                break;

            case HEAP_UTILIZATION:
                value = readHeapUtilization();
                break;
                
            case QUEUE_LENGTH:
                value = readQueueLength();
                break;
                
            case OPERATOR_LATENCY:
                value = readOperatorLatency();
                break;
        }

        return value;
    }
    
    /**
     * Calculate CPU utilisation and return as MetricValue (percentage).
     */
    private MetricValue readCpuUtilization() {
        int availableProcessors = operatingSystemMXBean.getAvailableProcessors();
        
        long stUpTime = runtimeMXBean.getUptime();
        long stProcessCpuTime = operatingSystemMXBean.getProcessCpuTime();

        try {
            Thread.sleep(100);
        } catch (Exception ex) { 
        }

        long edUpTime = runtimeMXBean.getUptime();
        long edProcessCpuTime = operatingSystemMXBean.getProcessCpuTime();
        
        long elapsedCpu = edProcessCpuTime - stProcessCpuTime;
        long elapsedTime = edUpTime - stUpTime;

        double cpuUsage = Math.min(100F, elapsedCpu / (elapsedTime * 10000F * availableProcessors));
        return MetricValue.percent(cpuUsage);
    }
    
    /**
     * Obtain heap used size and return as MetricValue (bytes).
     */
    private MetricValue readHeapSize() {
        Map<String, Metric> metrics = memoryMetricSet.getMetrics();
        MetricValue value = null;
        
        if (metrics.containsKey(MEMORY_HEAP_SIZE_KEY)) {
            value = MetricValue.bytes(
                    ((Long) ((Gauge) metrics.get(MEMORY_HEAP_SIZE_KEY)).getValue()).intValue());
        }
        
        return value;
    }
    
    /**
     * Obtain heap utilisation and return as MetricValue (percentage).
     */
    private MetricValue readHeapUtilization() {
        Map<String, Metric> metrics = memoryMetricSet.getMetrics();
        MetricValue value = null;
        
        if (metrics.containsKey(MEMORY_HEAP_UTIL_KEY)) {
            value = MetricValue.percent(
                    (Double) ((Gauge) metrics.get(MEMORY_HEAP_UTIL_KEY)).getValue());
        }
        
        return value;
    }
    
    /**
     * Obtain operator latency and return as MetricValue (milliseconds)
     */
    private MetricValue readOperatorLatency() {
        MetricValue value = null;
        
        Timer operatorLatency = metricRegistry
                .timer(MetricName.OPERATOR_LATENCY.getName());
        
        if (operatorLatency != null) {
            value = MetricValue.millis(Double.valueOf(
                    operatorLatency.getSnapshot().getMean() / 1000000).intValue());
        }
        
        return value;
    }
    
    /**
     * Obtain operator queue length and return as MetricValue (tuples)
     */
    private MetricValue readQueueLength() {
        MetricValue value = null;
        
        Counter queueLength = metricRegistry
                .counter(MetricName.QUEUE_LENGTH.getName());
        
        if (queueLength != null) {
            value = MetricValue.tuples(Long.valueOf(
                    queueLength.getCount()).intValue());
        }
        
        return value;
    }
}
