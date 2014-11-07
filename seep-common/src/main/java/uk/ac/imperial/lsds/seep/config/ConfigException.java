package uk.ac.imperial.lsds.seep.config;

import uk.ac.imperial.lsds.seep.errors.SeepException;

/**
 * Thrown if the user supplies an invalid configuration
 */
public class ConfigException extends SeepException {

    private static final long serialVersionUID = 1L;

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String name, Object value) {
        this(name, value, null);
    }

    public ConfigException(String name, Object value, String message) {
        super("Invalid value " + value + " for configuration " + name + (message == null ? "" : ": " + message));
    }

}

