package uk.ac.imperial.lsds.seep.infrastructure.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.InfrastructureAdaptor;
import uk.ac.imperial.lsds.seep.operator.Operator;

/**
 *
 * @author mrouaux
 */
public class SeepInfrastructureAdaptor implements InfrastructureAdaptor {

    private Infrastructure infrastructure;
    private Map<Integer, Integer> originalSizes;
    
    /**
     * Convenience constructor.
     * @param infrastructure 
     */
    public SeepInfrastructureAdaptor(Infrastructure infrastructure) {
        this.infrastructure = infrastructure;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public List<Integer> getOperatorIds() {
        List<Integer> operatorIds = new ArrayList<Integer>();
        for(Operator op : infrastructure.getOps()) {
            operatorIds.add(op.getOperatorId());
        }
        
        return operatorIds;
    }

    /**
     * 
     * @param operatorId
     * @return 
     */
    @Override
    public int getOperatorCurrentSize(int operatorId) {
        return infrastructure.getNodeIdsForOperatorId(operatorId).size();
    }

    /**
     * 
     * @param operatorId
     * @param size 
     */
    @Override
    public void setOperatorScaledSize(int operatorId, int size) {
        // Get the current size for the operator
        int currSize = getOperatorCurrentSize(operatorId);
        
        // If the desired size is greater than the current operator size, there
        // are fewer nodes that necessary and we need to scale out. The elastic
        // tools only provide a method to scale operators by one, we might need 
        // to scale a few times to achieve the desired size.
        if(size > currSize) {
            for(int i = 0; i < size - currSize; i++) {
                infrastructure.getEiu().alert(operatorId);
            }
        }
    }

    /**
     * 
     * @param operatorId
     * @return 
     */
    @Override
    public int getOperatorOriginalSize(int operatorId) {
        if(!originalSizes.containsKey(operatorId)) {
            // Use the current size as the original size if we haven't recorded this yet
            originalSizes.put(operatorId, getOperatorCurrentSize(operatorId));
        }
        
        return originalSizes.get(operatorId);
    }
}
