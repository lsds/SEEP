package uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTupleBuilder.tuple;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue.*;

/**
 *
 * @author mrouaux
 */
public class MetricsTupleBuilderTest {
    
    public MetricsTupleBuilderTest() {
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
    public void testBuildTupleSingleMetric() {
        System.out.println("testBuildTupleSingleMetric");
        
        Double expectedValue = Double.valueOf(50);
        int expectedOperatorId = 1000;
        
        MetricsTuple tuple = tuple()
                        .forOperator(expectedOperatorId)
                        .withMetric(MetricName.CPU_UTILIZATION, percent(expectedValue))
                        .build();
        System.out.println(tuple.toString());
        
        assertEquals("Value of operator identifier is not as expected",
                    expectedOperatorId, tuple.getOperatorId());
        
        assertEquals("Value of metric is not as expected", expectedValue, 
                    Double.valueOf(tuple.getMetricValue(MetricName.CPU_UTILIZATION).getValue()));
        
        assertNull("Metric not in tuple should return a null value",
                    tuple.getMetricValue(MetricName.HEAP_SIZE));
    }
    
    @Test
    public void testBuildTupleMultipleMetrics() {
        System.out.println("testBuildTupleMultipleMetrics");
        
        Double expectedValue1 = Double.valueOf(50);
        Integer expectedValue2 = Integer.valueOf(1);
        Integer expectedValue3 = Integer.valueOf(1000);
        int expectedOperatorId = 1000;
        
        MetricsTuple tuple = tuple()
                        .forOperator(expectedOperatorId)
                        .withMetric(MetricName.CPU_UTILIZATION, percent(expectedValue1))
                        .withMetric(MetricName.HEAP_SIZE, gb(expectedValue2))
                        .withMetric(MetricName.QUEUE_LENGTH, tuples(expectedValue3))
                        .build();
        System.out.println(tuple.toString());
        
        assertEquals("Value of operator identifier is not as expected",
                    expectedOperatorId, tuple.getOperatorId());
        
        assertEquals("Value of metric #1 is not as expected", expectedValue1, 
                    Double.valueOf(tuple.getMetricValue(MetricName.CPU_UTILIZATION).getValue()));
        
        assertEquals("Value of metric #2 is not as expected", expectedValue2.intValue(), 
                    Double.valueOf(tuple.getMetricValue(MetricName.HEAP_SIZE).getValue()).intValue());
        
        assertEquals("Value of metric #3 is not as expected", expectedValue3.intValue(), 
                    Double.valueOf(tuple.getMetricValue(MetricName.QUEUE_LENGTH).getValue()).intValue());
     
        assertNull("Metric not in tuple should return a null value",
                    tuple.getMetricValue(MetricName.OPERATOR_LATENCY));
    }
    
    @Test
    public void testBuildTupleMultipleMetricsWithDuplicates() {
        System.out.println("testBuildTupleMultipleMetricsWithDuplicates");
        
        Double expectedValue1 = Double.valueOf(50);
        Double expectedValue2 = Double.valueOf(70);
        int expectedOperatorId = 1000;
        
        MetricsTuple tuple = tuple()
                        .forOperator(expectedOperatorId)
                        .withMetric(MetricName.CPU_UTILIZATION, percent(expectedValue1))
                        .withMetric(MetricName.CPU_UTILIZATION, percent(expectedValue2))
                        .build();
        System.out.println(tuple.toString());
        
        assertEquals("Value of operator identifier is not as expected",
                    expectedOperatorId, tuple.getOperatorId());
        
        assertEquals("Value of metric is not as expected", expectedValue2, 
                    Double.valueOf(tuple.getMetricValue(MetricName.CPU_UTILIZATION).getValue()));
    }
}
