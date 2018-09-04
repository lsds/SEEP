/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Matteo Migliavacca - Definition of inner classes and method implementation
 *     Martin Rouaux - Removal of upstream and downstream connections (OperatorStaticInformation)
 *     which is required to support scale-in of operators.
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.operator;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.imperial.lsds.seep.infrastructure.master.Node;
import uk.ac.imperial.lsds.seep.manet.Query;


public class OperatorContext implements Serializable{

	private static final long serialVersionUID = 1L;
	/** VAR -> Location information of this operator and of their upstream and downstream **/
	private OperatorStaticInformation location;
	public final DownIter downstreams = new DownIter();
	public final UpIter upstreams = new UpIter();
	private ArrayList<Integer> connectionsD = new ArrayList<Integer>();
	private ArrayList<Integer> connectionsU = new ArrayList<Integer>();
	private ArrayList<OperatorStaticInformation> upstream = new ArrayList<OperatorStaticInformation>();
	private ArrayList<OperatorStaticInformation> downstream = new ArrayList<OperatorStaticInformation>();
	private ArrayList<Integer> originalDownstream = new ArrayList<Integer>();
	
	private boolean isSource = false;
	private boolean isSink = false;
	private Query frontierQuery = null;
	
	// store the type of input data ingestion mode per upstream operator. <OpId - InputDataIngestionMode>
	private Map<Integer, InputDataIngestionMode> inputDataIngestionModePerUpstream = new HashMap<Integer, InputDataIngestionMode>();
	//This map stores static info (for different types of downstream operators). StreamId -> list of downstreams
	public HashMap<Integer, ArrayList<Integer>> routeInfo = new HashMap<Integer, ArrayList<Integer>>();
	
	/** Tuple dependent information **/
	private List<String> declaredWorkingAttributes;
	private String keyAttribute = null;
	
	public OperatorContext(){
		
	}
	
	public boolean isSource(){
		return isSource;
	}
	
	public boolean isSink(){
		return isSink;
	}
	
	public void setIsSource(boolean isSource){
		this.isSource = isSource;
	}
	
	public void setIsSink(boolean isSink){
		this.isSink = isSink;
	}
	
	public void setFrontierQuery(Query query) { this.frontierQuery = query; }
	public Query getFrontierQuery() { return frontierQuery; }
	
	public boolean doesRequireLogicalRouting(){
		// If there are more than one addressable streamIds in the logicalRouting table, it does require specific routing
		return routeInfo.size() > 1;
	}
	
	public String getKeyAttribute(){
		return keyAttribute;
	}
	
	public void setKeyAttribute(String key){
		this.keyAttribute = key;
	}
	
	public void setDeclaredWorkingAttributes(List<String> declaredWorkingAttributes){
		this.declaredWorkingAttributes = declaredWorkingAttributes;
	}
	
	public List<String> getDeclaredWorkingAttributes(){
		return declaredWorkingAttributes;
	}
	
	public HashMap<Integer, ArrayList<Integer>> getRouteInfo(){
		return routeInfo;
	}
	
        public void setRouteInfo(HashMap<Integer, ArrayList<Integer>> map){
		routeInfo = map;
	}
	public Map<Integer, InputDataIngestionMode> getInputDataIngestionModePerUpstream(){
		return inputDataIngestionModePerUpstream;
	}
	
	public void setInputDataIngestionModePerUpstream(int opId, InputDataIngestionMode mode){
		inputDataIngestionModePerUpstream.put(opId, mode);
	}
	
	public int getOriginalUpstreamFromOpId(int opId){
		for(OperatorStaticInformation op : upstream){
			if(op.getOpId() == opId){
				return op.getOriginalOpId();
			}
		}
		///\fixme{make operators id consistent, or propagate errors with exceptions otherwise}
		return -1000;
	}
	
	public int getUpstreamNumberOfType(int originalOpId){
		int total = 0;
		for(OperatorStaticInformation op : upstream){
			if(op.getOriginalOpId() == originalOpId) total++;
		}
		return total;
	}
	
	private static boolean ipEquals(InetAddress ip1, InetAddress ip2) {
		String ip1Str = ip1.getHostAddress();
		String ip2Str = ip2.getHostAddress();

		return ip1.equals(ip2) ||
				ip1Str.equals("127.0.0.1") && ip2Str.equals("127.0.1.1") ||
				ip1Str.equals("127.0.1.1") && ip2Str.equals("127.0.0.1");
	}

	public int getOpIdFromUpstreamIp(InetAddress ip){
		for(OperatorStaticInformation op : upstream){
			if(ipEquals(op.getMyNode().getIp(), ip)){
				return op.getOpId();
			}
		}
		///\fixme{make operators id consistent, or propagate errors with exceptions otherwise}
		return -1000;
	}

	public int getOpIdFromUpstreamIpPort(InetAddress ip, int srcPort)
	{
		//TODO: What if a join?
		//First get the set of upstream op ids (ids) that match the ip.
		List<Integer> sameNodeUpstreams = new ArrayList<>();
		for(OperatorStaticInformation op : upstream){
			if(ipEquals(op.getMyNode().getIp(), ip)){
				sameNodeUpstreams.add(op.getOpId());
			}
		}

		//If n = |ids| == 1 one then return the id
		//If n > 1, get replica local index r = srcPort mod n and
		// return sorted(ids)[r]

		int upstreamIndex = srcPort % sameNodeUpstreams.size();
		Collections.sort(sameNodeUpstreams);
		int upstreamId = sameNodeUpstreams.get(upstreamIndex);	
		return upstreamId;
	}

	public int getDownOpIdFromIndex(int index){
		int opId = 0;
		for(PlacedOperator down : downstreams){
			if(down.index() == index){
				return down.opID();
			}
		}
		return opId;
	}
	
	public int getUpOpIdFromIndex(int index){
		int opId = 0;
		for(PlacedOperator up : upstreams){
			if(up.index() == index){
				return up.opID();
			}
		}
		return opId;
	}
	
	public ArrayList<Integer> getListOfDownstreamIndexes() {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for(PlacedOperator down : downstreams){
			indexes.add(down.index());
		}
		return indexes;
	}
	
	public ArrayList<Integer> getListOfUpstreamIndexes()
	{
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for(PlacedOperator up: upstreams){
			indexes.add(up.index());
		}
		return indexes;
	}

	//Check if the given opId is statefull
	public boolean isDownstreamOperatorStateful(int opId) {
		for(PlacedOperator op : downstreams){
			if(op.opID() == opId){
				if(op.location().isStatefull()){
					return true;
				}
				else{
					return false;
				}
			}
		}
		return false;
	}
	
	//Check if all downstreamms are stateful
	public boolean isDownstreamStateful() {
		for(PlacedOperator op : downstreams){
			if((op.location().isStatefull())){
				return true;
			}
		}
		return false;
	}
	
	public PlacedOperator minimumUpstream() {
		PlacedOperator min = null;
		for (PlacedOperator op : upstreams) {
			if (min==null) { min = op; }
			else if (min.opID() > op.opID()) { min = op; }
		}
		return min;
	}
	
	public int getDownstreamSize() { 
		return downstream.size(); 
	}
	
	public int getUpstreamSize(){
		return upstream.size();
	}
	
	public OperatorStaticInformation getDownstreamLocation(int opID) {
		return downstream.get(findOpIndex(opID, connectionsD));
	}
	
	public OperatorStaticInformation getUpstreamLocation(int opID) {
		return upstream.get(findOpIndex(opID, connectionsU));
	}
	
	public PlacedOperator findUpstream(int opId){
		for (PlacedOperator op : upstreams) {
			if (op.opID() == opId) return op;
		}
		return null;
	}
	
	public PlacedOperator findDownstream(int opID) {
		for (PlacedOperator op : downstreams) {
			if (op.opID() == opID) return op;
		}
		return null;
	}
	
	public int getDownOpIndexFromOpId(int opId){
		for(PlacedOperator po : downstreams){
			if(po.opID() == opId) return po.index;
		}
		return -1;
	}
	
	public int getUpOpIndexFromOpId(int opId){
		for(PlacedOperator po : upstreams){
			if(po.opID() == opId) return po.index;
		}
		return -1;
	}
	
	public ArrayList<Integer> getDownstreamOpIdList()
	{
		return connectionsD;
	}
	
	public ArrayList<Integer> getUpstreamOpIdList()
	{
		return connectionsU;
	}
	
	/** Methods called by QuerySpecificationI **/
	
	public void setOriginalDownstream(ArrayList<Integer> originalDownstream){
		this.originalDownstream = originalDownstream;
	}
	
	public void addDownstream(int opID) {
		connectionsD.add(new Integer(opID));
	}
	
    public void removeDownstream(int opID) {
        connectionsD.remove(new Integer(opID));
    }
    
	public void addOriginalDownstream(Integer opId){
		originalDownstream.add(opId);
	}
	
	public void addUpstream(int opID) {
		connectionsU.add(new Integer(opID));
	}
    
    public void removeUpstream(int opID) {
        connectionsU.remove(new Integer(opID));
    }

//	public void setQueryAttribute(String queryAttribute){
//		this.queryAttribute = queryAttribute;
//	}
	
	//if less or greater is than a given value. if equal could be with many values, with range is a special case as well
//	public void routeValueToDownstream(RelationalOperator operator, int value, int downstream){
//		//if it is operator EQUALS, use specific routeInfo
//		if(operator.equals(RelationalOperator.EQ)){
//			//If there was a downstream assigned for this value
//			if(routeInfo.containsKey(value)){
//				// add the new downstream
//				routeInfo.get(value).add(downstream);
//			}
//			else{
//				ArrayList<Integer> aux = new ArrayList<Integer>();
//				aux.add(downstream);
//				routeInfo.put(value,aux);
//			}
//		}
//	}
	
	public void routeValueToDownstream(int streamId, int downstream){
		if(routeInfo.containsKey(streamId)){
			// add the new downstream
			routeInfo.get(streamId).add(downstream);
		}
		else{
			ArrayList<Integer> aux = new ArrayList<Integer>();
			aux.add(downstream);
			routeInfo.put(streamId, aux);
		}
	}
	
	/** Methods called by Infrastructure **/
	
	public OperatorStaticInformation getOperatorStaticInformation(){
		return location;
	}
	
	public void setOperatorStaticInformation(OperatorStaticInformation location){
		this.location = location;
	}
	
	public void setDownstreamOperatorStaticInformation(int opID, OperatorStaticInformation loc) {
		addOrReplace(opID, loc, connectionsD, downstream);
	}

	//Add if it is a new upstrea, replace if it is a previous upstream that failer or that have changed node for any other reason
	public void setUpstreamOperatorStaticInformation(int opID, OperatorStaticInformation loc) {
		addOrReplace(opID, loc, connectionsU, upstream);
	}
	
	private void addOrReplace(int opID, OperatorStaticInformation loc, ArrayList<Integer> IDList, ArrayList<OperatorStaticInformation> LocationList) {
		int opIndex = findOpIndex(opID, IDList);
		if (opIndex<LocationList.size())
			LocationList.set(opIndex, loc);
		else {
			for (int index = LocationList.size(); index < opIndex; index++) {
				LocationList.add(null);
			}
			LocationList.add(loc);
		}
	}
	
        public int findOpIndexFromDownstream(int opId){
            for(Integer id : connectionsD){
                if(id == opId){
                    return connectionsD.indexOf(id);
                }
            }
            return -1;
        }
        
	private int findOpIndex(int opID, ArrayList<Integer> IDList) {
		for(Integer id : IDList){
			if(id == opID){
				return IDList.indexOf(id);
			}
		}
		throw new IllegalArgumentException("opID "+ opID + " not found");
	}
	
	//this method and the following one do not support changing the node with
	//a new node with a different port, however this is not a problem for operators,
	//that do not need to contact the secondary but just need ports for operators.
	//the slight problem is that if the new node has a different port this would not
	//be reflected in the node object, which might be confusing for debugging.
	public void changeLocation(InetAddress oldIp, InetAddress newIp){
		for(int i = 0; i < upstream.size(); i++){
			if(upstream.get(i).getMyNode().getIp().equals(oldIp)){
				Node newNode = upstream.get(i).getMyNode().setIp(newIp);
				OperatorStaticInformation newLoc = upstream.get(i).setNode(newNode);
				upstream.set(i, newLoc);
			}
		}
		for(int i = 0; i < downstream.size(); i++){
			if(downstream.get(i).getMyNode().getIp().equals(oldIp)){
				Node newNode = downstream.get(i).getMyNode().setIp(newIp);
				OperatorStaticInformation newLoc = downstream.get(i).setNode(newNode);
				downstream.set(i, newLoc);
			}
		}
		throw new RuntimeException("Channge location");
	}

	public void changeLocation(int opId, InetAddress newIp){
		for(int i = 0; i < upstream.size(); i++){
			if((upstream.get(i).getInD() - 40000) == opId){
				Node newNode = upstream.get(i).getMyNode().setIp(newIp);
				OperatorStaticInformation newLoc = upstream.get(i).setNode(newNode);
				upstream.set(i, newLoc);
			}
		}
		for(int i = 0; i < downstream.size(); i++){
			if((downstream.get(i).getInD() - 40000) == opId){
				Node newNode = downstream.get(i).getMyNode().setIp(newIp);
				OperatorStaticInformation newLoc = downstream.get(i).setNode(newNode);
				downstream.set(i, newLoc);
			}
		}
		throw new RuntimeException("Channge location");
	}
	
	
	public ArrayList<Integer> getOriginalDownstream(){
		return originalDownstream;
	}
	
	/** Support classes **/
	
	//TODO place opID in location instead of this?
	public static class PlacedOperator {
		int index;
		ArrayList<Integer> conns;
		ArrayList<OperatorStaticInformation> locs;
		
		public PlacedOperator(int index, ArrayList<Integer> conns, ArrayList<OperatorStaticInformation> locs) {
			this.index = index;
			this.conns = conns;
			this.locs = locs;
		}
		
		public OperatorStaticInformation location() { 
			return locs.get(index); 
		}
		
		public boolean isStateful(){
			return locs.get(index).isStatefull();
		}
		
		public int opID() {  
			return conns.get(index); 
		}
		public int index() { 
			return index; 
		}
	};
	
	static class Iter implements Iterator<PlacedOperator> {

		int index = -1;
		ArrayList<Integer> conns;
		ArrayList<OperatorStaticInformation> locs;

		private Iter(ArrayList<Integer> conns, ArrayList<OperatorStaticInformation> locs) { this.conns = conns; this.locs = locs; }
		public boolean hasNext() { return index<conns.size()-1; }

		public PlacedOperator next() { index++; return new PlacedOperator(index, conns, locs); }

		public void remove() { throw new UnsupportedOperationException(); }
	}
	
	public class DownIter implements Iterable<PlacedOperator>, Serializable {
		private static final long serialVersionUID = 1L;
		public Iterator<PlacedOperator> iterator() { 
			return new Iter(connectionsD,downstream); 
		}
		public int size() { 
			return connectionsD.size(); 
		}
	}
	
	public class UpIter implements Iterable<PlacedOperator>, Serializable {
		private static final long serialVersionUID = 1L;
		public Iterator<PlacedOperator> iterator() { 
			return new Iter(connectionsU,upstream); 
		}
		public int size() { 
			return connectionsU.size(); 
		}
	}
	
	@Override 
	public String toString() {
		StringBuffer buf = new StringBuffer("@"+ getOperatorStaticInformation());
		if (downstream.size()>0) {
			buf.append(" down: [");
			for (PlacedOperator op : downstreams) {
				buf.append("Id: "+op.opID() + op.location());
				System.out.println();
			}
			buf.append("]");
		}
		if (upstream.size()>0) {
			buf.append(" up: [");
			for (PlacedOperator op : upstreams) {
				buf.append("Id: "+op.opID() + op.location());
				System.out.println();
			}
			buf.append("]");
		}
		return buf.toString();
	}
}
