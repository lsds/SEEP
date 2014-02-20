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
