package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.trigger;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;
import static uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue.*;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.MetricThreshold;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.TimeThreshold;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReading;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.TimeReference;

/**
 *
 * @author mrouaux
 */
public class ActionTriggerTest {

    private final MetricThreshold mockValueThreshold = mock(MetricThreshold.class);
    private final TimeThreshold mockTimeThreshold = mock(TimeThreshold.class);
    private final TimeReference mockTimeReference = mock(TimeReference.class);
    
    public ActionTriggerTest() {
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
    
    private Instant createInstant(int year, int month, int day, 
                                int hour, int minute, int second) {
        return new DateTime()
                        .withYear(year)
                        .withMonthOfYear(month)
                        .withDayOfMonth(day)
                        .withHourOfDay(hour)
                        .withMinuteOfHour(minute)
                        .withSecondOfMinute(second)
                        .withMillisOfSecond(0).toInstant();
    }
    
    private MetricReading createFakeReading(Instant timestamp, MetricName name, MetricValue value) {
        MetricReading r = new MetricReading();
        r.setTimestamp(timestamp);
        r.getValues().put(name, value);
        return r;
    }
    
    @Test
    public void testTriggerWithSingleReadingTimeTrueAndValueTrue() {
        System.out.println("testTriggerWithSingleReadingTimeTrueAndValueTrue");
        
        List<MetricReading> readings = new ArrayList<MetricReading>();
        MetricReading r1 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 30, 0),
                                MetricName.CPU_UTILIZATION, percent(50));
        readings.add(r1);
        
        Instant currentTime = createInstant(2013, 12, 29, 10, 33, 0);

        when(mockTimeReference.now()).thenReturn(currentTime);
        
        when(mockTimeThreshold.evaluate(eq(new Period(r1.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
                
        when(mockValueThreshold.evaluate(eq(r1.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        
        ActionTrigger trigger = new ActionTrigger(
                                        mockValueThreshold,
                                        mockTimeThreshold,
                                        MetricName.CPU_UTILIZATION);
        trigger.evaluate(readings, mockTimeReference);
        
        assertTrue("Trigger should have fired", trigger.isFired());
        assertTrue("Trigger state should have changed ", trigger.hasChanged());
    }

    @Test
    public void testTriggerWithSingleReadingTimeTrueAndValueFalse() {
        System.out.println("testTriggerWithSingleReadingTimeTrueAndValueFalse");
        
        List<MetricReading> readings = new ArrayList<MetricReading>();
        MetricReading r1 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 30, 0),
                                MetricName.CPU_UTILIZATION, percent(50));
        readings.add(r1);
        
        Instant currentTime = createInstant(2013, 12, 29, 10, 33, 0);

        when(mockTimeReference.now()).thenReturn(currentTime);
        
        when(mockTimeThreshold.evaluate(eq(new Period(r1.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
                
        when(mockValueThreshold.evaluate(eq(r1.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.FALSE);
        
        ActionTrigger trigger = new ActionTrigger(
                                        mockValueThreshold,
                                        mockTimeThreshold,
                                        MetricName.CPU_UTILIZATION);
        trigger.evaluate(readings, mockTimeReference);
        
        assertFalse("Trigger shouldn't have fired", trigger.isFired());
        assertFalse("Trigger state shouldn't have changed ", trigger.hasChanged());
    }
        
    @Test
    public void testTriggerWithSingleReadingTimeFalse() {
        System.out.println("testTriggerWithSingleReadingTimeFalse");
        
        List<MetricReading> readings = new ArrayList<MetricReading>();
        MetricReading r1 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 30, 0),
                                MetricName.CPU_UTILIZATION, percent(50));
        readings.add(r1);
        
        Instant currentTime = createInstant(2013, 12, 29, 10, 33, 0);

        when(mockTimeReference.now()).thenReturn(currentTime);
        
        when(mockTimeThreshold.evaluate(eq(new Period(r1.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.FALSE);
                
        verify(mockValueThreshold, never())
                    .evaluate(eq(r1.getValues().get(MetricName.CPU_UTILIZATION)));

        ActionTrigger trigger = new ActionTrigger(
                                        mockValueThreshold,
                                        mockTimeThreshold,
                                        MetricName.CPU_UTILIZATION);
        trigger.evaluate(readings, mockTimeReference);

        assertFalse("Trigger shouldn't have fired", trigger.isFired());
        assertFalse("Trigger state shouldn't have changed ", trigger.hasChanged());
    }

    @Test
    public void testTriggerWithMultipleReadingsTimeTrueAndValueTrue() {
        System.out.println("testTriggerWithMultipleReadingsTimeTrueAndValueTrue");
        
        List<MetricReading> readings = new ArrayList<MetricReading>();
 
        MetricReading r1 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 30, 0),
                                MetricName.CPU_UTILIZATION, percent(50));
        
        MetricReading r2 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 31, 0),
                                MetricName.CPU_UTILIZATION, percent(60));
        
        MetricReading r3 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 31, 30),
                                MetricName.CPU_UTILIZATION, percent(70));
        
        readings.add(r1);
        readings.add(r2);
        readings.add(r3);
        
        Instant currentTime = createInstant(2013, 12, 29, 10, 33, 0);

        when(mockTimeReference.now()).thenReturn(currentTime);
        
        // Mock expectations for time threshold
        when(mockTimeThreshold.evaluate(eq(new Period(r1.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        when(mockTimeThreshold.evaluate(eq(new Period(r2.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        when(mockTimeThreshold.evaluate(eq(new Period(r3.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        
        // Mock expectations for value threshold
        when(mockValueThreshold.evaluate(eq(r1.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        when(mockValueThreshold.evaluate(eq(r2.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        when(mockValueThreshold.evaluate(eq(r3.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        
        ActionTrigger trigger = new ActionTrigger(
                                        mockValueThreshold,
                                        mockTimeThreshold,
                                        MetricName.CPU_UTILIZATION);
        trigger.evaluate(readings, mockTimeReference);
        
        assertTrue("Trigger should have fired", trigger.isFired());
        assertTrue("Trigger state should have changed ", trigger.hasChanged());
    }
    
    @Test
    public void testTriggerWithMultipleReadingsStateChangesOnceOnly() {
        System.out.println("testTriggerWithMultipleReadingsStateChangesOnceOnly");
        
        List<MetricReading> readings = new ArrayList<MetricReading>();
 
        MetricReading r1 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 30, 0),
                                MetricName.CPU_UTILIZATION, percent(50));
        
        MetricReading r2 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 31, 0),
                                MetricName.CPU_UTILIZATION, percent(60));
        
        MetricReading r3 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 31, 30),
                                MetricName.CPU_UTILIZATION, percent(70));
        
        readings.add(r1);
        readings.add(r2);
        readings.add(r3);
        
        Instant currentTime = createInstant(2013, 12, 29, 10, 33, 0);

        when(mockTimeReference.now()).thenReturn(currentTime);
        
        // Mock expectations for time threshold
        when(mockTimeThreshold.evaluate(eq(new Period(r1.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        when(mockTimeThreshold.evaluate(eq(new Period(r2.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        when(mockTimeThreshold.evaluate(eq(new Period(r3.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        
        // Mock expectations for value threshold
        when(mockValueThreshold.evaluate(eq(r1.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        when(mockValueThreshold.evaluate(eq(r2.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        when(mockValueThreshold.evaluate(eq(r3.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        
        ActionTrigger trigger = new ActionTrigger(
                                        mockValueThreshold,
                                        mockTimeThreshold,
                                        MetricName.CPU_UTILIZATION);
        
        trigger.evaluate(readings, mockTimeReference);
        assertTrue("Trigger should have fired", trigger.isFired());
        assertTrue("Trigger state should have changed ", trigger.hasChanged());
        
        trigger.evaluate(readings, mockTimeReference);
        assertTrue("Trigger should remain fired", trigger.isFired());
        assertFalse("Trigger state shouldn't have changed", trigger.hasChanged());    
    }
    
    @Test
    public void testTriggerWithMultipleReadingsWithOneTimeFalse() {
        System.out.println("testTriggerWithMultipleReadingsWithOneTimeFalse");
        
        List<MetricReading> readings = new ArrayList<MetricReading>();
 
        MetricReading r1 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 25, 0),
                                MetricName.CPU_UTILIZATION, percent(50));
        
        MetricReading r2 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 31, 0),
                                MetricName.CPU_UTILIZATION, percent(60));
        
        MetricReading r3 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 31, 30),
                                MetricName.CPU_UTILIZATION, percent(70));
        
        readings.add(r1);
        readings.add(r2);
        readings.add(r3);
        
        Instant currentTime = createInstant(2013, 12, 29, 10, 33, 0);

        when(mockTimeReference.now()).thenReturn(currentTime);
        
        // Mock expectations for time threshold
        when(mockTimeThreshold.evaluate(eq(new Period(r1.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.FALSE);
        
        when(mockTimeThreshold.evaluate(eq(new Period(r2.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        when(mockTimeThreshold.evaluate(eq(new Period(r3.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        
        // Mock expectations for value threshold, the first reading is outside
        // the time threshold for the trigger. Therefore, the value threshold 
        // should never be evaluated against it.
        verify(mockValueThreshold, never()).evaluate(eq(r1.getValues().get(MetricName.CPU_UTILIZATION)));
                    
        when(mockValueThreshold.evaluate(eq(r2.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        when(mockValueThreshold.evaluate(eq(r3.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        
        ActionTrigger trigger = new ActionTrigger(
                                        mockValueThreshold,
                                        mockTimeThreshold,
                                        MetricName.CPU_UTILIZATION);
        
        trigger.evaluate(readings, mockTimeReference);
        assertTrue("Trigger should have fired", trigger.isFired());
        assertTrue("Trigger state should have changed ", trigger.hasChanged());
    }
    
    @Test
    public void testTriggerWithMultipleReadingsWithOneValueFalse() {
        System.out.println("testTriggerWithMultipleReadingsWithOneValueFalse");
        
        List<MetricReading> readings = new ArrayList<MetricReading>();
 
        MetricReading r1 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 30, 0),
                                MetricName.CPU_UTILIZATION, percent(50));
        
        MetricReading r2 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 31, 0),
                                MetricName.CPU_UTILIZATION, percent(60));
        
        MetricReading r3 = createFakeReading(
                                createInstant(2013, 12, 29, 10, 31, 30),
                                MetricName.CPU_UTILIZATION, percent(70));
        
        readings.add(r1);
        readings.add(r2);
        readings.add(r3);
        
        Instant currentTime = createInstant(2013, 12, 29, 10, 33, 0);

        when(mockTimeReference.now()).thenReturn(currentTime);
        
        // Mock expectations for time threshold
        when(mockTimeThreshold.evaluate(eq(new Period(r1.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        when(mockTimeThreshold.evaluate(eq(new Period(r2.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        when(mockTimeThreshold.evaluate(eq(new Period(r3.getTimestamp(), currentTime))))
                    .thenReturn(Boolean.TRUE);
        
        // Mock expectations for value threshold
        when(mockValueThreshold.evaluate(eq(r1.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        when(mockValueThreshold.evaluate(eq(r2.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.TRUE);
        when(mockValueThreshold.evaluate(eq(r3.getValues().get(MetricName.CPU_UTILIZATION))))
                    .thenReturn(Boolean.FALSE);
        
        ActionTrigger trigger = new ActionTrigger(
                                        mockValueThreshold,
                                        mockTimeThreshold,
                                        MetricName.CPU_UTILIZATION);
        
        trigger.evaluate(readings, mockTimeReference);
        assertFalse("Trigger shouldn't have fired", trigger.isFired());
        assertFalse("Trigger state shouldn't have changed ", trigger.hasChanged());
    }
}