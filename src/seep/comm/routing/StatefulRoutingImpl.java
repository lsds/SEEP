package seep.comm.routing;

import java.util.ArrayList;

public class StatefulRoutingImpl implements RoutingStrategyI{
		
	//These structures store the information for doing consistent hashing
	//downstreamNodeKeys stores the keys (within the INTEGER space of keys) where each node lies
	private ArrayList<Integer> downstreamNodeKeys = null;
	private ArrayList<Integer> keyToDownstreamRealIndex = null;
		
	public ArrayList<Integer> getDownstreamNodeKeys() {
		return downstreamNodeKeys;
	}

	public void setDownstreamNodeKeys(ArrayList<Integer> downstreamNodeKeys) {
		this.downstreamNodeKeys = downstreamNodeKeys;
	}

	public ArrayList<Integer> getKeyToDownstreamRealIndex() {
		return keyToDownstreamRealIndex;
	}

	public void setKeyToDownstreamRealIndex(ArrayList<Integer> keyToDownstreamRealIndex) {
		this.keyToDownstreamRealIndex = keyToDownstreamRealIndex;
	}
		
	public StatefulRoutingImpl(ArrayList<Integer> keyToDownstreamRealIndex, ArrayList<Integer> downstreamNodeKeys){
		this.keyToDownstreamRealIndex = keyToDownstreamRealIndex;
		this.downstreamNodeKeys = downstreamNodeKeys;
	}
		
	public StatefulRoutingImpl(int realIndex){
		//Initialize structures to do consistent hashing
		downstreamNodeKeys = new ArrayList<Integer>();
		keyToDownstreamRealIndex = new ArrayList<Integer>();
			
		//For every downstream of the same type, save in a virtual index, the real index of that downstream
		keyToDownstreamRealIndex.add(realIndex);
			
		//Compute the key and add it in the structure
		int key = Integer.MAX_VALUE;
		downstreamNodeKeys.add(key);
	}
		
	public int newReplica(int oldOpIndex, int newOpIndex) {
		//map real index to virtual index
	/**		int oldVirtualIndex = reverseMap(oldOpIndex); */
		int oldVirtualIndex = keyToDownstreamRealIndex.indexOf(oldOpIndex);
		//store newOpIndex as a virtualIndex
			
	/**		virtualIndexToRealIndex.put(virtualIndex, newOpIndex); */
	//System.out.println("OLD_VIRTUAL_INDEX: "+oldVirtualIndex);
		//. decide where to split key space
		int key = downstreamNodeKeys.get(oldVirtualIndex); 
		//previous key is the min value if there was just one operator or the key of the previous operator to oldOpIndex
		int previousKey = oldVirtualIndex == 0 ? Integer.MIN_VALUE : downstreamNodeKeys.get(oldVirtualIndex-1);
		//the new key is the medium point between key and previous key
		long maxInteger = Integer.MAX_VALUE;
		long difference = (key == Integer.MAX_VALUE && previousKey == Integer.MIN_VALUE) ? (maxInteger-Integer.MIN_VALUE) : (key-previousKey);
		difference = (difference < 0) ? (difference * -1) : difference;

		long aux = key - (difference/2);
		int newKey = (int)aux;
	//System.out.println("KEY: "+key+" PREV_KEY: "+previousKey+" DIF: "+difference+" AUX: "+aux+" NEW_KEY: "+newKey);

		// install the new key and operator for dispatching, from this moment new tuples are buffered on
		// store in oldOpIndex, the value of newOpIndex, (so insert by the left)
	//explain +1
		keyToDownstreamRealIndex.add(oldVirtualIndex+1, newOpIndex);
		downstreamNodeKeys.add(oldVirtualIndex, newKey);

			
		return newKey;
	}
		
	/// \todo{OPTIMIZE THIS METHOD}
	public ArrayList<Integer> route(ArrayList<Integer> targets, int value) {
		int hash = Router.customHash(value);
		//int hash = value;
		int realIndex = -1;
		/** for(Integer nodeKey : downstreamNodeKeys){ */
		for(int i = 0; i<downstreamNodeKeys.size(); i++){
			int nodeKey = downstreamNodeKeys.get(i);
	//System.out.println("DNK: "+downstreamNodeKeys);
	//System.out.println("DOW: "+keyToDownstreamRealIndex);
			//If yes then we have the node to route the info
			if(hash < nodeKey){
				realIndex = keyToDownstreamRealIndex.get(i);

				if(!targets.contains(realIndex)){
					targets.add(realIndex);
				}
				return targets;
			}
		}
		return null;
	}

	public ArrayList<Integer> route(int value) {
//		System.out.println("WARNING...route(int value) in StatefulRoutingImpl");
		ArrayList<Integer> targets = new ArrayList<Integer>();
		return route(targets, value);
	}
}
