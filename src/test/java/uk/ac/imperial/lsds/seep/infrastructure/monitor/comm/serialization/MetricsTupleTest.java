package uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricUnit;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue.*;

public class MetricsTupleTest {

    public MetricsTupleTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testTupleWithSingleMetric() {
        System.out.println("testTupleWithSingleMetric");
        
        Double expectedValue = Double.valueOf(100);
        int expectedOperator = 1;
        
        MetricsTuple tuple = new MetricsTuple();
        tuple.setOperatorId(expectedOperator);
        tuple.setMetricValue(MetricName.CPU_UTILIZATION, percent(expectedValue));
        
        assertEquals("Operator identifier is incorrect", 
                expectedOperator, tuple.getOperatorId());
        
        assertEquals("Metric value in tuple is incorrect", expectedValue, 
                Double.valueOf(tuple.getMetricValue(MetricName.CPU_UTILIZATION).getValue()));
        
        assertEquals("Metric unit in tuple is incorrect", MetricUnit.PERCENT, 
                tuple.getMetricValue(MetricName.CPU_UTILIZATION).getUnit());
    }
    
    @Test
    public void testTupleWithMultipleMetrics() {
        System.out.println("testTupleWithMultipleMetrics");
        
        Double expectedValue1 = Double.valueOf(100);
        Integer expectedValue2 = Integer.valueOf(512);
        int expectedOperator = 1;
        
        MetricsTuple tuple = new MetricsTuple();
        tuple.setOperatorId(expectedOperator);
        tuple.setMetricValue(MetricName.CPU_UTILIZATION, percent(expectedValue1));
        tuple.setMetricValue(MetricName.HEAP_SIZE, mb(expectedValue2));
        
        assertEquals("Operator identifier is incorrect", 
                expectedOperator, tuple.getOperatorId());
        
        assertEquals("Metric value in tuple is incorrect for " + MetricName.CPU_UTILIZATION.toString(), 
                expectedValue1, 
                Double.valueOf(tuple.getMetricValue(MetricName.CPU_UTILIZATION).getValue()));
        
        assertEquals("Metric value in tuple is incorrect for " + MetricName.HEAP_SIZE.toString(), 
                expectedValue2.intValue(), 
                Double.valueOf(tuple.getMetricValue(MetricName.HEAP_SIZE).getValue()).intValue());
        
        assertEquals("Metric unit in tuple is incorrect for " + MetricName.CPU_UTILIZATION.toString(), 
                MetricUnit.PERCENT, 
                tuple.getMetricValue(MetricName.CPU_UTILIZATION).getUnit());
    
        assertEquals("Metric unit in tuple is incorrect for " + MetricName.HEAP_SIZE.toString(), 
                MetricUnit.MEGABYTES, 
                tuple.getMetricValue(MetricName.HEAP_SIZE).getUnit());
    }
}