package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.scale.constraint;

/**
 * Absolute scaling constraint for a policy rule. This class represents an absolute 
 * constraint on the scaling-out permitted for a given operator or set of operators.
 * The scaling-in constraint is implicit and assumed to be 1 (e.g.: each operator is 
 * mapped to at least one node/VM in the physical query plan). 
 * 
 * @author mrouaux
 */
public class AbsoluteScaleConstraint extends ScaleConstraint {

    /**
     * Convenience constructor.
     * @param constraint Absolute scaling constraint for a policy rule.
     */
    AbsoluteScaleConstraint(int constraint) {
        super(constraint);
    }
    
    /**
     * @param scaledSize Size after scaling for a given operator. 
     * @return True if current scaling exceeds absolute constraint.
     */
    public boolean evaluate(int scaledSize) {
        return (scaledSize > getValue());
    }
}
