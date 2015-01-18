/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.syntax;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor.ScaleFactor;

/**
 *
 * @author martinrouaux
 */
public interface ScaleFactorSyntax {
    
    ScaleFactorSyntax by(final ScaleFactor scaleFactor);

}
