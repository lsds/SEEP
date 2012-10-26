package seep.operator.collection.financialQuery;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Sink extends Operator implements StatelessOperator{

	private static final long serialVersionUID = 1L;

	public Sink(int opID) {
		super(opID);
		subclassOperator = this;
	}

	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void processData(DataTuple dt) {
		System.out.println("Parity found");
		System.out.println("EXCHANGE_A: "+dt.getExchangeId()+" EXCHANGE_B: "+dt.getxParity());
		System.out.println("PRICE: "+dt.getStrikePrice()+" expires on "+dt.getExpiryDay()+" year: "+dt.getExpiryYear());
		System.out.println("");
		System.out.println("");
	}
}
