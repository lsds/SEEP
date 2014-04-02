/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author mrouaux
 */
public class RelativeScaleConstraintTest {
    
    public RelativeScaleConstraintTest() {
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
    public void testRelativeConstraintCreation() {
        System.out.println("testRelativeConstraintCreation");
        
        int expectedConstraintSize = 2;
        int expectedOriginalSize = 5;
        
        RelativeScaleConstraint constraint = new RelativeScaleConstraint(expectedConstraintSize)
                                        .withOriginalSize(expectedOriginalSize);
        
        assertEquals("Actual original size does not match expectation", 
                        expectedOriginalSize, constraint.getOriginalSize());
        
        assertEquals("Actual constraint size does not match expectation", 
                        expectedConstraintSize, new Double(constraint.getValue()).intValue());
    }
    
    @Test
    public void testRelativeConstraintExceeded() {
        System.out.println("testRelativeConstraintExceeded");
        
        RelativeScaleConstraint constraint;
        
        constraint = new RelativeScaleConstraint(10).withOriginalSize(1);
        assertFalse("Constraint should not be exceeded by 1", constraint.evaluate(1));
        assertFalse("Constraint should not be exceeded by 5", constraint.evaluate(5));
        assertFalse("Constraint should not be exceeded by 10", constraint.evaluate(10));
        assertTrue("Constraint should be exceeded by 20", constraint.evaluate(20));
        
        constraint = new RelativeScaleConstraint(10).withOriginalSize(5);
        assertFalse("Constraint should not be exceeded by 5", constraint.evaluate(5));
        assertFalse("Constraint should not be exceeded by 10", constraint.evaluate(10));
        assertFalse("Constraint should not be exceeded by 50", constraint.evaluate(50));
        assertTrue("Constraint should be exceeded by 51", constraint.evaluate(51));
    }
}
