package uk.ac.imperial.lsds.seep.infrastructure.monitor.master;

import uk.ac.imperial.lsds.seep.GLOBALS;
import uk.ac.imperial.lsds.seep.infrastructure.master.Infrastructure;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.Factory;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.SeepInfrastructureAdaptor;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.PolicyRules;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.InfrastructureAdaptor;

/**
 * Factory class for MonitorMaster objects.
 * @author mrouaux
 */
public class MonitorMasterFactory implements Factory<MonitorMaster> {

    private Infrastructure infrastructure;
    private PolicyRules rules;
    private int masterPort;

    /**
     * Convenience constructor.
     * @param infrastructure Underlying infrastructure over which to apply scaling 
     * decisions.
     * @param rules Scaling rules that control scaling for the current query.
     * @param monitorMasterPort TCP port on which to listen for incoming slave 
     * connections.
     */
    public MonitorMasterFactory(final Infrastructure infrastructure, 
                final PolicyRules rules) {
        
        this.infrastructure = infrastructure;
        this.rules = rules;
        
        this.masterPort = Integer.valueOf(GLOBALS.valueFor("monitorManagerPort"));
    }
    
    /**
     * Creates and initialises a MonitorMaster instance.
     * @return MonitorMaster instance.
     */
    @Override
    public MonitorMaster create() {
        InfrastructureAdaptor adaptor 
                            = new SeepInfrastructureAdaptor(infrastructure);
        return new MonitorMaster(adaptor, rules, masterPort);
    }
}
