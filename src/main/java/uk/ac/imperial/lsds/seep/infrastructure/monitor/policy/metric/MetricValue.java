/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author mrouaux
 */
public class MetricValue implements Serializable {
    
    public static MetricValue percent(double value) {
        return new MetricValue(value, MetricUnit.PERCENT);
    }
            
    public static MetricValue gb(int value) {
        return new MetricValue(value, MetricUnit.GIGABYTES);
    }
    
    public static MetricValue mb(int value) {
        return new MetricValue(value, MetricUnit.MEGABYTES);
    }
    
    public static MetricValue bytes(int value) {
        return new MetricValue(value, MetricUnit.BYTES);
    }
    
    public static MetricValue millis(int value) {
        return new MetricValue(value, MetricUnit.MILLISECONDS);
    }
    
    public static MetricValue tuples(int value) {
        return new MetricValue(value, MetricUnit.TUPLES);
    }
    
    private double value;
    private MetricUnit unit;

    /**
     * Default constructor. A no-arguments constructor is needed in order to 
     * support serialisation properly.
     */
    public MetricValue() {
    }
    
    public MetricValue(double value, MetricUnit unit) {
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
    
    /**
     * Converts the current object into a new instance of the target unit. Only
     * storage units are supported at the moment (byte, megabyte and gigabyte).
     * @param dstUnit Target unit
     * @return New MetricValue instance with the converted value and desired units.
     */
    public MetricValue convertTo(MetricUnit dstUnit) {
        MetricUnit[] supportedUnits = new MetricUnit[] {
            MetricUnit.BYTES, MetricUnit.MEGABYTES, MetricUnit.GIGABYTES};
        
        if (!Arrays.asList(supportedUnits).contains(dstUnit)) {
            return this;
        }
        
        MetricUnit srcUnit = this.unit;
        int[] distanceUnits = new int[] {0, 20, 30};
        
        // Calculate index of source and destination unit
        int srcUnitIndex = Arrays.asList(supportedUnits).indexOf(srcUnit);
        int dstUnitIndex = Arrays.asList(supportedUnits).indexOf(dstUnit);
        
        int distance = distanceUnits[srcUnitIndex] - distanceUnits[dstUnitIndex];
        
        // Construct the new object and return
        MetricValue convertedValue = new MetricValue();
        convertedValue.setValue(this.value * Math.pow(2, distance));
        convertedValue.setUnit(dstUnit);
        return convertedValue; 
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
