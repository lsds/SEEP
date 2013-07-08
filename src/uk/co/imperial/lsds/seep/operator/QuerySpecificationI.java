package uk.co.imperial.lsds.seep.operator;


public interface QuerySpecificationI {

	int getOperatorId();

	OperatorContext getOpContext();
	
	public void setOpContext(OperatorContext opContext);

	public void connectTo(QuerySpecificationI down, boolean originalQuery);
	
//	public void setRoutingQueryFunction(String queryFunction_methodName);
	
//	public void route(Router.RelationalOperator operand, int value, Operator toConnect);
	
//	public void declareWorkingAttributes(String... attributes);
		
	///\fixme{this should be done automatically}
//	public void set();

}
