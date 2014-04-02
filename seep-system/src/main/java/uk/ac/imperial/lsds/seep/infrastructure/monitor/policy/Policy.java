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

/**
 * 
 * @author mrouaux
 */
public class Policy {
    
    public static final String DEFAULT_POLICY_NAME = "default-policy";
    
    private String name;
    private PolicyRules rules;

    public Policy() {
        this.name = DEFAULT_POLICY_NAME;
        this.rules = null;
    }

    public Policy(PolicyRules rules) {
        this.name = DEFAULT_POLICY_NAME;
        this.rules = rules;
    }
    
    public Policy(String name, PolicyRules rules) {
        this.name = name;
        this.rules = rules;
    }

    public String named(String name) {
        return name;
    }
    
    public PolicyRules getRules() {
        return rules;
    }
}
