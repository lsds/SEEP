package seep.operator;

public interface Connectable {

	int getOperatorId();

	OperatorContext getOpContext();

	public void connectTo(Connectable down);

}
