/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.master;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsDeserializer;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRules;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.evaluate.AbstractEvaluator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.InfrastructureAdaptor;

/**
 *
 * @author mrouaux
 */
public class MonitorMasterTest {
    
    public MonitorMasterTest() {
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
    @Ignore
    public void testStartSlaveConnect() throws InterruptedException {
        System.out.println("testStartSlaveConnect");
        
        InfrastructureAdaptor mockAdapor = mock(InfrastructureAdaptor.class);
        PolicyRules mockRules = mock(PolicyRules.class);
        AbstractEvaluator mockEvaluator = mock(AbstractEvaluator.class);
        MonitorMasterWorker mockWorker = mock(MonitorMasterWorker.class);
        
        int masterPort = 63000;
        
        MonitorMaster monitorMaster = spy(new MonitorMaster(mockAdapor, mockRules, masterPort));
        
        // Set expectations for the methods we intend to mock
        when(monitorMaster.createEvaluator(eq(mockAdapor), eq(mockRules)))
                .thenReturn(mockEvaluator);
        
        when(monitorMaster.createMasterWorker(
                                any(Socket.class), 
                                any(MetricsDeserializer.class), 
                                eq(mockEvaluator)))
                .thenReturn(mockWorker);
        
        doNothing().when(monitorMaster)
                .startMasterWorker(eq(mockWorker));
        
        Thread t = new Thread(monitorMaster);
        t.start();
        
        // We need to allow the master to catch-up and be ready listening for 
        // incoming connections from the monitoring slaves.
        Thread.sleep(100);
        
        Socket s = new Socket();

        try {
            s.connect(new InetSocketAddress("localhost", masterPort));
        } catch (IOException ex) {
            fail("Exception {" + ex.getMessage() + "} thrown but not expected");
        }
    
        assertThat("Socket to MonitorMaster is expected to be connected", 
                                s.isConnected(), Matchers.is(Boolean.TRUE));
        
        verify(monitorMaster).createEvaluator(
                                eq(mockAdapor), 
                                eq(mockRules));
        
        verify(monitorMaster).createMasterWorker(
                                any(Socket.class), 
                                any(MetricsDeserializer.class), 
                                eq(mockEvaluator));
        
        verify(monitorMaster).startMasterWorker(eq(mockWorker));
        
        monitorMaster.stop();
    }
    
    @Test
    public void testStartSlaveConnectAndStop() throws InterruptedException {
        System.out.println("testStartSlaveConnectAndStop");
        
        InfrastructureAdaptor mockAdapor = mock(InfrastructureAdaptor.class);
        PolicyRules mockRules = mock(PolicyRules.class);
        AbstractEvaluator mockEvaluator = mock(AbstractEvaluator.class);
        MonitorMasterWorker mockWorker = mock(MonitorMasterWorker.class);
        
        int masterPort = 63001;
        
        MonitorMaster monitorMaster = spy(new MonitorMaster(mockAdapor, mockRules, masterPort));
        
        // Set expectations for the methods we intend to mock
        when(monitorMaster.createEvaluator(eq(mockAdapor), eq(mockRules)))
                .thenReturn(mockEvaluator);
        
        when(monitorMaster.createMasterWorker(
                                any(Socket.class), 
                                any(MetricsDeserializer.class), 
                                eq(mockEvaluator)))
                .thenReturn(mockWorker);
        
        Thread t = new Thread(monitorMaster);
        t.start();
        
        // We need to allow the master to catch-up and be ready listening for 
        // incoming connections from the monitoring slaves.
        Thread.sleep(100);
        
        Socket clientSocket1 = new Socket();

        try {
            clientSocket1.connect(new InetSocketAddress("localhost", masterPort));
        } catch (IOException ex) {
            fail("Exception {" + ex.getMessage() + "} thrown but not expected");
        }
    
        assertThat("Socket to MonitorMaster is expected to be connected", 
                                clientSocket1.isConnected(), Matchers.is(Boolean.TRUE));
        
        monitorMaster.stop();
        
        // Similar reason here, we want the master to fully stop
        Thread.sleep(100);
        
        Socket clientSocket2 = new Socket();
        boolean exceptionThrown = false;
        
        try {
            clientSocket2.connect(new InetSocketAddress("localhost", masterPort));
        } catch (IOException ex) {
            exceptionThrown = true;
        }
        
        if (!exceptionThrown) {
            fail("Exception was expected but not thrown when connecting to MonitorMaster");
        }
    }
}
