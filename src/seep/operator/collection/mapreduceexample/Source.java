package seep.operator.collection.mapreduceexample;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import seep.comm.serialization.DataTuple;
import seep.operator.Operator;
import seep.operator.StatelessOperator;

public class Source extends Operator implements StatelessOperator{
	
	private static final long serialVersionUID = 1L;

	private String wikiData = "";
	
	public Source(int opId){
		super(opId);
		subclassOperator = this;
	}

	@Override
	public void processData(DataTuple dt) {
		Kryo kryo = new Kryo();
		kryo.register(DataTuple.class);

		InputStream is = null;
		try {
			is = new FileInputStream(wikiData);
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Input i = new Input(is);
		while(true){
			DataTuple tuple = kryo.readObject(i, DataTuple.class);
			sendDown(tuple);
			try{
				Thread.sleep(1);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public boolean isOrderSensitive() {
		// TODO Auto-generated method stub
		return false;
	}
}
