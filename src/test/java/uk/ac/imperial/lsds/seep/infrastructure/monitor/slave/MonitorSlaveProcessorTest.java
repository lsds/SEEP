package uk.ac.imperial.lsds.seep.infrastructure.monitor.slave;

import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsSerializer;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.comm.serialization.MetricsTuple;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.slave.reader.MetricsReader;

/**
 *
 * @author mrouaux
 */
public class MonitorSlaveProcessorTest {
    
    public MonitorSlaveProcessorTest() {
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
    public void testProcessorSingleReaderSingleReporterOnce() {
        System.out.println("testProcessorSingleReaderSingleReporterOnce");
        
        int operatorId = 1000;
        
        MetricsReader mockReader = mock(MetricsReader.class);
        MetricsSerializer mockReporter = mock(MetricsSerializer.class);
        
        ArgumentCaptor<MetricsTuple> tupleCaptor = 
                                ArgumentCaptor.forClass(MetricsTuple.class);
        
        MonitorSlaveProcessor processor = new MonitorSlaveProcessor(operatorId);
        
        processor.addReader(mockReader);
        processor.addReporter(mockReporter);
        
        when(mockReader.readableNames())
                .thenReturn(Arrays.asList(new MetricName[] {
                    MetricName.CPU_UTILIZATION,
                    MetricName.HEAP_SIZE
                }));

        when(mockReader.readValue(eq(MetricName.CPU_UTILIZATION)))
                .thenReturn(percent(50));
                
        when(mockReader.readValue(eq(MetricName.HEAP_SIZE)))
                .thenReturn(gb(1024));
        
        doNothing().when(mockReporter).serialize(tupleCaptor.capture());
        
        processor.process();
        
        assertNotNull(tupleCaptor.getValue());
        
        assertEquals("Operator identifier in tuple is incorrect", 
                        operatorId, tupleCaptor.getValue().getOperatorId());
        
        assertEquals("Value is not as expected for " + MetricName.CPU_UTILIZATION, 
                Double.valueOf(50), Double.valueOf(
                    tupleCaptor.getValue().getMetricValue(MetricName.CPU_UTILIZATION).getValue()));
    
        assertEquals("Value is not as expected for " + MetricName.HEAP_SIZE, 
                Double.valueOf(1024), Double.valueOf(
                    tupleCaptor.getValue().getMetricValue(MetricName.HEAP_SIZE).getValue()));
    
    }
    
    @Test
    public void testProcessorSingleReaderSingleReporterMultiple() {
        System.out.println("testProcessorSingleReaderSingleReporterMultiple");
        int operatorId = 1000;
        
        MetricsReader mockReader = mock(MetricsReader.class);
        MetricsSerializer mockReporter = mock(MetricsSerializer.class);
        
        ArgumentCaptor<MetricsTuple> tupleCaptor = 
                                ArgumentCaptor.forClass(MetricsTuple.class);
        
        MonitorSlaveProcessor processor = new MonitorSlaveProcessor(operatorId);
        
        processor.addReader(mockReader);
        processor.addReporter(mockReporter);
        
        when(mockReader.readableNames())
                .thenReturn(Arrays.asList(new MetricName[] {
                    MetricName.CPU_UTILIZATION,
                    MetricName.HEAP_SIZE
                }));

        when(mockReader.readValue(eq(MetricName.CPU_UTILIZATION)))
                .thenReturn(percent(50))
                .thenReturn(percent(80));
                
        when(mockReader.readValue(eq(MetricName.HEAP_SIZE)))
                .thenReturn(gb(1024))
                .thenReturn(gb(800));
        
        doNothing().when(mockReporter).serialize(tupleCaptor.capture());
        
        processor.process();
        processor.process();
        
        assertNotNull(tupleCaptor.getAllValues());
        assertEquals("Not enough arguments captured", 2, tupleCaptor.getAllValues().size());
                
        assertEquals("Operator identifier in tuple(0) is incorrect", 
                        operatorId, tupleCaptor.getAllValues().get(0).getOperatorId());
        
        assertEquals("Operator identifier in tuple(1) is incorrect", 
                        operatorId, tupleCaptor.getAllValues().get(1).getOperatorId());
        
        assertEquals("Tuple(0): value is not as expected for " + MetricName.CPU_UTILIZATION, 
                Double.valueOf(50), Double.valueOf(
                    tupleCaptor.getAllValues().get(0).getMetricValue(MetricName.CPU_UTILIZATION).getValue()));
    
        assertEquals("Tuple(0): value is not as expected for " + MetricName.HEAP_SIZE, 
                Double.valueOf(1024), Double.valueOf(
                    tupleCaptor.getAllValues().get(0).getMetricValue(MetricName.HEAP_SIZE).getValue()));
        
        assertEquals("Tuple(1): value is not as expected for " + MetricName.CPU_UTILIZATION, 
                Double.valueOf(80), Double.valueOf(
                    tupleCaptor.getAllValues().get(1).getMetricValue(MetricName.CPU_UTILIZATION).getValue()));
    
        assertEquals("Tuple(1): value is not as expected for " + MetricName.HEAP_SIZE, 
                Double.valueOf(800), Double.valueOf(
                    tupleCaptor.getAllValues().get(1).getMetricValue(MetricName.HEAP_SIZE).getValue()));
    }
    
    @Test
    public void testProcessorMultipleReaderMultipleReporterOnce() {
        System.out.println("testProcessorMultipleReaderMultipleReporterOnce");
        
        int operatorId = 1000;
        
        MetricsReader mockReader1 = mock(MetricsReader.class);
        MetricsReader mockReader2 = mock(MetricsReader.class);
        
        MetricsSerializer mockReporter1 = mock(MetricsSerializer.class);
        MetricsSerializer mockReporter2 = mock(MetricsSerializer.class);
        
        ArgumentCaptor<MetricsTuple> tupleCaptor = 
                                ArgumentCaptor.forClass(MetricsTuple.class);
        
        MonitorSlaveProcessor processor = new MonitorSlaveProcessor(operatorId);
        
        processor.addReader(mockReader1);
        processor.addReader(mockReader2);
        
        processor.addReporter(mockReporter1);
        processor.addReporter(mockReporter2);
        
        when(mockReader1.readableNames())
                .thenReturn(Arrays.asList(new MetricName[] {
                    MetricName.CPU_UTILIZATION
                }));

        when(mockReader2.readableNames())
                .thenReturn(Arrays.asList(new MetricName[] {
                    MetricName.HEAP_SIZE
                }));
        
        when(mockReader1.readValue(eq(MetricName.CPU_UTILIZATION)))
                .thenReturn(percent(50));
                
        when(mockReader2.readValue(eq(MetricName.HEAP_SIZE)))
                .thenReturn(gb(1024));
        
        doNothing().when(mockReporter1).serialize(tupleCaptor.capture());
        doNothing().when(mockReporter2).serialize(tupleCaptor.capture());
        
        processor.process();
        
        assertNotNull(tupleCaptor.getAllValues());
        assertEquals("Not enough arguments captured", 2, tupleCaptor.getAllValues().size());
        
        assertEquals("Operator identifier in tuple(0) is incorrect", 
                        operatorId, tupleCaptor.getAllValues().get(0).getOperatorId());
        
        assertEquals("Operator identifier in tuple(1) is incorrect", 
                        operatorId, tupleCaptor.getAllValues().get(1).getOperatorId());
        
        assertEquals("Tuple(0): value is not as expected for " + MetricName.CPU_UTILIZATION, 
                Double.valueOf(50), Double.valueOf(
                    tupleCaptor.getAllValues().get(0).getMetricValue(MetricName.CPU_UTILIZATION).getValue()));
    
        assertEquals("Tuple(0): value is not as expected for " + MetricName.HEAP_SIZE, 
                Double.valueOf(1024), Double.valueOf(
                    tupleCaptor.getAllValues().get(0).getMetricValue(MetricName.HEAP_SIZE).getValue()));
        
        assertEquals("Tuple(1): value is not as expected for " + MetricName.CPU_UTILIZATION, 
                Double.valueOf(50), Double.valueOf(
                    tupleCaptor.getAllValues().get(1).getMetricValue(MetricName.CPU_UTILIZATION).getValue()));
    
        assertEquals("Tuple(1): value is not as expected for " + MetricName.HEAP_SIZE, 
                Double.valueOf(1024), Double.valueOf(
                    tupleCaptor.getAllValues().get(1).getMetricValue(MetricName.HEAP_SIZE).getValue()));
    }
}