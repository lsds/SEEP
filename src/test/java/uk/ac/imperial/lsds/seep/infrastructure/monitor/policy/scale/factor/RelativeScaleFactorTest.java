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
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.ScaleInAction;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.ScaleOutAction;

/**
 *
 * @author mrouaux
 */
public class RelativeScaleFactorTest {
    
    public RelativeScaleFactorTest() {
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
    public void testRelativeFactorCreation() {
        System.out.println("testRelativeFactorCreation");
        
        int expectedFactor = 2;
        RelativeScaleFactor factor = new RelativeScaleFactor(expectedFactor);
        
        assertEquals("Actual factor does not match expectation", 
                        expectedFactor, new Double(factor.getFactor()).intValue());
    }
    
    @Test
    public void testRelativeFactorApplyWithScaleOut() {
        System.out.println("testRelativeFactorApply");
        
        RelativeScaleFactor factor;
        
        factor = new RelativeScaleFactor(2);
        assertEquals("New scaling size is incorrect", 2, factor.apply(1, new ScaleOutAction()));
        assertEquals("New scaling size is incorrect", 4, factor.apply(2, new ScaleOutAction()));
        assertEquals("New scaling size is incorrect", 10, factor.apply(5, new ScaleOutAction()));
    }
    
    @Test
    public void testRelativeFactorApplyWithScaleIn() {
        System.out.println("testRelativeFactorApply");
        
        RelativeScaleFactor factor;
        
        factor = new RelativeScaleFactor(2);
        assertEquals("New scaling size is incorrect", 5, factor.apply(10, new ScaleInAction()));
        assertEquals("New scaling size is incorrect", 2, factor.apply(4, new ScaleInAction()));
        assertEquals("New scaling size is incorrect", 1, factor.apply(2, new ScaleInAction()));
        
        // Need to ensure that scaling never goes below 1 for ScaleInAction
        assertEquals("New size is less than 1 after scaling in", 
                                        1, factor.apply(1, new ScaleInAction()));
    }
}