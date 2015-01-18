/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.syntax;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.TimeThreshold;

/**
 *
 * @author martinrouaux
 */
public interface TimeThresholdSyntax {
    
    TimeThresholdSyntax forAtLeast(TimeThreshold timeThreshold);

}
