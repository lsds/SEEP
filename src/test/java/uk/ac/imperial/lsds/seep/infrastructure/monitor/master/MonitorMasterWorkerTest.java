/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.master;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsDeserializer;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTuple;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.evaluate.AbstractEvaluator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReadingProvider;

/**
 *
 * @author mrouaux
 */
public class MonitorMasterWorkerTest {

    public MonitorMasterWorkerTest() {
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
    public void testReceiveTupleWithSingleValueAndEvaluate() throws InterruptedException {
        Socket mockSocket = mock(Socket.class);
        MetricsDeserializer mockDeserializer = mock(MetricsDeserializer.class);
        AbstractEvaluator mockEvaluator = mock(AbstractEvaluator.class);

        MonitorMasterWorker worker = new MonitorMasterWorker(mockSocket,
                mockDeserializer, mockEvaluator);

        SocketAddress fakeAddres = new InetSocketAddress("fake.host.com", 9999);
        MetricsTuple fakeTuple = new MetricsTuple();
        fakeTuple.setOperatorId(1);
        fakeTuple.setMetricValue(MetricName.CPU_UTILIZATION, percent(50));
        
        when(mockSocket.getRemoteSocketAddress()).thenReturn(fakeAddres);
        
        when(mockDeserializer.deserialize())
                .thenReturn(fakeTuple)
                .thenReturn(null);
        
        ArgumentCaptor<MetricReadingProvider> providerCaptor 
                = ArgumentCaptor.forClass(MetricReadingProvider.class);
        
        doNothing().when(mockEvaluator).evaluate(providerCaptor.capture());

        // Start the worker in its own thread and stop it after a few milliseconds
        new Thread(worker).start();
        Thread.sleep(10);
        worker.stop();
        
        // Check that the provider delivers the correct data to the policy evaluator
        assertNotNull(providerCaptor.getValue());
        
        assertEquals(fakeTuple.getOperatorId(), providerCaptor.getValue().getOperatorId());
        
        Map<MetricName, MetricValue> actualValues 
                = providerCaptor.getValue().nextReading().getValues();
        
        assertTrue("CPU_UTILIZATION missing from metric reading", 
                actualValues.containsKey(MetricName.CPU_UTILIZATION));
        
        assertEquals("CPU_UTILIZATION value different in metric reading", 
                fakeTuple.getMetricValue(MetricName.CPU_UTILIZATION), 
                actualValues.get(MetricName.CPU_UTILIZATION));
    }

    @Test
    public void testReceiveTupleWithMultipleValuesAndEvaluate() throws InterruptedException {
        Socket mockSocket = mock(Socket.class);
        MetricsDeserializer mockDeserializer = mock(MetricsDeserializer.class);
        AbstractEvaluator mockEvaluator = mock(AbstractEvaluator.class);

        MonitorMasterWorker worker = new MonitorMasterWorker(mockSocket,
                mockDeserializer, mockEvaluator);

        SocketAddress fakeAddres = new InetSocketAddress("fake.host.com", 9999);
        MetricsTuple fakeTuple = new MetricsTuple();
        fakeTuple.setOperatorId(1);
        fakeTuple.setMetricValue(MetricName.CPU_UTILIZATION, percent(50));
        fakeTuple.setMetricValue(MetricName.HEAP_UTILIZATION, percent(80));
        fakeTuple.setMetricValue(MetricName.OPERATOR_LATENCY, millis(100));
        
        when(mockSocket.getRemoteSocketAddress()).thenReturn(fakeAddres);
        
        when(mockDeserializer.deserialize())
                .thenReturn(fakeTuple)
                .thenReturn(null);
        
        ArgumentCaptor<MetricReadingProvider> providerCaptor 
                = ArgumentCaptor.forClass(MetricReadingProvider.class);
        
        doNothing().when(mockEvaluator).evaluate(providerCaptor.capture());

        // Start the worker in its own thread and stop it after a few milliseconds
        new Thread(worker).start();
        Thread.sleep(10);
        worker.stop();
        
        // Check that the provider delivers the correct data to the policy evaluator
        assertNotNull(providerCaptor.getValue());
        
        assertEquals(fakeTuple.getOperatorId(), providerCaptor.getValue().getOperatorId());
        
        Map<MetricName, MetricValue> actualValues 
                = providerCaptor.getValue().nextReading().getValues();
        
        assertTrue("CPU_UTILIZATION missing from metric reading", 
                actualValues.containsKey(MetricName.CPU_UTILIZATION));
        
        assertEquals("CPU_UTILIZATION value different in metric reading", 
                fakeTuple.getMetricValue(MetricName.CPU_UTILIZATION), 
                actualValues.get(MetricName.CPU_UTILIZATION));
        
        assertTrue("HEAP_UTILIZATION missing from metric reading", 
                actualValues.containsKey(MetricName.HEAP_UTILIZATION));
        
        assertEquals("HEAP_UTILIZATION value different in metric reading", 
                fakeTuple.getMetricValue(MetricName.HEAP_UTILIZATION), 
                actualValues.get(MetricName.HEAP_UTILIZATION));
        
        assertTrue("OPERATOR_LATENCY missing from metric reading", 
                actualValues.containsKey(MetricName.OPERATOR_LATENCY));
        
        assertEquals("OPERATOR_LATENCY value different in metric reading", 
                fakeTuple.getMetricValue(MetricName.OPERATOR_LATENCY), 
                actualValues.get(MetricName.OPERATOR_LATENCY));
    }
}
