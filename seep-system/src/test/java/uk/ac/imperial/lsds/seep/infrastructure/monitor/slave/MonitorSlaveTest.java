/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.slave;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Ignore;

/**
 *
 * @author mrouaux
 */
public class MonitorSlaveTest {
    
    public MonitorSlaveTest() {
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
    public void testSlaveConnectToMaster() throws IOException, InterruptedException {
        System.out.println("testSlaveConnectToMaster");
        
        final int operatorId = 1;
        final int masterPort = 64000;
        
        final MutableBoolean connected = new MutableBoolean(false);
        
        MonitorSlave slave = spy(new MonitorSlave(operatorId, "localhost", masterPort, 1));
        MonitorSlaveProcessor mockProcessor = mock(MonitorSlaveProcessor.class);
        
        doReturn(mockProcessor).when(slave)
                .createSlaveProcessor(any(OutputStream.class));
        
        doNothing().when(mockProcessor).process();
        
        // Start a mock server (in a different thread)
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket mockServer = new ServerSocket(masterPort);
                    Socket slaveSocket = mockServer.accept();
                    
                    System.out.println("Accepted connection from " 
                            + slaveSocket.getRemoteSocketAddress().toString());
                            
                    connected.setValue(true);
                } catch (IOException ex) {
                    fail("Unexpected exception while waiting for slave to connect");
                }
            }
        }).start();
        
        new Thread(slave).start();
        Thread.sleep(1500);
        slave.stop();
        
        verify(mockProcessor, atLeast(1)).process();
        assertThat("Slave should have connecetd to master",connected.getValue(), equalTo(true));
    }
}
