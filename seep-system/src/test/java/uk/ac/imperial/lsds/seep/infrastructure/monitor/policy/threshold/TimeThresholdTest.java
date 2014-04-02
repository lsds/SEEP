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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold;

import org.joda.time.Period;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mrouaux
 */
public class TimeThresholdTest {
    
    public TimeThresholdTest() {
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
    public void testThresholdEvaluateTrue() {
        System.out.println("testThresholdEvaluateTrue");
        
        TimeThreshold threshold = new TimeThreshold(Period.minutes(5));
        
        assertTrue(threshold.evaluate(Period.ZERO));
        assertTrue(threshold.evaluate(Period.minutes(0)));
        assertTrue(threshold.evaluate(Period.minutes(1)));
        assertTrue(threshold.evaluate(Period.minutes(4)));
        assertTrue(threshold.evaluate(Period.seconds(30)));
        assertTrue(threshold.evaluate(Period.millis(30)));
    }
    
    @Test
    public void testThresholdEvaluateFalse() {
        System.out.println("testThresholdEvaluateFalse");
        
        TimeThreshold threshold = new TimeThreshold(Period.minutes(1));
   
        assertFalse(threshold.evaluate(Period.seconds(90)));
        assertFalse(threshold.evaluate(Period.seconds(120)));
        assertFalse(threshold.evaluate(Period.minutes(2)));
        assertFalse(threshold.evaluate(Period.minutes(5)));
    }
}
