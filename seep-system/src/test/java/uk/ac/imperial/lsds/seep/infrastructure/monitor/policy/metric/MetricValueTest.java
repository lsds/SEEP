/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author martinrouaux
 */
public class MetricValueTest {
    
    public MetricValueTest() {
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
    public void testUnitConversionBytesToBytes() {
        System.out.println("testUnitConversionBytesToBytes");
        MetricValue convertedValue = null;
        
        convertedValue = new MetricValue(1024 * 1024 * 1024, MetricUnit.BYTES)
                .convertTo(MetricUnit.BYTES);
        
        assertEquals("Value is incorrect", Double.valueOf(1024 * 1024 * 1024).doubleValue(), 
                convertedValue.getValue(), 0.001);
        
        assertEquals("Units are incorrect", MetricUnit.BYTES, 
                convertedValue.getUnit());
    }

    @Test
    public void testUnitConversionBytesToMegabytes() {
        System.out.println("testUnitConversionBytesToMegabytes");
        MetricValue convertedValue = null;
        
        convertedValue = new MetricValue(8 * 1024 * 1024, MetricUnit.BYTES)
                .convertTo(MetricUnit.MEGABYTES);
        
        assertEquals("Value is incorrect", Double.valueOf(8).doubleValue(), 
                convertedValue.getValue(), 0.001);
        
        assertEquals("Units are incorrect", MetricUnit.MEGABYTES, 
                convertedValue.getUnit());
    }
    
    @Test
    public void testUnitConversionBytesToGigabytes() {
        System.out.println("testUnitConversionBytesToGigabytes");
        MetricValue convertedValue = null;
        
        convertedValue = new MetricValue(1024 * 1024 * 1024, MetricUnit.BYTES)
                .convertTo(MetricUnit.GIGABYTES);
        
        assertEquals("Value is incorrect", Double.valueOf(1).doubleValue(), 
                convertedValue.getValue(), 0.001);
        
        assertEquals("Units are incorrect", MetricUnit.GIGABYTES, 
                convertedValue.getUnit());
    }
}