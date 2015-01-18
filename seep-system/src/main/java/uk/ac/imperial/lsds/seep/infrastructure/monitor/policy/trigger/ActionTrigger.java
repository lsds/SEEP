/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.trigger;

import java.util.List;
import java.util.Objects;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.MetricThreshold;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.TimeThreshold;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReading;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.TimeReference;

/**
 * Trigger that evaluates a metric value threshold and a time threshold. If both
 * thresholds evaluate to true, then the trigger state changes to fired. Otherwise,
 * the trigger reverts to state non-fired.
 * 
 * @author mrouaux
 */
public class ActionTrigger {
    
    private static final Logger logger = LoggerFactory.getLogger(ActionTrigger.class);
  
    public enum ActionTriggerState {
        FIRED,
        NON_FIRED
    }
    
    private MetricThreshold valueThreshold;
    private TimeThreshold timeThreshold;
    private MetricName metricName;
    private ActionTriggerState triggerState;
    private boolean stateChanged;

    /**
     * Convenience constructor
     * @param valueThreshold Value threshold that will change the state of the trigger
     * @param timeThreshold Time threshold that will change the state of the trigger
     * @param metricName Name of the metric associated to this trigger.
     */
    public ActionTrigger(
                final MetricThreshold valueThreshold, 
                final TimeThreshold timeThreshold, 
                final MetricName metricName) {
        
        this.valueThreshold = valueThreshold;
        this.timeThreshold = timeThreshold;
        this.metricName = metricName;
        
        this.triggerState = ActionTriggerState.NON_FIRED;
        this.stateChanged = false;
    }

    /**
     * Evaluates the state of the trigger. Both the value and the time threshold
     * need to evaluate to true in order for the trigger state to change from
     * non-fired to fired.
     * @param readings metric readings to evaluate (all those that are within the
     * time threshold need to evaluate to true in terms of their value).
     */
    public void evaluate(List<MetricReading> readings,
                         TimeReference time) {
        
        logger.info("Evaluating trigger for " 
                        + metricName.toString() + " - "
                        + readings.size() + " readings provided");

        logger.debug("value threshold: " + valueThreshold.toString());
        logger.debug("time threshold: " + timeThreshold.toString());

        // Determine the new state of the trigger depending on the result of
        // evaluating boh thresholds. Need to mark flag if state changes.
        ActionTriggerState pastTriggerState = triggerState;
        
        boolean enoughReadings = true;
        
        // Check that we have enough readings to cover the entire time threshold
        if ((readings != null) && (readings.size() > 0)) {
            MetricReading mostRecentReading = readings.get(readings.size() - 1);
            MetricReading leastRecentReading = readings.get(0);
        
            logger.info("Most recent reading [" + mostRecentReading.getTimestamp() + "]");
            logger.info("Least recent reading [" + leastRecentReading.getTimestamp() + "]");
            
            Period readingsPeriod = new Period(
                        leastRecentReading.getTimestamp(), 
                        mostRecentReading.getTimestamp());
            
            int toleranceSeconds = new Double(0.1 
                * timeThreshold.toPeriod().toStandardSeconds().getSeconds()).intValue();
            
            if (readingsPeriod.toStandardSeconds().isLessThan(
                    timeThreshold.toPeriod().toStandardSeconds().minus(toleranceSeconds))) {
                
                logger.info("Not enough readings, only for last period[" + readingsPeriod + "]");
                enoughReadings = false;
            }
        }
        
        // If we have enough readings, then we evaluate in detail
        if (enoughReadings) {
            int i = 0;
            for(MetricReading r : readings) {
                Period metricPeriod = new Period(r.getTimestamp(), time.now());
                MetricValue metricValue = r.getValues().get(metricName);

                logger.info("Evaluating reading[" + i + "] value["
                            + metricValue.toString() + "] period["
                            + metricPeriod.toString() + "]");

                // We evaluate the time threshold first, simple optimisation to be
                // able to abort the iteration sooner (readings are guaranteed to be
                // sorted by time of reception, from most recent to least recent).
                if(timeThreshold.evaluate(metricPeriod)) {
                    triggerState = valueThreshold.evaluate(metricValue)? 
                                            ActionTriggerState.FIRED:
                                            ActionTriggerState.NON_FIRED;

                    // If there is a reading within the time threshold for which
                    // the value evaluates to false (trigger is non-fired), then
                    // we can break from the evaluation loop.
                    if(triggerState.equals(ActionTriggerState.NON_FIRED)) {
                        break;
                    }
                }

                i++;
            }
        }

        stateChanged = (triggerState != pastTriggerState);
        logger.info("New trigger state is [" + triggerState.toString() 
                            + "] changed[" + stateChanged + "]");
    }
    
    /**
     * @return True if the state of the trigger is fired. False otherwise.
     */
    public boolean isFired() {
        return (triggerState.equals(ActionTriggerState.FIRED));
    }
    
    /**
     * @return True if the state of the trigger changed during the last evaluation.
     */
    public boolean hasChanged() {
        return stateChanged;
    }

    /**
     * @return Value threshold for the trigger
     */
    public MetricThreshold getValueThreshold() {
        return valueThreshold;
    }

    /**
     * @return Time threshold for the trigger
     */
    public TimeThreshold getTimeThreshold() {
        return timeThreshold;
    }

    /**
     * @return Metric name for the trigger
     */
    public MetricName getMetricName() {
        return metricName;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.valueThreshold);
        hash = 47 * hash + Objects.hashCode(this.timeThreshold);
        hash = 47 * hash + (this.metricName != null ? this.metricName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ActionTrigger other = (ActionTrigger) obj;
        if (!Objects.equals(this.valueThreshold, other.valueThreshold)) {
            return false;
        }
        if (!Objects.equals(this.timeThreshold, other.timeThreshold)) {
            return false;
        }
        if (this.metricName != other.metricName) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ActionTrigger{" 
                    + "valueThreshold=" + valueThreshold 
                    + ", timeThreshold=" + timeThreshold 
                    + ", metricName=" + metricName + '}';
    }
}
