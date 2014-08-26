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
package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.Builder;
import java.util.ArrayList;
import java.util.List;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.Action;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.ScaleInAction;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.ScaleOutAction;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.metric.MetricName;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.Operator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor.ScaleFactor;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.ScaleConstraint;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.MetricThreshold;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.TimeThreshold;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.trigger.ActionTrigger;

/**
 * 
 * @author mrouaux
 */
public class PolicyRuleBuilder implements Builder<PolicyRule> {
	
    /**
     * Listener to notify others when the builder creates a new PolicyRule instance.
     * Classes that create their
     */
    public interface PolicyRuleBuilderListener {
        
        void onRuleBuilt(PolicyRule builtRule);
    
    } 
    
    private PolicyRuleBuilderListener listener;
    
    private String name;
    private Action action;
    private List<ActionTrigger> triggers;
	private Operator operator;
	private ScaleFactor scaleFactor;
	private ScaleConstraint scaleConstraint;
    private TimeThreshold scaleGuardTime;
	
    private MetricName triggerMetricName;
    private MetricThreshold triggerMetricThreshold;
    private TimeThreshold triggerTimeThreshold;
    
    /**
     * Default constructor
     */
    PolicyRuleBuilder() {
        this.listener = null;

        this.name = null;
		this.action = null;
        this.triggers = new ArrayList<ActionTrigger>();
		this.operator = null;
        this.scaleFactor = null;
        this.scaleConstraint = null;        
    }

    /**
     * Convenience constructor
     * @param name Name of the rule that will be created by the builder instance.
     */
	PolicyRuleBuilder(final String name) {
        
        this.listener = null;
		
        this.name = name;
		this.action = null;
        this.triggers = new ArrayList<ActionTrigger>();
		this.operator = null;
        this.scaleFactor = null;
        this.scaleConstraint = null;
	}
    
    /**
     * Convenience constructor
     * @param name Name of the rule that will be created by the builder instance.
     * @param listener Listener to notify whenever a new rule is built.
     */
	PolicyRuleBuilder(final String name, 
                      final PolicyRuleBuilderListener listener) {
        
        this.listener = listener;
		
        this.name = name;
		this.action = null;
        this.triggers = new ArrayList<ActionTrigger>();
		this.operator = null;
        this.scaleFactor = null;
        this.scaleConstraint = null;
	}

	public PolicyRuleBuilder scaleIn(final Operator operator) {
		this.action = new ScaleInAction();
        this.operator = operator;
		return this;
	}
	
	public PolicyRuleBuilder scaleOut(final Operator operator) {
		this.action = new ScaleOutAction();
        this.operator = operator;
		return this;		
	}
    
	public PolicyRuleBuilder by(final ScaleFactor scaleFactor) {
		this.scaleFactor = scaleFactor;
		return this;
	}
	
	public PolicyRuleBuilder butNeverBelow(final ScaleConstraint scaleConstraint) {
		this.scaleConstraint = scaleConstraint;
		return this;
	}
	
	public PolicyRuleBuilder butNeverAbove(final ScaleConstraint scaleConstraint) {
		this.scaleConstraint = scaleConstraint;
		return this;
	}
    
    public PolicyRuleBuilder when(MetricName metricName) {
        this.triggerMetricName = metricName;
        return this;
    }
    
    public PolicyRuleBuilder and(MetricName metricName) {
        this.triggerMetricName = metricName;
        return this;
    }

    public PolicyRuleBuilder is(MetricThreshold metricThreshold) {
        this.triggerMetricThreshold = metricThreshold;
        return this;
    }
    
    public PolicyRuleBuilder forAtLeast(TimeThreshold timeThreshold) {
        this.triggerTimeThreshold = timeThreshold;
        
        // Create new trigger for the rule action and to the list of triggers 
        // for the current rule. One rule can have multiple triggers that need
        // to fire simultaneously in order for the associate action to be executed.
        triggers.add(new ActionTrigger(
                            triggerMetricThreshold, 
                            triggerTimeThreshold, 
                            triggerMetricName));
        
        return this;
    }
	
    public PolicyRuleBuilder withNoScaleInSince(TimeThreshold scaleGuardTime) {
        this.scaleGuardTime = scaleGuardTime;
        return this;
    }

    public PolicyRuleBuilder withNoScaleOutSince(TimeThreshold scaleGuardTime) {
        this.scaleGuardTime = scaleGuardTime;
        return this;
    }
            
	@Override
	public PolicyRule build() {
        // Create new policy rule instance with specified parameters
		PolicyRule policyRule = 
				new PolicyRule(
                    name,
					action,
                    triggers,
					operator,
					scaleFactor,
					scaleConstraint,
                    scaleGuardTime);
		
        // Notify interested parties that a new rule has just been created
        if(listener != null) {
            listener.onRuleBuilt(policyRule);
        }
        
		return policyRule;
	}
}
