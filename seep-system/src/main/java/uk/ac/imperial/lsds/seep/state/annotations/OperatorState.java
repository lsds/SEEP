package uk.ac.imperial.lsds.seep.state.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OperatorState {
	boolean partitionable() default true;
}
