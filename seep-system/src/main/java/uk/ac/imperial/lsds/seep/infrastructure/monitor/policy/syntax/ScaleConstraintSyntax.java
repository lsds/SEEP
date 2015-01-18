/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.syntax;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.ScaleConstraint;

/**
 *
 * @author martinrouaux
 */
public interface ScaleConstraintSyntax {
        
    ScaleConstraintSyntax butNeverBelow(final ScaleConstraint scaleConstraint);

    ScaleConstraintSyntax butNeverAbove(final ScaleConstraint scaleConstraint);

}
