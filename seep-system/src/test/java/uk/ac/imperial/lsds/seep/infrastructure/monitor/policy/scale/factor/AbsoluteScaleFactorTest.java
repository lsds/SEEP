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
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.ScaleOutAction;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.AbsoluteScaleConstraint;

/**
 *
 * @author mrouaux
 */
public class AbsoluteScaleFactorTest {
    
    public AbsoluteScaleFactorTest() {
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
    public void testAbsoluteFactorCreation() {
        System.out.println("testAbsoluteFactorCreation");
        
        int expectedFactor = 10;
        AbsoluteScaleFactor factor = new AbsoluteScaleFactor(expectedFactor);
        
        assertEquals("Actual factor does not match expectation", 
                        expectedFactor, new Double(factor.getFactor()).intValue());
    }
    
    @Test
    public void testAbsoluteFactorApplyWithScaleOut() {
        System.out.println("testAbsoluteFactorApply");
        
        AbsoluteScaleFactor factor;
        
        factor = new AbsoluteScaleFactor(10);
        assertEquals("New scaling size is incorrect", 11, factor.apply(1, new ScaleOutAction()));
        assertEquals("New scaling size is incorrect", 20, factor.apply(10, new ScaleOutAction()));
        assertEquals("New scaling size is incorrect", 15, factor.apply(5, new ScaleOutAction()));
    }
}