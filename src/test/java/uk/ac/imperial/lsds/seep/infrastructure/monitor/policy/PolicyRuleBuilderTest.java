package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.ScaleInAction;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.ScaleOutAction;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.Operator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.ScaleConstraint;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor.ScaleFactor;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.MetricThreshold;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.TimeThreshold;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.trigger.ActionTrigger;

/**
 *
 * @author mrouaux
 */
public class PolicyRuleBuilderTest {
    
    public PolicyRuleBuilderTest() {
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
    public void testBuildSimpleRule() {
        System.out.println("testBuildSimpleRule");
        
        String ruleName = "Test name";
        Operator operator = mock(Operator.class);
        MetricName metricName = MetricName.CPU_UTILIZATION;
        
        TimeThreshold timeThreshold = mock(TimeThreshold.class);
        MetricThreshold metricThreshold = mock(MetricThreshold.class);
        
        ScaleFactor scaleFactor = mock(ScaleFactor.class);
        ScaleConstraint scaleConstraint = mock(ScaleConstraint.class);
        TimeThreshold scaleGuardTime = mock(TimeThreshold.class);

        PolicyRule rule = new PolicyRuleBuilder(ruleName)
                            .scaleIn(operator)
                            .by(scaleFactor)
                            .butNeverAbove(scaleConstraint)
                            .when(metricName)
                            .is(metricThreshold)
                            .forAtLeast(timeThreshold)
                            .withNoScaleOutSince(scaleGuardTime).build();
        
        assertEquals("Name does not match expectations", ruleName, rule.getName());
        
        assertNotNull("Action should not be null", rule.getAction());

        assertEquals("Operator does not match expectations", 
                                        operator, rule.getOperator());
        
        assertNotNull("Triggers should not be null", rule.getTriggers());
        assertFalse("Triggers should not be empty", rule.getTriggers().isEmpty());
        assertEquals("Triggers should have one item", 1, rule.getTriggers().size());
                
        // Assert that the trigger for the rule is correct
        ActionTrigger expectedTrigger = new ActionTrigger(
                                        metricThreshold, timeThreshold, metricName);
        
        assertEquals("Triger does not match expectations",
                                    expectedTrigger, rule.getTriggers().get(0));
        
        assertEquals("Scale factor does not match expectations", 
                                    scaleFactor, rule.getScaleFactor());
        
        assertEquals("Scale constraint does not match expectations", 
                                    scaleConstraint, rule.getScaleConstraint());
        
        assertEquals("Scale guard time does not match expectations", 
                                    scaleGuardTime, rule.getScaleGuardTime());
    }
    
    @Test
    public void testBuildSingleScaleInRule() {
        System.out.println("testBuildSingleScaleInRule");
        
        PolicyRuleBuilder builder = new PolicyRuleBuilder();
        
        Operator operator = mock(Operator.class);
        MetricName metric = MetricName.CPU_UTILIZATION;
        
        TimeThreshold timeThreshold = mock(TimeThreshold.class);
        MetricThreshold metricThreshold = mock(MetricThreshold.class);
        
        ScaleFactor scaleFactor = mock(ScaleFactor.class);
        ScaleConstraint scaleConstraint = mock(ScaleConstraint.class);
        TimeThreshold scaleGuardTime = mock(TimeThreshold.class);

        PolicyRule rule = builder
                            .scaleIn(operator)
                            .by(scaleFactor)
                            .butNeverAbove(scaleConstraint)
                            .when(metric)
                            .is(metricThreshold)
                            .forAtLeast(timeThreshold).build();

        assertNotNull("Action should not be null", rule.getAction());
        
        assertTrue("Action is not of expected type", 
                                    rule.getAction() instanceof ScaleInAction);
    }
    
    @Test
    public void testBuildSingleScaleOutRule() {
        System.out.println("testBuildSingleScaleOutRule");
        
        PolicyRuleBuilder builder = new PolicyRuleBuilder();
        
        Operator operator = mock(Operator.class);
        MetricName metric = MetricName.CPU_UTILIZATION;
        
        TimeThreshold timeThreshold = mock(TimeThreshold.class);
        MetricThreshold metricThreshold = mock(MetricThreshold.class);
        
        ScaleFactor scaleFactor = mock(ScaleFactor.class);
        ScaleConstraint scaleConstraint = mock(ScaleConstraint.class);
        TimeThreshold scaleGuardTime = mock(TimeThreshold.class);

        PolicyRule rule = builder
                            .scaleOut(operator)
                            .by(scaleFactor)
                            .butNeverAbove(scaleConstraint)
                            .when(metric)
                            .is(metricThreshold)
                            .forAtLeast(timeThreshold).build();

        assertNotNull("Action should not be null", rule.getAction());
        
        assertTrue("Action is not of expected type", 
                                    rule.getAction() instanceof ScaleOutAction);
    }
    
    @Test
    public void testBuildSingleScaleRuleNoConstraint() {
        System.out.println("testBuildSingleScaleRuleNoConstraint");
        
        PolicyRuleBuilder builder = new PolicyRuleBuilder();
        
        Operator operator = mock(Operator.class);
        MetricName metric = MetricName.CPU_UTILIZATION;
        
        TimeThreshold timeThreshold = mock(TimeThreshold.class);
        MetricThreshold metricThreshold = mock(MetricThreshold.class);
        
        ScaleFactor scaleFactor = mock(ScaleFactor.class);
        ScaleConstraint scaleConstraint = mock(ScaleConstraint.class);
        TimeThreshold scaleGuardTime = mock(TimeThreshold.class);

        PolicyRule rule = builder
                            .scaleOut(operator)
                            .by(scaleFactor)
                            .when(metric)
                            .is(metricThreshold)
                            .forAtLeast(timeThreshold).build();

        assertNull("Scale constraint should be null", rule.getScaleConstraint());
    }
    
    @Test
    public void testBuildSingleScaleRuleWithListener() {
        System.out.println("testBuildSingleScaleRuleWithListener");
        
        Operator operator = mock(Operator.class);
        MetricName metric = MetricName.CPU_UTILIZATION;
        
        TimeThreshold timeThreshold = mock(TimeThreshold.class);
        MetricThreshold metricThreshold = mock(MetricThreshold.class);
        
        ScaleFactor scaleFactor = mock(ScaleFactor.class);
        ScaleConstraint scaleConstraint = mock(ScaleConstraint.class);
        TimeThreshold scaleGuardTime = mock(TimeThreshold.class);
        
        ArgumentCaptor<PolicyRule> captureRule = 
                        ArgumentCaptor.forClass(PolicyRule.class);
        
        // Mock listener and capture built rule when callback method is invoked
        PolicyRuleBuilder.PolicyRuleBuilderListener listener =
                        mock(PolicyRuleBuilder.PolicyRuleBuilderListener.class);
        doNothing().when(listener).onRuleBuilt(captureRule.capture());
        
        PolicyRule rule = new PolicyRuleBuilder("Test name", listener)
                            .scaleOut(operator)
                            .by(scaleFactor)
                            .when(metric)
                            .is(metricThreshold)
                            .forAtLeast(timeThreshold).build();
    
        assertNotNull("Rule should not be null", captureRule.getValue());
        
        assertEquals("Listener rule should be the same as returned by builder",
                rule, captureRule.getValue());
    }
    
    @Test
    public void testBuildMultipleTriggerScaleRule() {
        System.out.println("testBuildMultipleTriggerScaleRule");
        
        PolicyRuleBuilder builder = new PolicyRuleBuilder();
        
        Operator operator = mock(Operator.class);

        MetricName metricName1 = MetricName.CPU_UTILIZATION;
        TimeThreshold timeThreshold1 = mock(TimeThreshold.class);
        MetricThreshold metricThreshold1 = mock(MetricThreshold.class);
        
        MetricName metricName2 = MetricName.OPERATOR_LATENCY;
        TimeThreshold timeThreshold2 = mock(TimeThreshold.class);
        MetricThreshold metricThreshold2 = mock(MetricThreshold.class);
        
        MetricName metricName3 = MetricName.HEAP_SIZE;
        TimeThreshold timeThreshold3 = mock(TimeThreshold.class);
        MetricThreshold metricThreshold3 = mock(MetricThreshold.class);

        ScaleFactor scaleFactor = mock(ScaleFactor.class);
        ScaleConstraint scaleConstraint = mock(ScaleConstraint.class);
        TimeThreshold scaleGuardTime = mock(TimeThreshold.class);

        PolicyRule rule = builder
                            .scaleIn(operator)
                            .by(scaleFactor)
                            .butNeverAbove(scaleConstraint)
                            .when(metricName1)
                                .is(metricThreshold1)
                                .forAtLeast(timeThreshold1)
                            .and(metricName2)
                                .is(metricThreshold2)
                                .forAtLeast(timeThreshold2)
                            .and(metricName3)
                                .is(metricThreshold3)
                                .forAtLeast(timeThreshold3)
                            .build();

        assertNotNull("Triggers should not be null", rule.getTriggers());
        assertFalse("Triggers should not be empty", rule.getTriggers().isEmpty());
        assertEquals("Triggers should have 3 items", 3, rule.getTriggers().size());
                
        // Assert that the triggers for the rule are correct
        assertEquals("Triger[0] does not match expectations",
                            new ActionTrigger(metricThreshold1, timeThreshold1, metricName1), 
                            rule.getTriggers().get(0));
    
        assertEquals("Triger[1] does not match expectations",
                            new ActionTrigger(metricThreshold2, timeThreshold2, metricName2), 
                            rule.getTriggers().get(1));
        
        assertEquals("Triger[2] does not match expectations",
                            new ActionTrigger(metricThreshold3, timeThreshold3, metricName3), 
                            rule.getTriggers().get(2));
    }    
}