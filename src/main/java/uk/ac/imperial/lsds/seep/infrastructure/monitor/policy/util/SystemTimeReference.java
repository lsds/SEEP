package uk.ac.imperial.lsds.seep.infrastructure.monitor.policy.util;

import org.joda.time.Instant;

/**
 * Provides a time reference for rule evaluation, based on the current system time.
 * 
 * @author mrouaux
 */
public class SystemTimeReference implements TimeReference {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
