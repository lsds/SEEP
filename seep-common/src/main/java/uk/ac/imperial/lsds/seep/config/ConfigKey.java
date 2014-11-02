package uk.ac.imperial.lsds.seep.config;

import uk.ac.imperial.lsds.seep.config.ConfigDef.Importance;
import uk.ac.imperial.lsds.seep.config.ConfigDef.Type;
import uk.ac.imperial.lsds.seep.config.ConfigDef.Validator;

public class ConfigKey {
	
	private static final Object NO_DEFAULT_VALUE = new String("");
	
    public final String name;
    public final Type type;
    public final String documentation;
    public final Object defaultValue;
    public final Validator validator;
    public final Importance importance;

    public ConfigKey(String name, Type type, Object defaultValue, Validator validator, Importance importance, String documentation) {
        super();
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.importance = importance;
        if (this.validator != null)
            this.validator.ensureValid(name, defaultValue);
        this.documentation = documentation;
    }

    public boolean hasDefault() {
        return this.defaultValue != NO_DEFAULT_VALUE;
    }
}
