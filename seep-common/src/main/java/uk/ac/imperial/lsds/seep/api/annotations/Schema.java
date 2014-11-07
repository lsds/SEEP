package uk.ac.imperial.lsds.seep.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import uk.ac.imperial.lsds.seep.config.ConfigDef.Type;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Schema {
	String[] fieldname();
	Type[] types();
}
