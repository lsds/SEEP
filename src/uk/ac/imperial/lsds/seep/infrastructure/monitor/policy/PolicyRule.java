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

import java.util.ArrayList;
import java.util.List;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action.Action;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.operator.Operator;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.factor.ScaleFactor;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint.ScaleConstraint;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.threshold.TimeThreshold;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.trigger.ActionTrigger;

/**
 * Scaling policy rule. A rule is defined by:
 * <p><ul>
 * <li>An action to either scale up or scale down.
 * <li>A trigger that indicates when the action needs to be executed (e.g.:
 * certain metric above a given value for a predefined period of time).
 * <li>A query operator to which the scaling action and the trigger apply.
 * <li>A scaling factor that indicates by how much the given operator should be
 * scaled when the trigger for the action fires.
 * <li>A constraint restricting by how much an operator can be scaled up or down.
 * <li>A guard time since the last complementary action (e.g.: complement of scale
 * in is scale out) was executed for the operator affected by this rule.
 * <ul><p>
 * Instances of this class are not meant to be created directly from outside its
 * containing package. A {@link PolicyRuleBuilder} instance should be used instead
 * to construct new scaling rules (this builder exposes a fluent interface that
 * is meant to facilitate the definition of policy rules).
 * 
 * @author mrouaux
 */
public class PolicyRule {

    public static final String DEFAULT_POLICY_RULE_NAME = "default-rule";
    
    private String name;
	private Action action;
    private List<ActionTrigger> triggers;
	private Operator operator;
	private ScaleFactor scaleFactor; 
	private ScaleConstraint scaleConstraint;
    private TimeThreshold scaleGuardTime;

    /**
     * Default constructor
     */
    PolicyRule() {
        this.name = DEFAULT_POLICY_RULE_NAME;
        
        this.action = null;
        this.triggers = new ArrayList<ActionTrigger>();
        this.operator = null;
        this.scaleFactor = null;
        this.scaleConstraint = null;
        this.scaleGuardTime = null;
    }

    /**
     * Convenience constructor
     * @param name Name of the scaling rule
     * @param action Action to execute when the rule becomes active.
     * @param trigger Trigger that indicates when the action must be executed. 
     * @param operator Operator or operators to which the scaling action applies.
     * @param scaleFactor Factor to scale by
     * @param scaleConstraint Constraint that restrict the amount of scaling (up
     * or down) for the given operator.
     * @param scaleGuardTime
     */
    PolicyRule(final String name, 
                final Action action, 
                final List<ActionTrigger> triggers, 
                final Operator operator, 
                final ScaleFactor scaleFactor, 
                final ScaleConstraint scaleConstraint,
                final TimeThreshold scaleGuardTime) {
        
        this.name = name;
        this.action = action;
        this.triggers = triggers;
        this.operator = operator;
        this.scaleFactor = scaleFactor;
        this.scaleConstraint = scaleConstraint;
        this.scaleGuardTime = scaleGuardTime;
    }

    /**
     * 
     * @return 
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 
     * @return 
     */
    public Action getAction() {
        return action;
    }

    /**
     * 
     * @param action 
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * 
     * @return 
     */
    public List<ActionTrigger> getTriggers() {
        return triggers;
    }

    /**
     * 
     * @param triggers 
     */
    public void setTriggers(List<ActionTrigger> triggers) {
        this.triggers = triggers;
    }

    /**
     * 
     * @return 
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * 
     * @param operator 
     */
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    /**
     * 
     * @return 
     */
    public ScaleFactor getScaleFactor() {
        return scaleFactor;
    }

    /**
     * 
     * @param scaleFactor 
     */
    public void setScaleFactor(ScaleFactor scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    /**
     * 
     * @return 
     */
    public ScaleConstraint getScaleConstraint() {
        return scaleConstraint;
    }

    /**
     * 
     * @param scaleConstraint 
     */
    public void setScaleConstraint(ScaleConstraint scaleConstraint) {
        this.scaleConstraint = scaleConstraint;
    }

    /**
     * 
     * @return 
     */
    public TimeThreshold getScaleGuardTime() {
        return scaleGuardTime;
    }

    /**
     * 
     * @param scaleGuardTime 
     */
    public void setScaleGuardTime(TimeThreshold scaleGuardTime) {
        this.scaleGuardTime = scaleGuardTime;
    }

    /**
     * 
     * @return 
     */
    @Override
    public String toString() {
        // Construct a readable representation of the list of triggers 
        // associated to the scaling rule.
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(ActionTrigger trigger : triggers) {
            sb.append(trigger.toString());
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        
        // Return string representation of the scaling rule
        return "PolicyRule{" 
                        + "name=" + name 
                        + ", action=" + action 
                        + ", trigger=" + sb.toString()
                        + ", operator=" + operator 
                        + ", scaleFactor=" + scaleFactor 
                        + ", scaleConstraint=" + scaleConstraint 
                        + ", scaleGuardTime=" + scaleGuardTime + '}';
    }
}
