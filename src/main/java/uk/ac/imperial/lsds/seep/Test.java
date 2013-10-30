package uk.ac.imperial.lsds.seep;

import uk.ac.imperial.lsds.seep.api.largestateimpls.SeepMap;
import uk.ac.imperial.lsds.seep.state.CustomState;
import uk.ac.imperial.lsds.seep.state.LargeState;
import uk.ac.imperial.lsds.seep.state.Partitionable;
import uk.ac.imperial.lsds.seep.state.StateWrapper;
import uk.ac.imperial.lsds.seep.state.Streamable;
import uk.ac.imperial.lsds.seep.state.Versionable;

public class Test {
	
	public static void main(String args[]){
		
		SeepMap sm = new SeepMap();
		
		if(sm instanceof LargeState){
			System.out.println("it's LargeState");
		}
		if(sm instanceof Versionable){
			System.out.println("it's Versionable");
		}
		if(sm instanceof Streamable){
			System.out.println("it's Streamable");
		}
		
		Test test = new Test();
		T t = test.new T();
		
		if(t instanceof CustomState){
			System.out.println("it's CustomState");
		}
		if(t instanceof Partitionable){
			System.out.println("it's Partitionable");
		}
		
	}
	
	public class T implements CustomState, Partitionable{
		public int a;

		@Override
		public String getKeyAttribute() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void resetState() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setKeyAttribute(String keyAttribute) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public StateWrapper[] splitState(StateWrapper toSplit, int key) {
			// TODO Auto-generated method stub
			return null;
		}
	}

}
