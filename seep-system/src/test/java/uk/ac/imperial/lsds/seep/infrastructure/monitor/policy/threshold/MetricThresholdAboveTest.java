/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricUnit;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;

/**
 *
 * @author mrouaux
 */
public class MetricThresholdAboveTest {
    
    public MetricThresholdAboveTest() {
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
    public void testThresholdEvaluation() {
        System.out.println("testThresholdEvaluation");
        
        MetricValue thresholdValue = new MetricValue();
        thresholdValue.setValue(10);
        thresholdValue.setUnit(MetricUnit.GIGABYTES);
        
        MetricThresholdAbove threshold = new MetricThresholdAbove(thresholdValue);
        
        MetricValue value = new MetricValue();
        value.setValue(20);
        value.setUnit(MetricUnit.GIGABYTES);
        
        assertTrue("The value is below the threshold", threshold.evaluate(value));
    }
    
    @Test
    public void testThresholdEvaluationWithUnitConversion() {
        System.out.println("testThresholdEvaluationWithUnitConversion");
        
        MetricValue thresholdValue = new MetricValue();
        thresholdValue.setValue(20);
        thresholdValue.setUnit(MetricUnit.MEGABYTES);
        
        MetricThresholdAbove threshold = new MetricThresholdAbove(thresholdValue);
        
        MetricValue value = new MetricValue();
        value.setValue(1);
        value.setUnit(MetricUnit.GIGABYTES);
        
        assertTrue("The value is below the threshold", threshold.evaluate(value));
    }
}