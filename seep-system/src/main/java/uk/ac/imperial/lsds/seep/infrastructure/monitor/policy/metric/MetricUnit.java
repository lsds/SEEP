/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric;

/**
 *
 * @author mrouaux
 */
public enum MetricUnit {
    
    PERCENT("percent"),
    BYTES("bytes"),
    MEGABYTES("megabytes"),
    GIGABYTES("gigabytes"),
    MILLISECONDS("milliseconds"),
    TUPLES("tuples");
    
    private String name;
    
    MetricUnit(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
