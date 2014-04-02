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
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author mrouaux
 */
public abstract class PolicyRules implements Iterable<PolicyRule> {
    
    private final List<PolicyRule> rules;
    
    protected PolicyRules() {
        rules = new ArrayList<PolicyRule>();
    }
    
    /**
     * 
     * @return 
     */
    protected PolicyRuleBuilder rule() {
        String defaultName = PolicyRule.DEFAULT_POLICY_RULE_NAME 
                                + "-" + rules.size();
        return rule(defaultName);
    }
    
    /**
     * 
     * @param name
     * @return 
     */
    protected PolicyRuleBuilder rule(String name) {
        return new PolicyRuleBuilder(name,
            // Add the built rule to the list of rules
            new PolicyRuleBuilder.PolicyRuleBuilderListener() {
                @Override
                public void onRuleBuilt(PolicyRule builtRule) {
                    rules.add(builtRule);
                }
            });
    }

    @Override
    public Iterator<PolicyRule> iterator() {
        return rules.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(PolicyRule rule : rules) {
            sb.append(rule.toString());
            sb.append(",");
        }
        
        if(sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        
        return "PolicyRules{" 
                    + "rules=" + sb.toString() + '}';
    }
}
