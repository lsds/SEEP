package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.evaluate;

import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.InfrastructureAdaptor;
import uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util.MetricReadingProvider;

/**
 * 
 * @author mrouaux
 */
public abstract class AbstractEvaluator<T, 
                            U extends InfrastructureAdaptor,
                            V extends MetricReadingProvider> {
    
    private T subject; 
    private U adaptor;
    
    protected AbstractEvaluator(final T subject, final U adaptor) {
        this.subject = subject;
        this.adaptor = adaptor;
    }
    
    public abstract void evaluate(V provider);

    public T getEvalSubject() {
        return subject;
    }

    public U getEvalAdaptor() {
        return adaptor;
    }
}
