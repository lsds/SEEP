/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric;

/**
 *
 * @author mrouaux
 */
public class MetricValue {
    
    public static MetricValue percent(double value) {
        return new MetricValue(value, MetricUnit.PERCENT);
    }
            
    public static MetricValue gb(int value) {
        return new MetricValue(value, MetricUnit.GIGABYTES);
    }
    
    public static MetricValue mb(int value) {
        return new MetricValue(value, MetricUnit.MEGABYTES);
    }
    
    public static MetricValue millis(int value) {
        return new MetricValue(value, MetricUnit.MILLISECONDS);
    }
    
    public static MetricValue tuples(int value) {
        return new MetricValue(value, MetricUnit.TUPLES);
    }
    
    private double value;
    private MetricUnit unit;

    MetricValue(double value, MetricUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public MetricUnit getUnit() {
        return unit;
    }

    public void setUnit(MetricUnit unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "MetricValue{" + "value=" + value + ", unit=" + unit + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        hash = 11 * hash + (this.unit != null ? this.unit.hashCode() : 0);
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
        final MetricValue other = (MetricValue) obj;
        if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(other.value)) {
            return false;
        }
        if (this.unit != other.unit) {
            return false;
        }
        return true;
    }
}
