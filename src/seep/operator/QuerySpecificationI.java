package seep.operator;

import seep.comm.routing.Router;

public interface QuerySpecificationI {

	int getOperatorId();

	OperatorContext getOpContext();
	
	public void setOpContext(OperatorContext opContext);

	public void connectTo(QuerySpecificationI down, boolean originalQuery);
	
	public void setRoutingQueryFunction(String queryFunction_methodName);
	
	public void route(Router.RelationalOperator operand, int value, Operator toConnect);
	
	public void scaleOut(Operator toScaleOut);
	
	///\fixme{this should be done automatically}
//	public void set();

}
