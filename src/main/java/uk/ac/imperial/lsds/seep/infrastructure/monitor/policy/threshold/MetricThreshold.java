package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold;

import java.util.Objects;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricValue;

/**
 *
 * @author mrouaux
 */
public abstract class MetricThreshold extends Threshold<MetricValue> {
    
    public static MetricThresholdAbove above(MetricValue threshold) {
        return new MetricThresholdAbove(threshold);
    }
    
    public static MetricThresholdBelow below(MetricValue threshold) {
        return new MetricThresholdBelow(threshold);
    }
    
    private MetricValue threshold;

    public MetricThreshold(MetricValue threshold) {
        this.threshold = threshold;
    }

    public MetricValue getThreshold() {
        return threshold;
    }

    public void setThreshold(MetricValue threshold) {
        this.threshold = threshold;
    }

    @Override
    public String toString() {
        String className = this.getClass().getSimpleName();
        return className + "{" + "threshold=" + threshold + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.threshold);
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
        final MetricThreshold other = (MetricThreshold) obj;
        if (!Objects.equals(this.threshold, other.threshold)) {
            return false;
        }
        return true;
    }
}
