/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util;

import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;

/**
 *
 * @author mrouaux
 */
public class MetricReadingTest {
    
    public MetricReadingTest() {
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
    public void testCreateReadingWithDefaultConstructor() {
        System.out.println("testCreateReadingWithDefaultConstructor");
        
        MetricReading reading = new MetricReading();
        
        Instant expectedTimestamp = new DateTime()
                                            .withYear(2013)
                                            .withMonthOfYear(12)
                                            .withDayOfMonth(29)
                                            .withHourOfDay(10)
                                            .withMinuteOfHour(30)
                                            .withSecondOfMinute(0).toInstant();
        
        MetricValue expectedMetricValue1 = MetricValue.percent(50);
        MetricValue expectedMetricValue2 = MetricValue.gb(1);
        MetricValue expectedMetricValue3 = MetricValue.millis(500);
        
        reading.setTimestamp(expectedTimestamp);
        reading.getValues().put(MetricName.CPU_UTILIZATION, expectedMetricValue1);
        reading.getValues().put(MetricName.HEAP_SIZE, expectedMetricValue2);
        reading.getValues().put(MetricName.OPERATOR_LATENCY, expectedMetricValue3);
        
        System.out.println(reading.toString());
        
        assertEquals(expectedTimestamp, reading.getTimestamp());
        
        assertEquals("Metric value is incorrect for CPU_UTILIZATION", 
            expectedMetricValue1, reading.getValues().get(MetricName.CPU_UTILIZATION));
        
        assertEquals("Metric value is incorrect for HEAP_SIZE", 
            expectedMetricValue2, reading.getValues().get(MetricName.HEAP_SIZE));
        
        assertEquals("Metric value is incorrect for OPERATOR_LATENCY",
            expectedMetricValue3, reading.getValues().get(MetricName.OPERATOR_LATENCY));
    }
    
    @Test
    public void testCreateReadingWithConvenienceConstructor() {
        System.out.println("testCreateReadingWithConvenienceConstructor");

        
        Instant expectedTimestamp = new DateTime()
                                            .withYear(2013)
                                            .withMonthOfYear(12)
                                            .withDayOfMonth(29)
                                            .withHourOfDay(10)
                                            .withMinuteOfHour(30)
                                            .withSecondOfMinute(0).toInstant();
        
        MetricValue expectedMetricValue1 = MetricValue.percent(50);
        MetricValue expectedMetricValue2 = MetricValue.gb(1);
        MetricValue expectedMetricValue3 = MetricValue.millis(500);
        
        Map<MetricName, MetricValue> expectedValues = 
                                        new HashMap<MetricName, MetricValue>();
        
        expectedValues.put(MetricName.CPU_UTILIZATION, expectedMetricValue1);
        expectedValues.put(MetricName.HEAP_SIZE, expectedMetricValue2);
        expectedValues.put(MetricName.OPERATOR_LATENCY, expectedMetricValue3);
        
        MetricReading reading = new MetricReading(expectedValues, expectedTimestamp);
        System.out.println(reading.toString());
        
        assertEquals(expectedTimestamp, reading.getTimestamp());
        
        assertEquals("Metric value is incorrect for CPU_UTILIZATION", 
            expectedMetricValue1, reading.getValues().get(MetricName.CPU_UTILIZATION));
        
        assertEquals("Metric value is incorrect for HEAP_SIZE", 
            expectedMetricValue2, reading.getValues().get(MetricName.HEAP_SIZE));
        
        assertEquals("Metric value is incorrect for OPERATOR_LATENCY",
            expectedMetricValue3, reading.getValues().get(MetricName.OPERATOR_LATENCY));        
    }
}