/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers;

import java.net.InetAddress;

public class ReconfigureConnection {

	private int opId;
	private int originalOpId;
	private String command;
	private String ip;
	private int node_port;
	private int inC;
	private int inD;
	private boolean operatorNature;
	private String operatorType;
//	private InetAddress ipStarTopology;
//	private int opIdStarTopology; 
	
	public ReconfigureConnection(){}
	
	public ReconfigureConnection(String command){
		this.command = command;
	}
	
	public ReconfigureConnection(int opId, String command, String ip){
		this.opId = opId;
		this.command = command;
		this.ip = ip;
	}
	
	public ReconfigureConnection(int opId, int originalOpId, String command, String ip,int nodePort, int inC, int inD, boolean operatorNature, String operatorType) {
		this.opId = opId;
		this.originalOpId = originalOpId;
		this.command = command;
		this.ip = ip;
		this.node_port = nodePort;
		this.inC = inC;
		this.inD = inD;
		this.operatorNature = operatorNature;
		this.operatorType = operatorType;
	}
	
	public ReconfigureConnection(int opId, String command, int inC){
		this.opId = opId;
		this.command = command;
		this.inC = inC;
	}
	
//	public InetAddress getIpStarTopology() {
//		return ipStarTopology;
//	}
//
//	public void setIpStarTopology(InetAddress ipStarTopology) {
//		this.ipStarTopology = ipStarTopology;
//	}
//
//	public int getOpIdStarTopology() {
//		return opIdStarTopology;
//	}
//
//	public void setOpIdStarTopology(int opIdStarTopology) {
//		this.opIdStarTopology = opIdStarTopology;
//	}

	public int getOpId() {
		return opId;
	}
	
	public int getOriginalOpId(){
		return originalOpId;
	}
	
	public void setOpId(int opId) {
		this.opId = opId;
	}
	
	public void setOriginalOpId(int opId){
		this.originalOpId = opId;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public int getNode_port() {
		return node_port;
	}
	
	public void setNode_port(int nodePort) {
		node_port = nodePort;
	}
	
	public int getInC() {
		return inC;
	}
	
	public void setInC(int inC) {
		this.inC = inC;
	}
	
	public int getInD() {
		return inD;
	}
	
	public void setInD(int inD) {
		this.inD = inD;
	}
	
	public boolean getOperatorNature() {
		return operatorNature;
	}
	
	public void setOperatorNature(boolean operatorNature) {
		this.operatorNature = operatorNature;
	}
	
	public String getOperatorType() {
		return operatorType;
	}
	
	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;
	}
}
