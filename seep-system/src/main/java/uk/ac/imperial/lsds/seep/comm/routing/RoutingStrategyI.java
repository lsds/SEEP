/*******************************************************************************
 * Copyright (c) 2013 Imperial College London.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial design and implementation
 *     Martin Rouaux - Added methods to collapse replicas
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.comm.routing;

import java.util.ArrayList;

public interface RoutingStrategyI {

	public ArrayList<Integer> route(int value);
	public ArrayList<Integer> route_lowestCost();
	public ArrayList<Integer> route(ArrayList<Integer> targets, int value);
	public ArrayList<Integer> routeToAll(ArrayList<Integer> targets);
	public ArrayList<Integer> routeToAll();
    
	public void update_lowestCost(int newTarget);
	public int[] newReplica(int oldOpIndex, int newOpIndex);
    public int[] collapseReplica(int opIndex);
    
	public int[] newStaticReplica(int oldOpIndex, int newOpIndex);
    public int[] collapseStaticReplica(int opIndex);
}
