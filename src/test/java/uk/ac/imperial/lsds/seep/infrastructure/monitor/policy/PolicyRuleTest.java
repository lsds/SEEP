package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy;


import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.Action;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.Operator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.ScaleConstraint;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor.ScaleFactor;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.TimeThreshold;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.trigger.ActionTrigger;

/**
 *
 * @author mrouaux
 */
public class PolicyRuleTest {
    
    public PolicyRuleTest() {
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
    public void testCreateDefaultInstance() {
        System.out.println("testCreateDefaultInstance");
        PolicyRule rule = new PolicyRule();
        
        assertEquals("Name does not match expectation", 
                PolicyRule.DEFAULT_POLICY_RULE_NAME, rule.getName());
        
        assertNull("Action is expected to be null", rule.getAction());
        assertNull("Operator is expected to be null", rule.getOperator());
        
        assertNotNull("Triggers are not expected to be null", rule.getTriggers());
        assertTrue("Triggers are expected to be empty", rule.getTriggers().isEmpty());
        
        assertNull("Scale factor is expected to be null", rule.getScaleFactor());
        assertNull("Scale constraint is expected to be null", rule.getScaleConstraint());
        assertNull("Scale guard time is expected to be null", rule.getScaleGuardTime());
    }
    
    @Test
    public void testCreateCustomInstance() {
        System.out.println("testCreateCustomInstance");
        
        String expectedName = "Sample test rule";
        Action expectedAction = mock(Action.class);
        Operator expectedOperator = mock(Operator.class);
        
        List<ActionTrigger> expectedTriggers = new ArrayList<ActionTrigger>();
        expectedTriggers.add(mock(ActionTrigger.class));
                
        ScaleFactor expectedScaleFactor = mock(ScaleFactor.class);
        ScaleConstraint expectedScaleConstraint = mock(ScaleConstraint.class);
        TimeThreshold expectedScaleGuardTime = mock(TimeThreshold.class);
        
        PolicyRule rule = new PolicyRule(
                                    expectedName,
                                    expectedAction,
                                    expectedTriggers,                
                                    expectedOperator,
                                    expectedScaleFactor,
                                    expectedScaleConstraint,
                                    expectedScaleGuardTime);
        
        String actualName = rule.getName();
        Action actualAction = rule.getAction();
        Operator actualOperator = rule.getOperator();
        List<ActionTrigger> actualTriggers = rule.getTriggers();
        ScaleFactor actualScaleFactor = rule.getScaleFactor();
        ScaleConstraint actualScaleConstraint = rule.getScaleConstraint();
        TimeThreshold actualScaleGuardTime = rule.getScaleGuardTime();
        
        assertEquals("Name does not match expectations", 
                                expectedName, actualName);
        
        assertEquals("Action does not match expectations", 
                                expectedAction, actualAction);
        
        assertEquals("Operator does not match expectations", 
                                expectedOperator, actualOperator);
        
        assertEquals("Triggers do not match expectations", 
                                expectedTriggers, actualTriggers);
        
        assertEquals("Scale factor does not match expectations", 
                                expectedScaleFactor, actualScaleFactor);
        
        assertEquals("Scale constraint does not match expectations", 
                                expectedScaleConstraint, actualScaleConstraint);
        
        assertEquals("Scale guard time does not match expectations", 
                                expectedScaleGuardTime, actualScaleGuardTime);
    }
}
