package seep.comm.routing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.CRC32;


@SuppressWarnings("serial")
public class StatefulDynamicLoadBalancer implements Serializable, LoadBalancerI{
	
	private static CRC32 crc32 = new CRC32();
	
	//private boolean running = false;

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
	
	public StatefulDynamicLoadBalancer(ArrayList<Integer> keyToDownstreamRealIndex, ArrayList<Integer> downstreamNodeKeys){
		this.keyToDownstreamRealIndex = keyToDownstreamRealIndex;
		this.downstreamNodeKeys = downstreamNodeKeys;
	}
	
	public StatefulDynamicLoadBalancer(int realIndex){
		//Initialize structures to do consistent hashing
		downstreamNodeKeys = new ArrayList<Integer>();
		keyToDownstreamRealIndex = new ArrayList<Integer>();
		
		//For every downstream of the same type, save in a virtual index, the real index of that downstream
/**		virtualIndexToRealIndex.put(virtualIndex, realIndex); */ 
		keyToDownstreamRealIndex.add(realIndex);
		
		//Compute the key and add it in the structure
		int key = Integer.MAX_VALUE;
		downstreamNodeKeys.add(key);
/**		
		//Compute the virtual downstream node and add it in
		keyToDownstreamRealIndex.add(virtualIndex);
		
		//update virtual index
		virtualIndex++;
*/	
	}
	
	@Override
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
	
	/**	
		keyToDownstreamRealIndex.add(oldVirtualIndex+1, virtualIndex);
		downstreamNodeKeys.add(oldVirtualIndex, newKey);
		virtualIndex++;
	*/
		
//explain +1
		keyToDownstreamRealIndex.add(oldVirtualIndex+1, newOpIndex);
		downstreamNodeKeys.add(oldVirtualIndex, newKey);

//System.out.println("## UPDATED CONSISTENT HASHING ##");
//System.out.println("Keys: "+downstreamNodeKeys);
//System.out.println("Index: "+keyToDownstreamRealIndex);
		
		return newKey;
	}

//	private int reverseMap(int realIndex) {
//		for (Integer virtualIndex : virtualIndexToRealIndex.keySet()){
//			if(virtualIndexToRealIndex.get(virtualIndex) == realIndex){
//				return virtualIndex;
//			}
//		}
//		return -1;
//	}

	/// \todo{OPTIMIZE THIS METHOD}
//	public ArrayList<Integer> route(ArrayList<Integer> targets, Object value) {
//		int hash = 0;
//		if(value instanceof String){
//			hash = customHash(value.hashCode());
//		}
//		else if(value instanceof Integer){
//			hash = customHash(((Integer) value).intValue());
//		}
//		return route(targets, hash);
//	}
	
	/// \todo{OPTIMIZE THIS METHOD}
	public ArrayList<Integer> route(ArrayList<Integer> targets, int value) {
		int hash = customHash(value);
		//int hash = value;
		int realIndex = -1;
		/** for(Integer nodeKey : downstreamNodeKeys){ */
		for(int i = 0; i<downstreamNodeKeys.size(); i++){
			int nodeKey = downstreamNodeKeys.get(i);
//System.out.println("DNK: "+downstreamNodeKeys);
//System.out.println("DOW: "+keyToDownstreamRealIndex);
			//If yes then we have the node to route the info
			if(hash < nodeKey){
				/**
				int accessPoint = downstreamNodeKeys.indexOf(nodeKey);
//System.out.println("ACCESSPOINT: "+accessPoint);
				virtualIndex = keyToDownstreamRealIndex.get(accessPoint);
				**/
				
				/**realIndex = virtualIndexToRealIndex.get(accessIndex);*/
				realIndex = keyToDownstreamRealIndex.get(i);

				if(!targets.contains(realIndex)){
					targets.add(realIndex);
				}
				return targets;
			}
		}
		return null;
	}
	
	public static int customHash(int value){
		crc32.update(value);
		int v = (int)crc32.getValue();
		crc32.reset();
		return v;
	}
}
	/// \todo this constructor is thought for the case when more than one split is run on the system from the beginning
	/*public ConsistentHashingUtil(ArrayList<Integer> downstreamIndexes){
System.out.println("NEW ROUTING STRUCTURE");
for(Integer i : downstreamIndexes){
	System.out.println("They give me these indexes: "+i);
}
		
		//Initialize structures to do consistent hashing
		downstreamNodeKeys = new ArrayList<Integer>();
		keyToDownstreamNodeKey = new ArrayList<Integer>();
		
		long wholeSpace = (Integer.MAX_VALUE-1) * 2;
		int downSize = downstreamIndexes.size();
		long subSpace = wholeSpace/downSize;
System.out.println("WholeSpace: "+wholeSpace+", downSize: "+downSize+" and subSpace: "+subSpace);
		//This is the initialization, we save in virtual index 0, the real index of the downstream
		for(int i = 0; i < downstreamIndexes.size(); i++){
			//For every downstream of the same type, save in a virtual index, the real index of that downstream
System.out.println("I map the real index: "+downstreamIndexes.get(i)+" to the virtual index: "+i);
			virtualIndexToRealIndex.put(i, downstreamIndexes.get(i));
			virtualIndex++;
			
			//Compute the key and add it in the structure
			int key = (int) (Integer.MAX_VALUE - (subSpace * i));
System.out.println("For VirtualIndex: "+i+" I have this key: "+key);
			downstreamNodeKeys.add(key);
			
			//Compute the virtual downstream node and add it in 
			/// \todo{this data structure may be avoided}
System.out.println("Save the virtual index in the same position");
			keyToDownstreamNodeKey.add(i);			
		}
	}*/
	
	
//	public int updateDataStructures(int oldOpIndex, int newOpIndex){
//	//map real index to virtual index
//	int oldVirtualIndex = reverseMap(oldOpIndex);
//System.out.println("REAL index: "+oldOpIndex+" is VIRTUAL index: "+oldVirtualIndex);
//	//store newOpIndex as a virtualIndex
//System.out.println("Mapping new virtualIndex: "+virtualIndex+" to Real new index: "+newOpIndex);
//	virtualIndexToRealIndex.put(virtualIndex, newOpIndex);
//	
//	//. decide where to split key space
//	int key = downstreamNodeKeys.get(oldVirtualIndex);
//	//previous key is the min value if there was just one operator or the key of the previous operator to oldOpIndex
//	int previousKey = oldVirtualIndex == 0 ? Integer.MIN_VALUE : downstreamNodeKeys.get(oldVirtualIndex-1);
//	//the new key is the medium point between key and previous key
//	int newKey = (key-previousKey) / 2;
//System.out.println("The new Key is: "+newKey);
//
//	// install the new key and operator for dispatching, from this moment new tuples are buffered on
//	// store in oldOpIndex, the value of newOpIndex, (so insert by the left)
//	keyToDownstreamNodeKey.add(oldVirtualIndex, virtualIndex);
//	downstreamNodeKeys.add(oldVirtualIndex, newKey);
//	virtualIndex++;
//	/*
//	Buffer oldBuffer = ((OutputInformation)downstreamTypeConnection.get(oldOpIndex)).buffer;
//	Buffer newBuffer = ((OutputInformation)downstreamTypeConnection.get(newOpIndex)).buffer;
//
//	oldBuffer.split(newKey,newBuffer);
//	*/
//	return newKey;
//}	
	
	
	
//	// The special operator indicates
//	public String query = null;
//	public Method queryFunction = null;
//	public RouteOperator so = null;
//	public ArrayList<Integer> specialOperatorArguments = new ArrayList<Integer>();
//	public ArrayList<Integer> downstream = new ArrayList<Integer>();
//	
//	
//	//This map stores static info (for different types of downstream operators)
//	public HashMap<Integer, ArrayList<Integer>> routeInfo = new HashMap<Integer, ArrayList<Integer>>();
	
//	public int valueToChain = -1;
//	public RoutingStructure chainedFilter = null;
	
	
//	public RoutingStructure(String query, RouteOperator so, int specialOperatorArguments, int downstream){
//		this.query = query;
//		this.so = so;
//		this.specialOperatorArguments.add(specialOperatorArguments);
//		this.downstream.add(downstream);
//	}
//	
//	public RoutingStructure(String query, RouteOperator so, int specialOperatorArguments) {
//		this.query = query;
//		this.so = so;
//		this.specialOperatorArguments.add(specialOperatorArguments);
//	}
	
	/*public void updateDataStructures(){
	System.out.println("################################");
	System.out.println("downstream size: "+opContext.downstreams.size());
			for(PlacedOperator down: opContext.downstreams){
				long aux = Integer.MAX_VALUE;
				downstreamNodeKeys.add(((int) (Integer.MIN_VALUE+((aux*2)/opContext.getDownstreamSize()*(down.index()+1))))+1);
				//	indices are in inverse order
				keyToDownstreamNodeKey.add((opContext.getDownstreamSize()-1)-(down.index()));
			}
			for(int i = 0; i< downstreamNodeKeys.size(); i++){
				System.out.println("POS "+i+": "+downstreamNodeKeys.get(i));
			}
			for(int i = 0; i< keyToDownstreamNodeKey.size(); i++){
				System.out.println("POS "+i+": "+keyToDownstreamNodeKey.get(i));
			}
		}*/
	
//	public void init() {
//		try {
//			Class c = seep.comm.tuples.Seep.DataTuple.class;
//			queryFunction = c.getMethod(query);
//////ADHOC
////			auxiliar = c.getMethod("getDir");
////			auxiliar2 = c.getMethod("getXway");
//		}
//		catch (NoSuchMethodException nsme){
//			nsme.printStackTrace();
//		}
//		if(chainedFilter != null){
//			chainedFilter.init();
//		}
//	}

//	public void addArgument(int value) {
//		this.specialOperatorArguments.add(value);
//	}
//
//	public void addDownstream(int downstream) {
//		this.downstream.add(downstream);
//	}
	
//	public ArrayList<Integer> applyFilter(Seep.DataTuple dt, int value){
//		//In case this is a consistent-hashing case
//		if(query == null){
//			return routeByDownstreamSplit(dt, value);
//		}
//		//otherwise, apply filter and possibly chained filter 
//		else{
//			return routeByDownstreamType(dt);
//		}
//	}

	
	
//	private boolean doApplyChainFilter(int contentValue) {
//		//System.out.println("VALUE TO CHAIN: "+valueToChain+" contentValue: "+contentValue);
//				//if final filter ALWAYS applies
//				if(valueToChain == -1){
//					return true;
//				}
//				return valueToChain==contentValue;
//			}
//
//			public int getKeyFromValue(int downOpId) {
//				int v = -1;
//				//is dow)nsOpId within the downstreams of this filter?
//				// if yes, then valueArguments is what it is needed to return
//				// else we have to return 
//				return v;
//			}
	
	/*public ArrayList<Integer> routeByDownstreamType(Seep.DataTuple dt){
		ArrayList<Integer> aux = null;
		
		int contentValue = 0;
		try {
			contentValue = (Integer)queryFunction.invoke(dt);
////ADHOC
//			if(query.equals("getSeg")){
//				int xway = (Integer)auxiliar2.invoke(dt);
//				contentValue = contentValue + (1000*xway);
//				int dir = (Integer)auxiliar.invoke(dt);
//				contentValue = (dir == 1) ? (contentValue+1)*-1 : (contentValue)+1;
//			}
////END ADHOC
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//System.out.println("FILTER contentValue: "+contentValue);
		switch(so){
			case LEQ:
				if(contentValue <= specialOperatorArguments.get(0)){
					if(chainedFilter != null && doApplyChainFilter(contentValue)){
						aux = chainedFilter.applyFilter(dt);
						if(aux != null){
							return aux;
						}
						else{
							return downstream;
						}
					}
					else{
						return downstream;
					}
				}
				break;
			case L:
				if(contentValue < specialOperatorArguments.get(0)){
					if(chainedFilter != null && doApplyChainFilter(contentValue)){
						aux = chainedFilter.applyFilter(dt);
						if(aux != null){
							return aux;
						}
						else{
							return downstream;
						}
					}
					else{
						return downstream;
					}
				}
				break;
			case EQ:
				//if operator EQUAL, then use the routing map
//System.out.println("EQ; CHAINED FILTER: "+chainedFilter);
				if(chainedFilter != null && doApplyChainFilter(contentValue)){
					aux = chainedFilter.applyFilter(dt);
					if(aux != null){
						return aux;
					}
					else{
//						System.out.println("######CHAINED BUT NOT MATCH");
						return downstream;
					}
				}
				else{
					return routeInfo.get(contentValue);
				}
			case G:
				if(contentValue > specialOperatorArguments.get(0)){
					if(chainedFilter != null && doApplyChainFilter(contentValue)){
						aux = chainedFilter.applyFilter(dt);
						if(aux != null){
							return aux;
						}
						else{
							return downstream;
						}
					}
					else{
						return downstream;
					}
				}
				break;
			case GEQ:
				if(contentValue >= specialOperatorArguments.get(0)){
					if(chainedFilter != null && doApplyChainFilter(contentValue)){
						aux = chainedFilter.applyFilter(dt);
						if(aux != null){
							return aux;
						}
						else{
							return downstream;
						}
					}
					else{
						return downstream;
					}
				}
				break;
			case RANGE:
//				System.out.println("RANGE OPERATOR, contentValue: "+contentValue+" limit_: "+specialOperatorArguments.get(0)+" limit-: "+specialOperatorArguments.get(1));
				if(contentValue >= specialOperatorArguments.get(0) && contentValue <= specialOperatorArguments.get(1)){
//					System.out.println("IN RANGE");
					if(chainedFilter != null && doApplyChainFilter(contentValue)){
						aux = chainedFilter.applyFilter(dt);
						if(aux != null){
//							System.out.println("NEW SPLIT AND MATCHES!!!!!");
							return aux;
						}
						else{
//							System.out.println("NEW SPLIT but no downstream for it");
							return downstream;
						}
					}
					else{
//						System.out.println("NOT APPLIES");
						return downstream;
					}
				}
				break;
		}
		return null;
	}*/

	
	
//	//This map stores the relation between Operator Id and set of replicas of that operator
//	private HashMap<Integer, ArrayList<Integer>> opIdToReplicas = new HashMap<Integer, ArrayList<Integer>>();
	
	
	//This structure maps the real indexes (where is a given downstream) with the virtual index (where is a downstream within the set of downstream of same type
	// so map virtual integer with real integer
	/// \todo{consider change this structure to arraylist}
//	private HashMap<Integer, Integer> virtualIndexToRealIndex = new HashMap<Integer, Integer>();
//	private int virtualIndex = 0;

//	public void setRunning(boolean running) {
//		this.running = running;
//	}

