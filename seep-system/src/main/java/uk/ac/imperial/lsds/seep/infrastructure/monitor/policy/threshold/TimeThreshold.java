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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold;

import java.util.Objects;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Time threshold expressed as a period of time. A metric value needs to be above
 * or below its threshold for at least the period of time expressed by the 
 * associated instance of TimeThreshold.
 * 
 * @author mrouaux
 */
public class TimeThreshold extends Threshold<Period> {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeThreshold.class);
    private Period threshold;
    
    public static TimeThreshold seconds(int seconds) {
        return new TimeThreshold(Period.seconds(seconds));
    }
    
    public static TimeThreshold minutes(int minutes) {
        return new TimeThreshold(Period.minutes(minutes));
    }
    
    /**
     * Convenience constructor
     * @param threshold threshold of time
     */
    public TimeThreshold(final Period threshold) {
        this.threshold = threshold;
    }

    /**
     * Evaluates the threshold with respect to a certain period of time.
     * @param period period of time to evaluate (usually a period ending at the
     * current time and spanning for X seconds/minutes into the past).
     * @return True if the time length of period is below the threshold.
     */
    @Override
    public boolean evaluate(Period period) {
        boolean result = false;
        
        if((period != null) && (period.toStandardSeconds() != null)) {
            result = period.toStandardSeconds()
                            .isLessThan(threshold.toStandardSeconds());
        }
        
        logger.debug("Evaluating threshold[" + threshold.toString() + 
                        "] period[" + period.toString() + "] result[" + result + "]");
        return result;
    }

    public Period toPeriod() {
        return new Period(threshold);
    } 
            
    @Override
    public String toString() {
        return "TimeThreshold{" + "threshold=" + threshold + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.threshold);
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
        final TimeThreshold other = (TimeThreshold) obj;
        if (!Objects.equals(this.threshold, other.threshold)) {
            return false;
        }
        return true;
    }
}
