/*******************************************************************************
 * Copyright (c) 2014 Imperial College London
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Raul Castro Fernandez - initial API and implementation
 ******************************************************************************/
package uk.ac.imperial.lsds.seep.acita15.heatmap;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Location implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Location.class);
	public final int x;
	public final int y;
	
	public Location(int x, int y) { this.x = x; this.y = y; }
	public Location(String loc)
	{
		String[] coords = loc.split(",");
		this.x = (int)Double.parseDouble(coords[0]);
		this.y = (int)Double.parseDouble(coords[1]);
	}
	
	public int getX() { return x; }
	public int getY() { return y; }
}
