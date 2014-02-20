package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.action;

/**
 * Class representing a scale-in action. This class is used simply as a marker
 * class, to indicate if a scaling factor needs to be applied growing or reducing
 * the number of nodes/VMs allocated to a particular operator.
 * 
 * @author mrouaux
 */
public class ScaleInAction implements Action {

    @Override
    public String toString() {
        return "ScaleInAction{" + '}';
    }
}
