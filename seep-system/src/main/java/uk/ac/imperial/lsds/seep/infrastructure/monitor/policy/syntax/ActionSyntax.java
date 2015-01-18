/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.syntax;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.Operator;

/**
 *
 * @author martinrouaux
 */
public interface ActionSyntax {
    
    ActionSyntax scaleIn(final Operator operator);
    
    ActionSyntax scaleOut(final Operator operator);

}
