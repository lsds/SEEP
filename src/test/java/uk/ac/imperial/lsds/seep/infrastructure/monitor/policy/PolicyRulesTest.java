package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy;

import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.ScaleInAction;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.ScaleOutAction;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.OneOperator;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.Operator.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.AbsoluteScaleConstraint;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.ScaleConstraint.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor.RelativeScaleFactor;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor.ScaleFactor.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.MetricThreshold.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.TimeThreshold.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.trigger.ActionTrigger;

/**
 *
 * @author martinrouaux
 */
public class PolicyRulesTest {
    
    public PolicyRulesTest() {
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
    public void testCreatePolicyWithOneRule() {
        PolicyRules rules = new PolicyRules() {{
            
            rule("CPU above 60% for 30 seconds")
                .scaleOut(operator("test-operator", 1))
                .by(relative(2)).butNeverAbove(nodes(10))
                .when(metric("cpu"))
                .is(above(percent(60)))
                .forAtLeast(seconds(30)).build();
        }};
        
        PolicyRule rule = rules.iterator().next();
        
        assertNotNull("Operator should not be null", rule.getOperator());
        assertTrue("Operator is of incorrect type", 
                                rule.getOperator() instanceof OneOperator);
        assertEquals("Operator name is incorrect", "test-operator",
                                ((OneOperator)rule.getOperator()).getName());
        assertEquals("Operator identifier is incorrect", 1,
                                ((OneOperator)rule.getOperator()).getId());
        
        assertEquals("Name does not match expectations", 
                                "CPU above 60% for 30 seconds", rule.getName());
        
        assertTrue("Action is not of expected type", 
                                rule.getAction() instanceof ScaleOutAction);
        
        assertTrue("Scale factor is not of expected type", 
                                rule.getScaleFactor() instanceof RelativeScaleFactor);
        assertEquals("Scale factor does not match expectation", 
                Double.valueOf(2), Double.valueOf(rule.getScaleFactor().getFactor()));
        
        assertTrue("Scale constraint is not of expected type", 
                                rule.getScaleConstraint() instanceof AbsoluteScaleConstraint);
        assertEquals("Scale constraint does not match expectation", 
                Double.valueOf(10), Double.valueOf(rule.getScaleConstraint().getValue()));
        
        ActionTrigger expectedTrigger = 
                        new ActionTrigger(above(percent(60)), 
                            seconds(30), MetricName.CPU_UTILIZATION);
        assertEquals("Trigger[0] do not match expectations", 
                            expectedTrigger, rule.getTriggers().get(0));
    }
    
    @Test
    public void testCreatePolicyWithMultipleRule() {
        ActionTrigger expectedTrigger;
        
        PolicyRules rules = new PolicyRules() {{
            
            rule("CPU above 60% for 30 seconds")
                .scaleOut(new OneOperator())
                .by(relative(2)).butNeverAbove(nodes(10))
                .when(metric("cpu"))
                .is(above(percent(60)))
                .forAtLeast(seconds(30)).build();
            
            rule("Operator latency below 500ms for 2 minutes")
                .scaleIn(new OneOperator())
                .by(relative(0.5)).butNeverBelow(nodes(1))
                .when(metric("latency"))
                .is(below(millis(500)))
                .forAtLeast(minutes(2)).build();
        }};
        
        // Check assertions on first rule
        Iterator<PolicyRule> it = rules.iterator();
        PolicyRule rule1 = it.next();
        
        assertEquals("Rule 1: name does not match expectations", 
                                "CPU above 60% for 30 seconds", rule1.getName());
        
        assertTrue("Rule 1: action is not of expected type", 
                                rule1.getAction() instanceof ScaleOutAction);
        
        assertTrue("Rule 1: scale factor is not of expected type", 
                                rule1.getScaleFactor() instanceof RelativeScaleFactor);
        assertEquals("Rule 1: scale factor does not match expectation", 
                Double.valueOf(2), Double.valueOf(rule1.getScaleFactor().getFactor()));
        
        assertTrue("Rule 1: scale constraint is not of expected type", 
                                rule1.getScaleConstraint() instanceof AbsoluteScaleConstraint);
        assertEquals("Rule 1: scale constraint does not match expectation", 
                Double.valueOf(10), Double.valueOf(rule1.getScaleConstraint().getValue()));
        
        expectedTrigger = new ActionTrigger(above(percent(60)), 
                            seconds(30), MetricName.CPU_UTILIZATION);
        assertEquals("Rule 1: trigger[0] do not match expectations", 
                            expectedTrigger, rule1.getTriggers().get(0));        
        
        // Check assertions on second rule     
        PolicyRule rule2 = it.next();
        
        assertEquals("Rule 2: name does not match expectations", 
                                "Operator latency below 500ms for 2 minutes", rule2.getName());
        
        assertTrue("Rule 2: action is not of expected type", 
                                rule2.getAction() instanceof ScaleInAction);
        
        assertTrue("Rule 2: scale factor is not of expected type", 
                                rule2.getScaleFactor() instanceof RelativeScaleFactor);
        assertEquals("Rule 2: scale factor does not match expectation", 
                Double.valueOf(0.5), Double.valueOf(rule2.getScaleFactor().getFactor()));
        
        assertTrue("Rule 2: scale constraint is not of expected type", 
                                rule2.getScaleConstraint() instanceof AbsoluteScaleConstraint);
        assertEquals("Rule 2: scale constraint does not match expectation", 
                Double.valueOf(1), Double.valueOf(rule2.getScaleConstraint().getValue()));
        
        expectedTrigger = new ActionTrigger(below(millis(500)), 
                            minutes(2), MetricName.OPERATOR_LATENCY);
        assertEquals("Rule 2: trigger[0] do not match expectations", 
                            expectedTrigger, rule2.getTriggers().get(0));        
        
    }
}