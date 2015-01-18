/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor.ScaleFactor.*;

/**
 *
 * @author mrouaux
 */
public class ScaleFactorTest {
    
    public ScaleFactorTest() {
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
    public void testCreateAbsoluteFactor() {
        System.out.println("testCreateAbsoluteFactor");
        
        int expectedFactor = 100;
        ScaleFactor factor = absolute(expectedFactor);
        
        assertTrue("Returned object is of incorrect type", 
                        factor instanceof AbsoluteScaleFactor);
        
        assertEquals("Actual factor does not match expectation", 
                        expectedFactor, new Double(factor.getFactor()).intValue());
    }
    
    @Test
    public void testCreateRelativeFactor() {
        System.out.println("testCreateRelativeFactor");
        
        int expectedFactor = 2;
        ScaleFactor factor = relative(expectedFactor);
        
        assertTrue("Returned object is of incorrect type", 
                        factor instanceof RelativeScaleFactor);
        
        assertEquals("Actual factor does not match expectation", 
                        expectedFactor, new Double(factor.getFactor()).intValue());
    }    
}