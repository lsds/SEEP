/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.evaluate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;
import org.mockito.internal.InOrderImpl;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRule;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRules;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.Operator.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.ScaleConstraint.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor.ScaleFactor.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.MetricThreshold.*;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.TimeThreshold.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.InfrastructureAdaptor;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReading;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReadingProvider;

/**
 *
 * @author martinrouaux
 */
public class PolicyRulesEvaluatorTest {
    
    private InfrastructureAdaptor mockAdaptor = mock(InfrastructureAdaptor.class);
    private MetricReadingProvider mockProvider = mock(MetricReadingProvider.class);
        
    public PolicyRulesEvaluatorTest() {
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
    
    private List<PolicyRule> evaluatorsToRules(List<PolicyRuleEvaluator> evaluators) {
        List<PolicyRule> rules = new ArrayList<PolicyRule>();
        
        for(PolicyRuleEvaluator evaluator : evaluators) {
            rules.add(evaluator.getEvalSubject());
        }
        
        return rules;
    }

    @Test
    public void testAllNullNoExceptionThrown() {
        System.out.println("testAllNullNoExceptionThrown");
        
        try {
            PolicyRulesEvaluator evaluator = new PolicyRulesEvaluator(null, null);
            evaluator.evaluate(null);
        } catch(Exception e) {
            fail("No excetion should be thrown by PolicyRulesEvaluator");
        }
    }
    
    @Test
    public void testEvaluateNoRulesWithoutWildcard() {
        System.out.println("testEvaluateNoRulesWithoutWildcard");
        
        InfrastructureAdaptor mockAdaptor = mock(InfrastructureAdaptor.class);
        MetricReadingProvider mockProvider = mock(MetricReadingProvider.class);
        
        PolicyRules fakeRules = new PolicyRules() {};

        try {
            PolicyRulesEvaluator evaluator = 
                                new PolicyRulesEvaluator(fakeRules, mockAdaptor);
            evaluator.evaluate(mockProvider);
        } catch(Exception e) {
            fail("No excetion should be thrown by PolicyRulesEvaluator");
        }
    }
    
    @Test
    public void testEvaluateNoRulesWithSingleWildcard() {
        System.out.println("testEvaluateNoRulesWithWildcard");
        
        final int operatorId = 1;
        
        MetricReading fakeReading = new MetricReading();
        
        when(mockProvider.getOperatorId()).thenReturn(operatorId);
        when(mockProvider.nextReading()).thenReturn(fakeReading);
        
        PolicyRules fakeRules = new PolicyRules() {{
            rule("CPU above 60% for 30 seconds")
                .scaleOut(allOperators())
                .by(relative(2)).butNeverAbove(nodes(10))
                .when(metric("cpu"))
                .is(above(percent(60)))
                .forAtLeast(seconds(30)).build();
        }};
        
        PolicyRulesEvaluator evaluator = 
                    spy(new PolicyRulesEvaluator(fakeRules, mockAdaptor));
        
        ArgumentCaptor<PolicyRuleEvaluator> captorEvaluator = 
                            ArgumentCaptor.forClass(PolicyRuleEvaluator.class);
        
        doNothing().when(evaluator)
                    .routeReadingToEvaluator(captorEvaluator.capture(), eq(mockProvider));
        
        evaluator.evaluate(mockProvider);
        
        // Assertions, make sure that routing method is invoked on object under test
        verify(evaluator, times(1))
                    .routeReadingToEvaluator((PolicyRuleEvaluator)any(), eq(mockProvider));
        
        assertThat(captorEvaluator.getValue(), is(not(nullValue())));
        assertThat(captorEvaluator.getValue().getEvalSubject(), 
                                    is(equalTo(fakeRules.iterator().next())));        
    }

    @Test
    public void testEvaluateNoRulesWithMultipleWildcard() {
        System.out.println("testEvaluateNoRulesWithMultipleWildcard");
        
        final int operatorId = 1;
        
        MetricReading fakeReading = new MetricReading();
        
        when(mockProvider.getOperatorId()).thenReturn(operatorId);
        when(mockProvider.nextReading()).thenReturn(fakeReading);
        
        PolicyRules fakeRules = new PolicyRules() {{
            rule("CPU above 60% for 30 seconds")
                .scaleOut(allOperators())
                .by(relative(2)).butNeverAbove(nodes(10))
                .when(metric("cpu"))
                .is(above(percent(60)))
                .forAtLeast(seconds(30)).build();
            
            rule("Memory above 1GB for 2 minutes")
                .scaleOut(allOperators())
                .by(relative(4)).butNeverAbove(nodes(10))
                .when(metric("memory"))
                .is(above(gb(1)))
                .forAtLeast(minutes(2)).build();   
        }};
        
        PolicyRulesEvaluator evaluator = 
                    spy(new PolicyRulesEvaluator(fakeRules, mockAdaptor));
        
        ArgumentCaptor<PolicyRuleEvaluator> captorEvaluator = 
                            ArgumentCaptor.forClass(PolicyRuleEvaluator.class);
        
        doNothing().when(evaluator)
                    .routeReadingToEvaluator(captorEvaluator.capture(), eq(mockProvider));
        
        evaluator.evaluate(mockProvider);
        
        // Assertions, make sure that routing method is invoked on object under test
        verify(evaluator, times(2))
                    .routeReadingToEvaluator((PolicyRuleEvaluator)any(), eq(mockProvider));
        
        assertThat(captorEvaluator.getAllValues(), is(not(nullValue())));
        assertThat(captorEvaluator.getAllValues().size(), is(equalTo(2)));
        
        Iterator<PolicyRule> it = fakeRules.iterator();
        PolicyRule rule1 = it.next();
        PolicyRule rule2 = it.next();
        
        List<PolicyRule> actualRules = 
                            evaluatorsToRules(captorEvaluator.getAllValues());
        assertThat(actualRules, containsInAnyOrder(rule1, rule2));       
    }
    
    @Test
    public void testEvaluateSingleRuleWithoutWildcard() {
        System.out.println("testEvaluateSingleRuleWithoutWildcard");
        
        final int operatorId = 1;
        
        MetricReading fakeReading = new MetricReading();
        
        when(mockProvider.getOperatorId()).thenReturn(operatorId);
        when(mockProvider.nextReading()).thenReturn(fakeReading);
        
        PolicyRules fakeRules = new PolicyRules() {{
            rule("CPU above 60% for 30 seconds")
                .scaleOut(operator("Test operator", operatorId))
                .by(relative(2)).butNeverAbove(nodes(10))
                .when(metric("cpu"))
                .is(above(percent(60)))
                .forAtLeast(seconds(30)).build();
        }};
        
        PolicyRulesEvaluator evaluator = 
                    spy(new PolicyRulesEvaluator(fakeRules, mockAdaptor));
        
        ArgumentCaptor<PolicyRuleEvaluator> captorEvaluator = 
                            ArgumentCaptor.forClass(PolicyRuleEvaluator.class);
        
        doNothing().when(evaluator)
                    .routeReadingToEvaluator(captorEvaluator.capture(), eq(mockProvider));
        
        evaluator.evaluate(mockProvider);
        
        // Assertions, make sure that routing method is invoked on object under test
        verify(evaluator, times(1))
                    .routeReadingToEvaluator((PolicyRuleEvaluator)any(), eq(mockProvider));
        
        assertThat(captorEvaluator.getValue(), is(not(nullValue())));
        assertThat(captorEvaluator.getValue().getEvalSubject(), 
                                    is(equalTo(fakeRules.iterator().next())));
    }
    
    @Test
    public void testEvaluateSingleRuleWithWildcard() {
        System.out.println("testEvaluateSingleRuleWithWildcard");
        
        final int operatorId = 1;
        
        MetricReading fakeReading = new MetricReading();
        
        when(mockProvider.getOperatorId()).thenReturn(operatorId);
        when(mockProvider.nextReading()).thenReturn(fakeReading);
        
        PolicyRules fakeRules = new PolicyRules() {{
            rule("CPU above 60% for 30 seconds")
                .scaleOut(operator("Test operator", operatorId))
                .by(relative(2)).butNeverAbove(nodes(10))
                .when(metric("cpu"))
                .is(above(percent(60)))
                .forAtLeast(seconds(30)).build();
            
            rule("Memory above 1GB for 2 minutes")
                .scaleOut(allOperators())
                .by(relative(4)).butNeverAbove(nodes(10))
                .when(metric("memory"))
                .is(above(gb(1)))
                .forAtLeast(minutes(2)).build();  
        }};
        
        PolicyRulesEvaluator evaluator = 
                    spy(new PolicyRulesEvaluator(fakeRules, mockAdaptor));
        
        ArgumentCaptor<PolicyRuleEvaluator> captorEvaluator = 
                            ArgumentCaptor.forClass(PolicyRuleEvaluator.class);
        
        doNothing().when(evaluator)
                    .routeReadingToEvaluator(captorEvaluator.capture(), eq(mockProvider));
        
        evaluator.evaluate(mockProvider);
        
        // Assertions, make sure that routing method is invoked on object under test
        verify(evaluator, times(2))
                    .routeReadingToEvaluator((PolicyRuleEvaluator)any(), eq(mockProvider));
        
        assertThat(captorEvaluator.getAllValues(), is(not(nullValue())));
        assertThat(captorEvaluator.getAllValues().size(), is(equalTo(2)));
        
        Iterator<PolicyRule> it = fakeRules.iterator();
        PolicyRule rule1 = it.next();
        PolicyRule rule2 = it.next();
        
        List<PolicyRule> actualRules = 
                            evaluatorsToRules(captorEvaluator.getAllValues());
        assertThat(actualRules, containsInAnyOrder(rule1, rule2));
    }
    
    @Test
    public void testEvaluateMultipleRulesWithoutWildcard() {
        System.out.println("testEvaluateMultipleRulesWithoutWildcard");
        
        final int[] operatorIds = new int[] {1, 2, 3};
        final MetricReading[] fakeReadings = new MetricReading[] {
                new MetricReading(), new MetricReading(), new MetricReading()};
         
        when(mockProvider.getOperatorId())
                                .thenReturn(operatorIds[0])
                                .thenReturn(operatorIds[1])
                                .thenReturn(operatorIds[2]);
        
        when(mockProvider.nextReading())
                                .thenReturn(fakeReadings[0])
                                .thenReturn(fakeReadings[1])
                                .thenReturn(fakeReadings[2]);
        
        PolicyRules fakeRules = new PolicyRules() {{
            rule("CPU above 60% for 30 seconds")
                .scaleOut(operator("Test operator #1", operatorIds[0]))
                .by(relative(2)).butNeverAbove(nodes(10))
                .when(metric("cpu"))
                .is(above(percent(60)))
                .forAtLeast(seconds(30)).build();
            
            rule("Memory above 1GB for 2 minutes")
                .scaleOut(operator("Test operator #2", operatorIds[1]))
                .by(relative(4)).butNeverAbove(nodes(10))
                .when(metric("memory"))
                .is(above(gb(1)))
                .forAtLeast(minutes(2)).build();
            
            rule("Memory below 512MB for 5 minutes")
                .scaleIn(operator("Test operator #3", operatorIds[2]))
                .by(relative(2))
                .when(metric("memory"))
                .is(below(mb(512)))
                .forAtLeast(minutes(5)).build();
        }};
        
        Iterator<PolicyRule> it = fakeRules.iterator();
        PolicyRule rule1 = it.next();
        PolicyRule rule2 = it.next();
        PolicyRule rule3 = it.next();
        
        PolicyRulesEvaluator evaluator = 
                    spy(new PolicyRulesEvaluator(fakeRules, mockAdaptor));
        
        ArgumentCaptor<PolicyRuleEvaluator> captorEvaluator = 
                            ArgumentCaptor.forClass(PolicyRuleEvaluator.class);
        
        doNothing().when(evaluator)
            .routeReadingToEvaluator(captorEvaluator.capture(), eq(mockProvider));
        
        evaluator.evaluate(mockProvider); // operatorId = 1
        evaluator.evaluate(mockProvider); // operatorId = 2
        evaluator.evaluate(mockProvider); // operatorId = 3
        
        // Make sure all offered readings are routed
        verify(evaluator, times(3))
            .routeReadingToEvaluator((PolicyRuleEvaluator)any(), eq(mockProvider));
        
        assertThat(captorEvaluator.getAllValues(), is(not(nullValue())));
        assertThat(captorEvaluator.getAllValues().size(), is(equalTo(3)));
        
        // Check evaluators are captured for all rules and in the correct order
        List<PolicyRule> actualRules = 
                            evaluatorsToRules(captorEvaluator.getAllValues());
        assertThat(actualRules, contains(rule1, rule2, rule3));
    }
    
    @Test
    public void testEvaluateMultipleRulesWithWildcard() {
        System.out.println("testEvaluateMultipleRulesWithWildcard");
    
        final int[] operatorIds = new int[] {1, 2, 3};
        final MetricReading[] fakeReadings = new MetricReading[] {
                new MetricReading(), new MetricReading(), new MetricReading()};
         
        when(mockProvider.getOperatorId())
                                .thenReturn(operatorIds[0])
                                .thenReturn(operatorIds[1])
                                .thenReturn(operatorIds[2]);
        
        when(mockProvider.nextReading())
                                .thenReturn(fakeReadings[0])
                                .thenReturn(fakeReadings[1])
                                .thenReturn(fakeReadings[2]);
        
        PolicyRules fakeRules = new PolicyRules() {{
            rule("CPU above 60% for 30 seconds")
                .scaleOut(operator("Test operator #1", operatorIds[0]))
                .by(relative(2)).butNeverAbove(nodes(10))
                .when(metric("cpu"))
                .is(above(percent(60)))
                .forAtLeast(seconds(30)).build();
            
            rule("Memory above 1GB for 2 minutes")
                .scaleOut(operator("Test operator #2", operatorIds[1]))
                .by(relative(4)).butNeverAbove(nodes(10))
                .when(metric("memory"))
                .is(above(gb(1)))
                .forAtLeast(minutes(2)).build();
            
            rule("Memory below 512MB for 5 minutes")
                .scaleIn(operator("Test operator #3", operatorIds[2]))
                .by(relative(2))
                .when(metric("memory"))
                .is(below(mb(512)))
                .forAtLeast(minutes(5)).build();
            
            rule("Latency exceeds 500 ms for 3 minutes")
                .scaleOut(allOperators())
                .by(absolute(1)).butNeverAbove(nodes(20))
                .when(metric("latency"))
                .is(above(millis(500)))
                .forAtLeast(minutes(3)).build();  
        }};
        
        Iterator<PolicyRule> it = fakeRules.iterator();
        PolicyRule rule1 = it.next();
        PolicyRule rule2 = it.next();
        PolicyRule rule3 = it.next();
        PolicyRule ruleAll = it.next();
        
        PolicyRulesEvaluator evaluator = 
                    spy(new PolicyRulesEvaluator(fakeRules, mockAdaptor));
        
        ArgumentCaptor<PolicyRuleEvaluator> captorEvaluator = 
                            ArgumentCaptor.forClass(PolicyRuleEvaluator.class);
        
        doNothing().when(evaluator)
            .routeReadingToEvaluator(captorEvaluator.capture(), eq(mockProvider));
        
        evaluator.evaluate(mockProvider); // operatorId = 1
        evaluator.evaluate(mockProvider); // operatorId = 2
        evaluator.evaluate(mockProvider); // operatorId = 3
        
        // Make sure all offered readings are routed
        verify(evaluator, times(6))
            .routeReadingToEvaluator((PolicyRuleEvaluator)any(), eq(mockProvider));
        
        assertThat(captorEvaluator.getAllValues(), is(not(nullValue())));
        assertThat(captorEvaluator.getAllValues().size(), is(equalTo(6)));
        
        // Check evaluators are captured for all rules and in the correct order
        List<PolicyRule> actualRules = 
                            evaluatorsToRules(captorEvaluator.getAllValues());
        
        assertThat(actualRules, contains(rule1, ruleAll,
                                         rule2, ruleAll,
                                         rule3, ruleAll));
    }
    
    @Test
    public void testEvaluateMultipleRulesForSameOperator() {
        System.out.println("testEvaluateMultipleRulesForSameOperator");
        
        final int operatorId = 1;
        
        MetricReading fakeReading = new MetricReading();
        
        when(mockProvider.getOperatorId()).thenReturn(operatorId);
        when(mockProvider.nextReading()).thenReturn(fakeReading);
        
        PolicyRules fakeRules = new PolicyRules() {{
            rule("CPU above 60% for 30 seconds")
                .scaleOut(operator("Test operator", operatorId))
                .by(relative(2)).butNeverAbove(nodes(10))
                .when(metric("cpu"))
                .is(above(percent(60)))
                .forAtLeast(seconds(30)).build();
            
            rule("Memory above 1GB for 2 minutes")
                .scaleOut(operator("Test operator", operatorId))
                .by(relative(4)).butNeverAbove(nodes(10))
                .when(metric("memory"))
                .is(above(gb(1)))
                .forAtLeast(minutes(2)).build();
        }};

        Iterator<PolicyRule> it = fakeRules.iterator();
        PolicyRule rule1 = it.next();
        PolicyRule rule2 = it.next();
        
        PolicyRulesEvaluator evaluator = 
                    spy(new PolicyRulesEvaluator(fakeRules, mockAdaptor));
        
        ArgumentCaptor<PolicyRuleEvaluator> captorEvaluator = 
                            ArgumentCaptor.forClass(PolicyRuleEvaluator.class);
        
        doNothing().when(evaluator)
                    .routeReadingToEvaluator(captorEvaluator.capture(), eq(mockProvider));
        
        evaluator.evaluate(mockProvider);
        
        // Assertions, make sure that routing method is invoked on object under test
        verify(evaluator, times(2))
            .routeReadingToEvaluator((PolicyRuleEvaluator)any(), eq(mockProvider));
        
        assertThat(captorEvaluator.getAllValues(), is(not(nullValue())));
        assertThat(captorEvaluator.getAllValues().size(), is(equalTo(2)));
        
        // Check evaluators are captured for all rules and in the correct order
        List<PolicyRule> actualRules = 
                            evaluatorsToRules(captorEvaluator.getAllValues());
        assertThat(actualRules, contains(rule1, rule2));
    }
    
    @Test
    public void testEvaluateMultipleRulesWithMultipleWildcard() {
        System.out.println("testEvaluateMultipleRulesWithMultipleWildcard");
        
        final int[] operatorIds = new int[] {1, 2, 3};
        final MetricReading[] fakeReadings = new MetricReading[] {
                new MetricReading(), new MetricReading(), new MetricReading()};
         
        when(mockProvider.getOperatorId())
                                .thenReturn(operatorIds[0])
                                .thenReturn(operatorIds[1])
                                .thenReturn(operatorIds[2]);
        
        when(mockProvider.nextReading())
                                .thenReturn(fakeReadings[0])
                                .thenReturn(fakeReadings[1])
                                .thenReturn(fakeReadings[2]);
        
        PolicyRules fakeRules = new PolicyRules() {{
            rule("CPU above 60% for 30 seconds")
                .scaleOut(operator("Test operator #1", operatorIds[0]))
                .by(relative(2)).butNeverAbove(nodes(10))
                .when(metric("cpu"))
                .is(above(percent(60)))
                .forAtLeast(seconds(30)).build();
            
            rule("Memory above 1GB for 2 minutes")
                .scaleOut(operator("Test operator #2", operatorIds[1]))
                .by(relative(4)).butNeverAbove(nodes(10))
                .when(metric("memory"))
                .is(above(gb(1)))
                .forAtLeast(minutes(2)).build();
            
            rule("Memory below 512MB for 5 minutes")
                .scaleIn(operator("Test operator #3", operatorIds[2]))
                .by(relative(2))
                .when(metric("memory"))
                .is(below(mb(512)))
                .forAtLeast(minutes(5)).build();
            
            rule("Latency exceeds 500 ms for 3 minutes")
                .scaleOut(allOperators())
                .by(absolute(1)).butNeverAbove(nodes(20))
                .when(metric("latency"))
                .is(above(millis(500)))
                .forAtLeast(minutes(3)).build();
            
            rule("Queue exceeds 1000 tuples for 1 minute")
                .scaleOut(allOperators())
                .by(absolute(1))
                .when(metric("queue-length"))
                .is(above(tuples(1000)))
                .forAtLeast(seconds(60)).build();  
        }};
        
        Iterator<PolicyRule> it = fakeRules.iterator();
        PolicyRule rule1 = it.next();
        PolicyRule rule2 = it.next();
        PolicyRule rule3 = it.next();
        
        PolicyRule ruleAll1 = it.next();
        PolicyRule ruleAll2 = it.next();
        
        PolicyRulesEvaluator evaluator = 
                    spy(new PolicyRulesEvaluator(fakeRules, mockAdaptor));
        
        ArgumentCaptor<PolicyRuleEvaluator> captorEvaluator = 
                            ArgumentCaptor.forClass(PolicyRuleEvaluator.class);
        
        doNothing().when(evaluator)
            .routeReadingToEvaluator(captorEvaluator.capture(), eq(mockProvider));
        
        evaluator.evaluate(mockProvider); // operatorId = 1
        evaluator.evaluate(mockProvider); // operatorId = 2
        evaluator.evaluate(mockProvider); // operatorId = 3
        
        // Make sure all offered readings are routed
        verify(evaluator, times(9))
            .routeReadingToEvaluator((PolicyRuleEvaluator)any(), eq(mockProvider));
        
        assertThat(captorEvaluator.getAllValues(), is(not(nullValue())));
        assertThat(captorEvaluator.getAllValues().size(), is(equalTo(9)));
        
        // Check evaluators are captured for all rules and in the correct order
        List<PolicyRule> actualRules = 
                            evaluatorsToRules(captorEvaluator.getAllValues());
        
        assertThat(actualRules, contains(rule1, ruleAll1, ruleAll2,
                                         rule2, ruleAll1, ruleAll2,
                                         rule3, ruleAll1, ruleAll2));
    }
}