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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.evaluate;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRule;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRules;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.AllOperators;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.OneOperator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.Operator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.InfrastructureAdaptor;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReadingProvider;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.SystemTimeReference;

/**
 * Evaluator for multiple policy rules. The class relies on an infrastructure adaptor
 * to inform the platform the new size of a given operator and query it to determine
 * its current size, when needed. Using an adaptor instigates decoupling from the
 * rest of the platform and facilitates testing.
 * 
 * @author mrouaux
 */
public class PolicyRulesEvaluator 
    extends AbstractEvaluator<PolicyRules, InfrastructureAdaptor, MetricReadingProvider>{

    private static final Logger logger = 
                            LoggerFactory.getLogger(PolicyRulesEvaluator.class);
    
    private MultiValueMap<Integer, PolicyRuleEvaluator> evaluators;
    private List<PolicyRuleEvaluator> allEvaluators;
            
    /**
     * Convenience constructor
     * @param rules scaling policy rules to evaluate
     * @param adaptor infrastructure adaptor (allows evaluators to delegate resizing
     * of a particular operator as a consequence of an action triggered by some
     * of the rules being evaluated). An adaptor is used to decouple policy 
     * evaluation from execution.
     */
    public PolicyRulesEvaluator(final PolicyRules rules,
                                final InfrastructureAdaptor adaptor) {
        super(rules, adaptor);
        initializeEvaluators();
    }
    
    /**
     * 
     * @param provider 
     */
    @Override
    public synchronized void evaluate(MetricReadingProvider provider) {
        routeReadingToEvaluators(provider);
    }
    
    /**
     * Initializes the data structure that allows for the mapping of metric readings
     * to the correct evaluators given the operator identifier in the reading and
     * the operator(s) referenced by each scaling rule.
     */
    protected void initializeEvaluators() {
        logger.info("Initialising evaluators for scaling policy rules...");
                
        this.evaluators = new MultiValueMap<Integer, PolicyRuleEvaluator>();
        this.allEvaluators = new ArrayList<PolicyRuleEvaluator>();
        
        // Rules that reference concrete operators are processed first
        PolicyRules rules = getEvalSubject();
        if(rules != null) {
            for(PolicyRule rule : rules) {
                Operator op = rule.getOperator();
                if(op instanceof OneOperator) {
                    Integer operatorId = 
                                Integer.valueOf(((OneOperator)op).getId());

                    logger.debug("Creating evaluator for operator [" + operatorId + "]");
                    logger.debug("Binding to rule " + rule.toString());

                    evaluators.put(operatorId, 
                                new PolicyRuleEvaluator(
                                    rule, getEvalAdaptor(), 
                                    new SystemTimeReference()));
                }


            }

            // Now process wildcard rules that apply to all operators
            for(PolicyRule rule : rules) {
                Operator op = rule.getOperator();
                if(op instanceof AllOperators) {
                    logger.debug("Creating evaluator for all operators");
                    logger.debug("Binding to rule " + rule.toString());

                    allEvaluators.add(
                                new PolicyRuleEvaluator(
                                    rule, getEvalAdaptor(), 
                                    new SystemTimeReference()));
                }
            }

            // Finally, the evaluators that apply to all operators should be referenced
            // by all operator identifiers in the map for OneOperator instances. This
            // facilitates the routing to the correct evaluators for a metric reading
            for(Integer operatorId : evaluators.keySet()) {
                for(PolicyRuleEvaluator allEvaluator : allEvaluators) {
                    evaluators.put(operatorId, allEvaluator);
                }
            }
        }
        
        logger.info("Done initialising evaluators for scaling policy rules");
    }
    
    /**
     * Routes metric reading to the appropriate evaluator given the operator 
     * identifier in the reading and the operator(s) referenced by each scaling 
     * rule.
     * @param provider Metric reading provider
     */
    protected void routeReadingToEvaluators(MetricReadingProvider provider) {
        
        if((evaluators != null) && (provider != null)) {
            Integer operatorId = Integer.valueOf(provider.getOperatorId());
            logger.info("Received metric reading for operator [" 
                                            + operatorId.intValue() + "]");
        
            // If there is at least one operator for the identifier, then forward
            // to these evaluators (which will also include any rules with wildcards)
            if(evaluators.containsKey(operatorId)) {
                for(PolicyRuleEvaluator e : evaluators.getCollection(operatorId)) {
                    logger.info("Routing to evaluator " + e.toString());

                    routeReadingToEvaluator(e, provider);
                }
            } else {
                // No specific rules for the operator, only forward to evaluators
                // not associated to any concrete operator
                for(PolicyRuleEvaluator allEvaluator : allEvaluators) {
                    logger.info("Routing to evaluator " + allEvaluator.toString());
                    routeReadingToEvaluator(allEvaluator, provider);
                }
            }
        }
    }
    
    protected void routeReadingToEvaluator(
            final PolicyRuleEvaluator evaluator, final MetricReadingProvider provider) {
        evaluator.evaluate(provider);
    }
}
