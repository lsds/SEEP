package uk.ac.imperial.lsds.seep.infrastructure.monitor.master;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRules;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.evaluate.AbstractEvaluator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.InfrastructureAdaptor;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReading;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReadingProvider;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.MonitorSlave;

/**
 *
 * @author mrouaux
 */
public class MonitorMasterAndSlaveTest {
    
    public MonitorMasterAndSlaveTest() {
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
    public void testSingleSlaveConnectToMaster() throws InterruptedException {
        System.out.println("testSingleSlaveConnectToMaster");
        
        InfrastructureAdaptor mockAdaptor = mock(InfrastructureAdaptor.class);
        PolicyRules mockRules = mock(PolicyRules.class);
        
        int masterPort = 63000;
        int operatorId = 1;
        
        // Create mock evaluator and capture the metric readind provider
        AbstractEvaluator mockEvaluator = mock(AbstractEvaluator.class);
        ArgumentCaptor<MetricReadingProvider> captorProvider 
                = ArgumentCaptor.forClass(MetricReadingProvider.class);
        doNothing().when(mockEvaluator).evaluate(captorProvider.capture());
        
        // Initialise and start the master process
        MonitorMaster master = spy(new MonitorMaster(mockAdaptor, mockRules, masterPort));
        
        when(master.createEvaluator(eq(mockAdaptor), eq(mockRules)))
                .thenReturn(mockEvaluator);
        
        new Thread(master).start();
        Thread.sleep(100);
        
        // Initialise and start one slave process
        MonitorSlave slave = new MonitorSlave(operatorId, "localhost", masterPort, 1);
        new Thread(slave).start();
        
        // Let the two threads run for a few seconds
        Thread.sleep(1500);
        
        slave.stop();
        master.stop();

        Thread.sleep(100);

        verify(mockEvaluator, atLeastOnce()).evaluate(any(MetricReadingProvider.class));
        
        assertNotNull("MetricReadingProvider for evaluator should not be null",
                captorProvider.getAllValues());
        
        assertTrue("MetricReadingProvider for evaluator should have captured ",
                captorProvider.getAllValues().size() > 0);
        
        // Check the operator identifier and readings for all cpatured values
        for(MetricReadingProvider provider : captorProvider.getAllValues()) {
            assertEquals(operatorId, provider.getOperatorId());

            MetricReading reading = provider.nextReading();
            assertNotNull("The metric reading is not expected to be null", reading);
        }
    }
    
    @Test
    public void testMultipleSlavesConnectToMaster() throws InterruptedException {
        System.out.println("testMultipleSlavesConnectToMaster");
        
        InfrastructureAdaptor mockAdaptor = mock(InfrastructureAdaptor.class);
        PolicyRules mockRules = mock(PolicyRules.class);
        
        int masterPort = 63001;
        int operatorId1 = 100;
        int operatorId2 = 200;
        
        // Create mock evaluator and capture the metric readind provider
        AbstractEvaluator mockEvaluator = mock(AbstractEvaluator.class);
        ArgumentCaptor<MetricReadingProvider> captorProvider 
                = ArgumentCaptor.forClass(MetricReadingProvider.class);
        doNothing().when(mockEvaluator).evaluate(captorProvider.capture());
        
        // Initialise and start the master process
        MonitorMaster master = spy(new MonitorMaster(mockAdaptor, mockRules, masterPort));
        
        when(master.createEvaluator(eq(mockAdaptor), eq(mockRules)))
                .thenReturn(mockEvaluator);
        
        new Thread(master).start();
        Thread.sleep(100);
        
        // Initialise and start the first slave process
        MonitorSlave slave1 = new MonitorSlave(operatorId1, "localhost", masterPort, 1);
        new Thread(slave1).start();
        
        // Initialise and start the first slave process
        MonitorSlave slave2 = new MonitorSlave(operatorId2, "localhost", masterPort, 1);
        new Thread(slave2).start();
        
        // Let the two threads run for a few seconds
        Thread.sleep(3000);

        slave1.stop();
        slave2.stop();
        
        master.stop();
        
        Thread.sleep(100);

        verify(mockEvaluator, atLeastOnce()).evaluate(any(MetricReadingProvider.class));
        
        // Check the operator identifier and readings for all cpatured values
        boolean operatorId1Found = false;
        boolean operatorId2Found = false;
        
        for(MetricReadingProvider provider : captorProvider.getAllValues()) {
            if (!operatorId1Found && (provider.getOperatorId() == operatorId1)) {
                operatorId1Found = true;
            }
            
            if (!operatorId2Found && (provider.getOperatorId() == operatorId2)) {
                operatorId2Found = true;
            }
            
            MetricReading reading = provider.nextReading();
            assertNotNull("The metric reading is not expected to be null", reading);
        }
        
        assertTrue("Operator identifier for first slave not found", operatorId1Found);       
        assertTrue("Operator identifier for second slave not found", operatorId2Found);
    }
}
