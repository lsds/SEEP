/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricUnit;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;

/**
 *
 * @author mrouaux
 */
public class MetricThresholdBelowTest {
    
    public MetricThresholdBelowTest() {
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
        
        MetricThresholdBelow threshold = new MetricThresholdBelow(thresholdValue);
        
        MetricValue value = new MetricValue();
        value.setValue(1);
        value.setUnit(MetricUnit.GIGABYTES);
        
        assertTrue("The value is below the threshold", threshold.evaluate(value));
    }
    
    @Test
    public void testThresholdEvaluationWithUnitConversion() {
        System.out.println("testThresholdEvaluationWithUnitConversion");
        
        MetricValue thresholdValue = new MetricValue();
        thresholdValue.setValue(10);
        thresholdValue.setUnit(MetricUnit.GIGABYTES);
        
        MetricThresholdBelow threshold = new MetricThresholdBelow(thresholdValue);
        
        MetricValue value = new MetricValue();
        value.setValue(500);
        value.setUnit(MetricUnit.MEGABYTES);
        
        assertTrue("The value is below the threshold", threshold.evaluate(value));
    }
}