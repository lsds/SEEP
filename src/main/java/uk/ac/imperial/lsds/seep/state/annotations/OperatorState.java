package uk.ac.imperial.lsds.seep.state.annotations;

public @interface OperatorState {
	int partitionable() default 1;
}
