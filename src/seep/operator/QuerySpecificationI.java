package seep.operator;

import seep.comm.routing.Router;
import seep.runtimeengine.CoreRE;
import seep.runtimeengine.RuntimeContext;

public interface QuerySpecificationI {

	int getOperatorId();

	RuntimeContext getOpContext();
	
	public void setOpContext(RuntimeContext opContext);

	public void connectTo(QuerySpecificationI down, boolean originalQuery);
	
	public void setRoutingQueryFunction(String queryFunction_methodName);
	
	public void route(Router.RelationalOperator operand, int value, CoreRE toConnect);
	
	public void scaleOut(CoreRE toScaleOut);
	
	///\fixme{this should be done automatically}
//	public void set();

}
