/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.ScaleConstraint.*;

/**
 *
 * @author mrouaux
 */
public class ScaleConstraintTest {
    
    public ScaleConstraintTest() {
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
    public void testCreateAbsoluteConstraint() {
        System.out.println("testCreateAbsoluteConstraint");
        
        int expectedConstraint = 100;
        ScaleConstraint constraint = nodes(expectedConstraint);
        
        assertTrue("Returned object is of incorrect type", 
                        constraint instanceof AbsoluteScaleConstraint);
        
        assertEquals("Actual factor does not match expectation", 
                        expectedConstraint, new Double(constraint.getValue()).intValue());
    }
    
    @Test
    public void testCreateRelativeConstraint() {
        System.out.println("testCreateRelativeConstraint");
        
        int expectedConstraint = 2;
        ScaleConstraint constraint = factor(expectedConstraint);
        
        assertTrue("Returned object is of incorrect type", 
                        constraint instanceof RelativeScaleConstraint);
        
        assertEquals("Actual factor does not match expectation", 
                        expectedConstraint, new Double(constraint.getValue()).intValue());
    }    
}