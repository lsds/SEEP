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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRule;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.OneOperator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.RelativeScaleConstraint;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.ScaleConstraint;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.trigger.ActionTrigger;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.InfrastructureAdaptor;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReading;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReadingProvider;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.TimeReference;

/**
 * Evaluator for a single policy rule. The class relies on an infrastructure adaptor
 * to inform the platform the new size of a given operator and query it to determine
 * its current size, when needed. Using an adaptor instigates decoupling from the
 * rest of the platform and facilitates testing.
 * 
 * @author mrouaux
 */
public class PolicyRuleEvaluator 
    extends AbstractEvaluator<PolicyRule, InfrastructureAdaptor, MetricReadingProvider>{
    
    private static final Logger logger = 
                            LoggerFactory.getLogger(PolicyRuleEvaluator.class);
  
    private Queue<MetricReading> pastReadings;
    private Period maximumAge;
    private TimeReference clock;
            
   /**
     * Convenience constructor
     * @param rules scaling policy rules to evaluate
     * @param adaptor infrastructure adaptor (allows evaluators to delegate resizing
     * of a particular operator as a consequence of an action triggered by some
     * of the rules being evaluated). An adaptor is used to decouple policy 
     * evaluation from execution.
     */
    public PolicyRuleEvaluator(final PolicyRule rule,
                               final InfrastructureAdaptor adaptor,
                               final TimeReference clock) {
        super(rule, adaptor);
        
        // Some rules need extra runtime configuration before being evaluated
        initRule(rule, adaptor);
        
        this.clock = clock;
        
        // Initialise the evaluator
        pastReadings = new LinkedList<MetricReading>();
        calcMaxAgeForReadings();
    }

    /**
     * 
     * @param provider 
     */
    @Override
    public void evaluate(MetricReadingProvider provider) {
        InfrastructureAdaptor adaptor = getEvalAdaptor();
        PolicyRule rule = getEvalSubject();

        MetricReading reading = provider.nextReading();
        if(reading != null) {
            pastReadings.offer(reading);
        }
        
        // Now prune old readings that are no longer needed given the time 
        // threshold for the current rule. There might be multiple time thresholds
        // (as we support multiple triggers per rule). We need to keep enough 
        // readings for the one with the longest time horizon
        pruneOldReadings(maximumAge);
        
        // Evaluate all the triggers for the rule, when all are fired and have 
        // changed, we need to execute the associated scaling action
        boolean scale = true;
        
        // Determine if we need to scale, the algorithm has to check all the readings
        // Delegate to triggers as they implement the logic to determine whether to 
        // scale or not. All triggers need to be evaluated.
        List<ActionTrigger> triggers = rule.getTriggers();
        
        if((triggers == null) || (triggers.isEmpty())) {
            scale = false;
        } else {
            // Evaluate each one of the triggers
            for(ActionTrigger trigger : triggers) {
                trigger.evaluate(new ArrayList<MetricReading>(pastReadings), clock);
                
                scale = (scale && (trigger.isFired() && trigger.hasChanged()));
                if(!scale) {
                    // If one trigger evaluates to false, we are done. No need to 
                    // continue checking the rest as the rule will not scale.
                    break;
                }
            }
        }
        
        // Execute the scale action
        if(scale) {
            if(rule.getOperator() instanceof OneOperator) {
                OneOperator op = (OneOperator)rule.getOperator();
                int operatorId = op.getId();
                
                // Scaling is applied to a single operator in the query
                scaleForSingleOperator(operatorId, rule, adaptor);
            } else {
                // Operator is of type AllOperators, scaling needs to be applied
                // to all the operators in the query.
                scaleForAllOperators(rule, adaptor);
            }
        }
    }
    
    /**
     * Scale a single operator identified by operatorId.
     * 
     * @param operatorId Operator identifier
     * @param rule Rule that governs the scaling (this method assumes that all
     * riggers associated to the rule have already been evaluated and they have
     * all fired accordingly).
     * @param adaptor Infrastructure adaptor
     */
    private void scaleForSingleOperator(final int operatorId,
                                        final PolicyRule rule, 
                                        final InfrastructureAdaptor adaptor) {
        
        // Calculate new size for operator by applying scale factor
        int currenSize = adaptor.getOperatorCurrentSize(operatorId);
        int scaledSize = rule.getScaleFactor()
                            .apply(currenSize, rule.getAction());

        boolean constraintExceeded = false;

        ScaleConstraint constraint = rule.getScaleConstraint();
        if(constraint != null) {
            constraintExceeded = constraint.evaluate(scaledSize);
        }

        if(!constraintExceeded) {
            adaptor.setOperatorScaledSize(operatorId, scaledSize);
        }
    }
    
    /**
     * Scale for all operators in the current query.
     * 
     * @param rule Rule that governs the scaling (this method assumes that all
     * riggers associated to the rule have already been evaluated and they have
     * all fired accordingly).
     * @param adaptor Infrastructure adaptor
     */
    private void scaleForAllOperators(final PolicyRule rule, 
                                      final InfrastructureAdaptor adaptor) {
        
        List<Integer> operatorIds = adaptor.getOperatorIds();
        if(operatorIds != null) {
            for(Integer operatorId : operatorIds) {
                scaleForSingleOperator(operatorId, rule, adaptor);
            }
        }
    }
    
    /**
     * Prune old readings from the queue of readings required to evaluate all the
     * triggers associated to the scaling rule bound to this evaluator.
     * 
     * @param maximumAge Maximum age for metric readings in the queue. Any readings
     * which have a timestamp older than this age can be safely discarded as they
     * are no longer needed to evaluate the state of the rule triggers.
     */
    private void pruneOldReadings(Period maximumAge) {
        boolean done = false;
        MetricReading reading = pastReadings.peek();
        
        while((reading != null) && !done) {
            Period readingAge = new Period(
                                    reading.getTimestamp(), Instant.now());
            
            // Once we find a reading with an age less than the maximum age, we
            // stop prunning as readings in the queue are sorted. An evaluator
            // works based on the assumption that metrics readings are offered 
            // with always increasing timestamps.
            if(readingAge.toStandardSeconds()
                        .isLessThan(maximumAge.toStandardSeconds())) {
                done = true;
            } else {
                pastReadings.poll();

                // Get the next element in the queue and continue pruning
                reading = pastReadings.peek();
            }
        }
    }
    
    /**
     * Given all the triggers associated to the scaling rule, it finds the one
     * with the longest time threshold (longest horizon). The evaluator needs to
     * be aware of this as this is the cut-off for metric readings, any readings 
     * older than the maximum time threshold can safely be discarded.
     */
    private void calcMaxAgeForReadings() {
        PolicyRule rule = getEvalSubject();
        maximumAge = null;
        
        for(ActionTrigger trigger : rule.getTriggers()) {
            Period triggerAge = trigger.getTimeThreshold().toPeriod();
            
            if(maximumAge == null) {
                maximumAge = triggerAge;
            } else {
                if(triggerAge.toStandardSeconds()
                        .isGreaterThan(maximumAge.toStandardSeconds())) {
                    maximumAge = triggerAge;
                }   
            }
        }
    }
    
    /**
     * Initializes a policy rule. 
     * 
     * @param rule
     * @param adaptor 
     */
    private void initRule(PolicyRule rule, InfrastructureAdaptor adaptor) {
        if(rule.getScaleConstraint() instanceof RelativeScaleConstraint) {
            if(rule.getOperator() instanceof OneOperator) {
                OneOperator op = (OneOperator)rule.getOperator();
                
                ((RelativeScaleConstraint)rule.getScaleConstraint())
                    .withOriginalSize(
                        adaptor.getOperatorOriginalSize(op.getId()));
            }   
        }
    }

    @Override
    public String toString() {
        PolicyRule rule = getEvalSubject();
        return "PolicyRuleEvaluator{" + rule.toString() + '}';
    }
}
